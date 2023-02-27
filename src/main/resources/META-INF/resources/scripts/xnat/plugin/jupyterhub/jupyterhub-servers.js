/*!
 * JupyterHub server functions
 */

console.debug('jupyterhub-servers.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.servers = getObject(XNAT.plugin.jupyterhub.servers || {});
XNAT.plugin.jupyterhub.servers.user_options = getObject(XNAT.plugin.jupyterhub.servers.user_options || {});

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

    let restUrl = XNAT.url.restUrl;

    let newServerUrl = XNAT.plugin.jupyterhub.servers.newServerUrl = function(username, servername, xsiType, itemId,
                                                                              itemLabel, projectId, eventTrackingId,
                                                                              profileId) {
        let url = `/xapi/jupyterhub/users/${username}/server`;

        // if (servername !== '') {
        //     url = `${url}/${servername}`;
        // }

        url = `${url}?xsiType=${xsiType}&itemId=${itemId}&itemLabel=${itemLabel}&eventTrackingId=${eventTrackingId}&profileId=${profileId}`;
        if (projectId) url = `${url}&projectId=${projectId}`;

        return restUrl(url);
    }

    let serverUrl = XNAT.plugin.jupyterhub.servers.serverUrl = function(username, servername, eventTrackingId) {
        let url = `/xapi/jupyterhub/users/${username}/server/${servername}?eventTrackingId=${eventTrackingId}`;
        return restUrl(url);
    }

    let getSubjectLabel = async function(subjectId) {
        console.debug(`jupyterhub-servers.js: getSubjectLabel`);

        const response = await fetch(XNAT.url.restUrl(`/data/subjects/${subjectId}?format=json`), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (response.ok) {
            let result = await response.json()
            return result['items'][0]['data_fields']['label']
        }
    }

    let getExperimentLabel = async function(experimentId) {
        console.debug(`jupyterhub-servers.js: getExperimentLabel`);

        const response = await fetch(XNAT.url.restUrl(`/data/experiments/${experimentId}?format=json`), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (response.ok) {
            let result = await response.json()
            return result['items'][0]['data_fields']['label']
        }
    }

    XNAT.plugin.jupyterhub.servers.startServerForProject = function(username = window.username,
                                                            servername = XNAT.data.context.projectID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            itemId = XNAT.data.context.projectID,
                                                            projectId = XNAT.data.context.projectID,
                                                            eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForProject`);
        startServer(username, servername, xsiType, itemId, projectId, projectId, eventTrackingId)
    }

    XNAT.plugin.jupyterhub.servers.startServerForSubject = function(username = window.username,
                                                            servername = XNAT.data.context.ID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            itemId = XNAT.data.context.ID,
                                                            projectId = XNAT.data.context.projectID,
                                                            eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForSubject`);

        if (XNAT.plugin.jupyterhub.isSharedItem()) {
            XNAT.dialog.open({
                width: 450,
                title: "Unsupported Operation",
                content: "Cannot start a Jupyter server on a shared subject.",
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                        action: function() {
                                xmodal.closeAll();
                        }
                    }
                ]
            });
        } else {
            getSubjectLabel(itemId).then(subjectLabel => startServer(username, servername, xsiType, itemId,
                                                                     subjectLabel, projectId, eventTrackingId))
        }
    }

    XNAT.plugin.jupyterhub.servers.startServerForExperiment = function(username = window.username,
                                                               servername = XNAT.data.context.ID,
                                                               xsiType = "xnat:experimentData",
                                                               itemId = XNAT.data.context.ID,
                                                               projectId = XNAT.data.context.projectID,
                                                               eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForExperiment`);

        if (XNAT.plugin.jupyterhub.isSharedItem()) {
            XNAT.dialog.open({
                width: 450,
                title: "Unsupported Operation",
                content: "Cannot start a Jupyter server on a shared experiment.",
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                        action: function() {
                            xmodal.closeAll();
                        }
                    }
                ]
            });
        } else {
            getExperimentLabel(itemId).then(experimentLabel => startServer(username, servername, xsiType, itemId,
                                                                           experimentLabel, projectId, eventTrackingId))
        }
    }

    XNAT.plugin.jupyterhub.servers.startServerForStoredSearch = function(username, servername, xsiType, itemId,
                                                                         itemLabel, projectId, eventTrackingId) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForStoredSearch`);
         startServer(username, servername, xsiType, itemId, itemLabel, projectId, eventTrackingId)
    }


    let handleServerError = XNAT.plugin.jupyterhub.servers.handleServerError = function(e, title, closeAll) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.handleServerError`);

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

    let startServer = XNAT.plugin.jupyterhub.servers.startServer = function(username, servername, xsiType, itemId, itemLabel, projectId, eventTrackingId) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServer`);
        console.debug(`Launching jupyter server. User: ${username}, Server Name: ${servername}, XSI Type: ${xsiType}, ID: ${itemId}, Label: ${itemLabel}, Project ID: ${projectId}, eventTrackingId: ${eventTrackingId}`);

        XNAT.plugin.jupyterhub.profiles.getAll().then(profiles => {
            // filter out disabled configurations
            profiles = profiles.filter(c => c['enabled'] === true);
            
            const cancelButton = {
                label: 'Cancel',
                isDefault: false,
                close: true,
            }
            
            const startButton = {
                label: 'Start Jupyter',
                isDefault: true,
                close: true,
                action: function(obj) {
                    const profile = obj.$modal.find('#profile').val();
                    const profileId = profiles.filter(c => c['name'] === profile)[0]['id'];
        
                    XNAT.xhr.ajax({
                        url: newServerUrl(username, servername, xsiType, itemId, itemLabel,
                            projectId, eventTrackingId, profileId),
                        method: 'POST',
                        contentType: 'application/json',
                        beforeSend: function () {
                            XNAT.app.activityTab.start(
                                'Start Jupyter Notebook Server' +
                                `<div class="actions"><a id="open-nb-${eventTrackingId}" class="icn open" style="display: none;"><i class="fa fa-book"></i></a>`,
                                eventTrackingId,
                                'XNAT.plugin.jupyterhub.servers.activityTabCallback', 1000);
                        },
                        fail: function (error) {
                            console.error(`Failed to send Jupyter server request: ${error}`)
                        }
                    });
                }
            }
            
            const buttons = (profiles.length === 0) ? [cancelButton] : [cancelButton, startButton];
            
            XNAT.dialog.open({
                title: 'Select Jupyter Profile',
                content: spawn('form'),
                maxBtn: true,
                width: 750,
                beforeShow: function(obj) {
                    if (profiles.length === 0) {
                        obj.$modal.find('.xnat-dialog-content').html('<div class="error">No Jupyter profiles are configured or enabled. Please contact your XNAT administrator.</div>');
                        return;
                    }
                    
                    let configOptions = profiles.map(c => c['name'])
                                                      .map(c => [{value: c}])
                                                      .flat();
                    
                    // spawn new image form
                    const formContainer$ = obj.$modal.find('.xnat-dialog-content');
                    formContainer$.addClass('panel');
                    
                    let initialProfile = {
                        name: profiles[0]['name'],
                        description: profiles[0]['description'],
                        image: profiles[0]['task_template']['container_spec']['image'],
                        cpuLimit: profiles[0]['task_template']['resources']['cpu_limit'] ? profiles[0]['task_template']['resources']['cpu_limit'] : 'No Limit',
                        cpuReservation: profiles[0]['task_template']['resources']['cpu_reservation'] ? profiles[0]['task_template']['resources']['cpu_reservation'] : 'No Reservation',
                        get cpu() {
                            return `${this.cpuReservation} / ${this.cpuLimit}`;
                        },
                        memoryLimit: profiles[0]['task_template']['resources']['mem_limit'] ? profiles[0]['task_template']['resources']['mem_limit'] : 'No Limit',
                        memoryReservation: profiles[0]['task_template']['resources']['mem_reservation'] ? profiles[0]['task_template']['resources']['mem_reservation'] : 'No Reservation',
                        get memory() {
                            return `${this.memoryReservation} / ${this.memoryLimit}`;
                        }
                    }
    
                    obj.$modal.find('form').append(
                        spawn('!', [
                            XNAT.ui.panel.select.single({
                                name: 'profile',
                                id: 'profile',
                                options: configOptions,
                                label: 'Profile',
                                required: true,
                                description: "Select the Jupyter profile to use. This will determine the Docker image, computing resources, and other configuration options used for your Jupyter notebook server."
                            }).element,
                            XNAT.ui.panel.element({
                                label: 'Description',
                                html: `<p class="profile-description">${initialProfile.description}</p>`,
                            }).element,
                            XNAT.ui.panel.element({
                                label: 'Image',
                                html: `<p class="profile-image">${initialProfile.image}</p><div class="description">The Docker image that will be used to launch your Jupyter notebook server.</div>`,
                            }).element,
                            XNAT.ui.panel.element({
                                label: 'CPU Reservation / Limit',
                                html: `<p class="profile-cpu">${initialProfile.cpu}</p><div class="description">The reservation is the minimum amount of CPU resources that will be guaranteed to your server. The limit is the maximum amount of CPU resources that will be allocated to your server.</div>`,
                            }).element,
                            XNAT.ui.panel.element({
                                label: 'Memory Reservation / Limit',
                                html: `<p class="profile-memory">${initialProfile.memory}</p><div class="description">The reservation is the minimum amount of memory resources that will be guaranteed to your server. The limit is the maximum amount of memory resources that will be allocated to your server.</div>`,
                            }).element,
                        ])
                    );
                    
                    let profileSelector = document.getElementById('profile');
                    profileSelector.addEventListener('change', () => {
                        let description = document.querySelector('.profile-description');
                        let profile = profiles.filter(c => c['name'] === profileSelector.value)[0];
                        description.innerHTML = profile['description'];
                        
                        let image = document.querySelector('.profile-image');
                        image.innerHTML = profile['task_template']['container_spec']['image'];
                        
                        let cpu = document.querySelector('.profile-cpu');
                        let cpuLimit = profile['task_template']['resources']['cpu_limit'] ? profile['task_template']['resources']['cpu_limit'] : 'No Limit';
                        let cpuReservation = profile['task_template']['resources']['cpu_reservation'] ? profile['task_template']['resources']['cpu_reservation'] : 'No Reservation';
                        cpu.innerHTML = `${cpuReservation} / ${cpuLimit}`;
                        
                        let memory = document.querySelector('.profile-memory');
                        let memoryLimit = profile['task_template']['resources']['mem_limit'] ? profile['task_template']['resources']['mem_limit'] : 'No Limit';
                        let memoryReservation = profile['task_template']['resources']['mem_reservation'] ? profile['task_template']['resources']['mem_reservation'] : 'No Reservation';
                        memory.innerHTML = `${memoryReservation} / ${memoryLimit}`;
                    });
                    
                },
                buttons: buttons
            });
        });
    }

    let activityTabCallback = XNAT.plugin.jupyterhub.servers.activityTabCallback = function(itemDivId, detailsTag, jsonobj, lastProgressIdx) {
        const succeeded = jsonobj['succeeded'];
        const payload = JSON.parse(jsonobj['payload']);
        let messages = "";
        let entryList = payload ? (payload['entryList'] || []) : [];
        if (entryList.length === 0 && succeeded == null) {
            return [null, lastProgressIdx];
        }
        entryList.forEach(function(e, i) {
            if (i <= lastProgressIdx) {
                return;
            }
            let level = e.status;
            let message = e.message.charAt(0).toUpperCase() + e.message.substr(1);
            let clazz;
            switch (level) {
                case 'Waiting':
                case 'InProgress':
                    clazz = 'info';
                    break;
                case 'Warning':
                    clazz = 'warning';
                    break;
                case 'Failed':
                    clazz = 'error';
                    break;
                case 'Completed':
                    clazz = 'success';
                    break;
            }
            messages += '<div class="prog ' + clazz + '">' + message + '</div>';
            lastProgressIdx = i;
        });
        if (succeeded != null) {
            messages += parseFinalMessage(jsonobj['finalMessage'], succeeded)
        }
        if (messages) {
            $(detailsTag).append(messages);
        }
        
        if (succeeded) {
            XNAT.plugin.jupyterhub.users.getUser(window.username).then(user => {
                let servers = user['servers'] || {};
                let eventTrackingId = jsonobj['key'];
                
                Object.entries(servers).forEach(([severName, server]) => {
                    let user_options = server['user_options'] || {};
                    let serverEventTrackingId = user_options['eventTrackingId'] || "";
                    
                    if (serverEventTrackingId && serverEventTrackingId === eventTrackingId) {
                        let openNbLink = document.getElementById(`open-nb-${eventTrackingId}`);
                        openNbLink.style.display = 'inline';
                        openNbLink.addEventListener('click', () => XNAT.plugin.jupyterhub.servers.goTo(server['url']));
                    }
                });
            })
        }
        
        return {succeeded: succeeded, lastProgressIdx: lastProgressIdx};
    }

    function parseFinalMessage(message, succeeded) {
        if (succeeded) {
            // Surround link to Jupyter server in message with <a> tag
            return '<div class="prog success">' + message.replace(/(\/.*$)/, "<a onclick='XNAT.plugin.jupyterhub.servers.goTo(\"$1\")'>$1</a>") + '</div>';
        } else {
            return '<div class="prog error">' + message + '</div>'
        }
    }

    XNAT.plugin.jupyterhub.servers.goTo = function(server_url) {
        XNAT.plugin.jupyterhub.users.tokens.create().then(token => {
            window.open(`${server_url}?token=${token['token']}`, '_blank');
        }).catch(() => {
            window.open(server_url, '_blank');
        })
    }

    XNAT.plugin.jupyterhub.servers.stopServer = async function(username = window.username,
                                                               servername = '',
                                                               eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.stopServer`);

        return XNAT.xhr.ajax({
            url: serverUrl(username, servername, eventTrackingId),
            data : {"eventTrackingId": eventTrackingId},
            method: 'DELETE',
        });
    }

    XNAT.plugin.jupyterhub.servers.user_options.get = async function(username, servername) {
        let url = servername ?
            `/xapi/jupyterhub/users/${username}/server/${servername}/user-options`:
            `/xapi/jupyterhub/users/${username}/server/user-options`;

        url = XNAT.url.restUrl(url);

        const response = await fetch(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting JupyterHub users: ${response.status}`);
        }

        return await response.json();
    }

    let generateEventTrackingId = XNAT.plugin.jupyterhub.servers.generateEventTrackingId = function() {
        let now = new Date();
        now = now.toISOString()
                 .replaceAll('-','')
                 .replaceAll(':', '')
                 .replaceAll('.', '');

        return now;
    }

}));