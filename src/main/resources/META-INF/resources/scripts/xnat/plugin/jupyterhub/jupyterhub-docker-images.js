/*!
 * JupyterHub docker image preference functions
 */

console.debug('jupyterhub-docker-images.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.dockerImages = getObject(XNAT.plugin.jupyterhub.dockerImages || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {


    function dockerImagePreferenceUrl() {
        let url = '/xapi/jupyterhub/preferences/dockerImages';
        return XNAT.url.restUrl(url);
    }

    let serverErrorHandler = function(e, title, closeAll) {
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        const errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + '</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function() {
                        if (closeAll) {
                            xmodal.closeAll();
                        }
                    }
                }
            ]
        });
    }

    let clientErrorHandler = function(errorMsg) {
        let errors = [];
        errorMsg.forEach(function(msg) { errors.push(spawn('li',msg)) });

        return spawn('div',[
            spawn('p', 'Errors found:'),
            spawn('ul', errors)
        ]);
    }

    XNAT.plugin.jupyterhub.dockerImages.getImages = async function() {
        console.debug(`jupyterhub-docker-images.js: XNAT.plugin.jupyterhub.dockerImages.getImages`);

        const response = await fetch(dockerImagePreferenceUrl(), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (response.ok) {
            let dockerImages = await response.json();
            XNAT.plugin.jupyterhub.dockerImages.images = dockerImages['dockerImages'];
            return dockerImages['dockerImages'];
        } else {
            const message = `An error has occurred get docker image preferences: ${response.status}`;
            throw new Error(message);
        }
    }

    XNAT.plugin.jupyterhub.dockerImages.dialog = function() {
        XNAT.dialog.open({
            title: 'Add Docker Image',
            content: spawn('form'),
            maxBtn: true,
            width: 600,
            beforeShow: function(obj) {
                // spawn new image form
                const formContainer$ = obj.$modal.find('.xnat-dialog-content');
                formContainer$.addClass('panel');
                obj.$modal.find('form').append(
                    spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'dockerImage',
                            id: 'dockerImage',
                            label: 'Docker Image',
                            description: 'JupyterHub enabled Docker image. Make sure to include the tag.'
                        }).element,
                        XNAT.ui.panel.input.switchbox({
                            name: 'enabled',
                            id: 'enabled',
                            label: 'Enabled',
                            checked: true,
                            value: 'true'
                        }).element
                    ])
                );
            },
            buttons: [
                {
                    label: 'Save',
                    isDefault: true,
                    close: false,
                    action: function() {
                        // on save
                        // get inputs
                        const dockerImageEl = document.getElementById("dockerImage");
                        const enabledEl = document.getElementById("enabled");

                        // validator for dockerImage
                        let validateDockerImage = XNAT.validate(dockerImageEl).reset().chain();
                        validateDockerImage.minLength(1).failure('Docker Image is a required field.');

                        // validator for enabled
                        let validateEnabled = XNAT.validate(enabledEl).reset().chain();
                        validateEnabled.required().failure('Enabled is a required field.');

                        // validate fields
                        let errorMessages = [];

                        [validateDockerImage, validateEnabled].forEach(validator => {
                            validator.check();
                            validator.messages.forEach(message => errorMessages.push(message))
                        })

                        if (errorMessages.length) {
                            // errors?
                            XNAT.dialog.open({
                                title: 'Validation Error',
                                width: 300,
                                content: clientErrorHandler(errorMessages)
                            })
                        } else {
                            // no errors -> send to xnat
                            let imageToSave = {
                                image: dockerImageEl.value,
                                enabled: enabledEl.value
                            };

                            // add new image
                            XNAT.plugin.jupyterhub.dockerImages.images.push(imageToSave);

                            XNAT.xhr.post({
                                url: dockerImagePreferenceUrl(),
                                data: JSON.stringify(XNAT.plugin.jupyterhub.dockerImages.images), // Submit all data, not just the single image
                                contentType: 'application/json',
                                success: function () {
                                    XNAT.ui.banner.top(1000, '<b>"' + imageToSave['image'] + '"</b> saved.', 'success');
                                    XNAT.plugin.jupyterhub.dockerImages.refresh();
                                    xmodal.closeAll();
                                    XNAT.ui.dialog.closeAll();
                                },
                                fail: function (e) {
                                    serverErrorHandler(e, 'Failed to add image');
                                }
                            })
                        }
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        });
    }

    XNAT.plugin.jupyterhub.dockerImages.table = function(containerId, callback) {
        console.debug(`jupyterhub-docker-images.js: XNAT.plugin.jupyterhub.dockerImages.table`);

        // initialize the table
        const dockerImagesTable = XNAT.table({
            className: 'dockerImages xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        dockerImagesTable.tr()
            .th({ addClass: 'left', html: '<b>Image</b>' })
            .th('<b>Enabled</b>')
            .th('<b>Actions</b>')

        function enabledToggle(image, checked){
            let enabled = !!checked;
            let ckbox = spawn('input.enabled', {
                type: 'checkbox',
                checked: enabled,
                value: 'true',
                data: { name: image, checked: enabled },
                onchange: () => {
                    let index = XNAT.plugin.jupyterhub.dockerImages.images.findIndex(img => img['image'] === image);
                    XNAT.plugin.jupyterhub.dockerImages.images[index]['enabled'] = !enabled;

                    XNAT.xhr.post({
                        url: dockerImagePreferenceUrl(),
                        data: JSON.stringify(XNAT.plugin.jupyterhub.dockerImages.images), // Submit all data, not just the single image
                        contentType: 'application/json',
                        success: function () {
                            console.debug("Success")
                        },
                        fail: function (e) {
                            serverErrorHandler(e, 'Failed to update image');
                        }
                    })

                }
            });

            return spawn('label.switchbox', [ckbox, ['span.switchbox-outer', [['span.switchbox-inner']]]]);
        }

        function deleteButton(imageToDelete) {
            return spawn('button.btn.sm.delete', {
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>Are you sure you'd like to delete this image?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            let newImages = XNAT.plugin.jupyterhub.dockerImages.images.filter(image => image.image !== imageToDelete.image);

                            XNAT.xhr.post({
                                url: dockerImagePreferenceUrl(),
                                data: JSON.stringify(newImages),
                                contentType: 'application/json',
                                success: function() {
                                    XNAT.ui.banner.top(1000, '<b>"'+ imageToDelete['image'] + '"</b> deleted.', 'success');
                                    XNAT.plugin.jupyterhub.dockerImages.refresh();
                                },
                                fail: function(e) {
                                    serverErrorHandler(e, 'Failed to delete image');
                                }
                            });
                        }
                    })
                },
                disabled: XNAT.plugin.jupyterhub.dockerImages.images.length <= 1,
                title: XNAT.plugin.jupyterhub.dockerImages.images.length <= 1 ? "Cannot delete the only image" : "Delete Image",
            }, [ spawn('i.fa.fa-trash') ]);
        }

        XNAT.plugin.jupyterhub.dockerImages.getImages().then(images => {
            // Sort table by image name
            images.sort(XNAT.plugin.jupyterhub.dockerImages.comparator('image'))

            // Create row for each image
            images.forEach(image => {
                dockerImagesTable.tr()
                    .td([ spawn('div.left', [image['image']]) ])
                    .td([ spawn('div.center', [enabledToggle(image['image'], image['enabled'])]) ])
                    .td([ spawn('div.center', [deleteButton(image)]) ])
            })

            if (containerId) {
                $$(containerId).append(dockerImagesTable.table);
            }

            if (isFunction(callback)) {
                callback(dockerImagesTable.table);
            }
        })

        XNAT.plugin.jupyterhub.dockerImages.$table = $(dockerImagesTable.table);

        return dockerImagesTable.table;
    }

    XNAT.plugin.jupyterhub.dockerImages.comparator = function(property) {
        return function(a,b) {
            const aValue = a[property].toUpperCase()
            const bValue = b[property].toUpperCase()

            return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        }
    }

    XNAT.plugin.jupyterhub.dockerImages.init = function(container) {
        console.debug(`jupyterhub-docker-images.js: XNAT.plugin.jupyterhub.dockerImages.init`);

        const $manager = $$(container || 'div#jupyterhub-image-preferences');
        const $footer = $('#jupyterhub-image-preferences').parents('.panel').find('.panel-footer');

        XNAT.plugin.jupyterhub.dockerImages.$container = $manager;

        $manager.append(XNAT.plugin.jupyterhub.dockerImages.table());

        const newImage = spawn('button.new-image.btn.btn-sm.submit', {
            html: 'Add Image',
            onclick: function() {
                XNAT.plugin.jupyterhub.dockerImages.dialog();
            }
        });

        // add the 'add new' button to the panel footer
        $footer.append(spawn('div.pull-right', [newImage]));
        $footer.append(spawn('div.clear.clearFix'));

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function() {
                return $manager[0]
            }
        };

    }

    XNAT.plugin.jupyterhub.dockerImages.refresh = function() {
        console.debug(`jupyterhub-docker-images.js: XNAT.plugin.jupyterhub.dockerImages.refresh`);

        XNAT.plugin.jupyterhub.dockerImages.$table.remove();
        XNAT.plugin.jupyterhub.dockerImages.table(null, function(table) {
            XNAT.plugin.jupyterhub.dockerImages.$container.prepend(table);
        });
    };

}));