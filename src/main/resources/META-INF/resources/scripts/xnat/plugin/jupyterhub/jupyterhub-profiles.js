// noinspection RegExpRedundantEscape

/*!
 * JupyterHub Profiles
 */

console.debug('jupyterhub-profiles.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.profiles = getObject(XNAT.plugin.jupyterhub.profiles || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.plugin.jupyterhub.profiles.empty = () => {
        return {
            "enabled": true,
            "id": null,
            "name": "",
            "description": "",
            "projects": [],
            "spawner": "dockerspawner.SwarmSpawner",
            "task_template": {
                "container_spec": {
                    "env": {},
                    "image": "",
                    "mounts": []
                },
                "placement": {
                    "constraints": []
                },
                "resources": {
                    "cpu_limit": null,
                    "cpu_reservation": null,
                    "mem_limit": null,
                    "mem_reservation": null,
                    "generic_resources": {}
                }
            }
        }
    };
    
    XNAT.plugin.jupyterhub.profiles.get = async (id) => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.get(${id})`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles/${id}`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error fetching profile');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.getAll = async () => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.getAll()`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error fetching profiles');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.create = async (profile) => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.create`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles`);
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(profile)
        });
        
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('Error creating profile');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.update = async (profile) => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.update`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles/${profile.id}`);
        
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(profile)
        });
        
        if (!response.ok) {
            throw new Error('Error updating profile');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.delete = async (id) => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.delete(${id})`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles/${id}`);
        
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Error deleting profile');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.getForProject = async (projectId) => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.getForProject(${projectId})`);
        
        const url = XNAT.url.csrfUrl(`/xapi/jupyterhub/profiles/projects/${projectId}`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error getting profiles for project');
        }
    }
    
    XNAT.plugin.jupyterhub.profiles.manager = async (containerId = 'jupyterhub-profiles') => {
        console.debug(`jupyterhub-profiles.js - XNAT.plugin.jupyterhub.profiles.manager`);
        
        const containerEl = document.getElementById(containerId);
        const footerEl = containerEl.parentElement.parentElement.querySelector('.panel-footer');
        
        const clearContainer = () => containerEl.innerHTML = '';
        
        const editButton = (profile) => {
            return spawn('button.btn.sm', {
                title: 'Edit',
                onclick: function (e) {
                    e.preventDefault();
                    editor(profile);
                }
            }, [spawn('span.fa.fa-pencil')]);
        }
        
        const deleteButton = (profile) => {
            return spawn('button.btn.sm.delete', {
                title: 'Delete',
                onclick: function (e) {
                    e.preventDefault();
                    xmodal.confirm({
                        title: 'Delete Profile',
                        height: 200,
                        width: 600,
                        scroll: false,
                        content: "" +
                            "<p>Are you sure you want to delete the profile <b>" + profile.name + "</b>?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function () {
                            XNAT.plugin.jupyterhub.profiles.delete(profile.id)
                                .then(() => {
                                    refreshTable();
                                    XNAT.ui.banner.top(2000, 'Profile deleted', 'success');
                                })
                                .catch((err) => {
                                    console.error(err);
                                    XNAT.ui.banner.top(2000, 'Error deleting profile', 'error');
                                });
                        }
                    })
                }
            }, [spawn('i.fa.fa-trash')]);
        }
    
        const enabledToggle = (profile) => {
            let enabled = profile['enabled'];
            let ckbox = spawn('input', {
                type: 'checkbox',
                checked: enabled,
                value: enabled ? 'true' : 'false',
                data: {checked: enabled},
                onchange: () => {
                    profile['enabled'] = !enabled;
                    
                    XNAT.plugin.jupyterhub.profiles.update(profile).then(() => {
                        XNAT.ui.banner.top(2000, 'Profile ' + (enabled ? 'disabled' : 'enabled'), 'success');
                    }).catch((err) => {
                        console.error(err);
                        XNAT.ui.banner.top(2000, 'Error updating profile', 'error');
                    });
                }
            });
    
            return spawn('label.switchbox', [ckbox, ['span.switchbox-outer', [['span.switchbox-inner']]]]);
        }
        
        const spacer = (width) => {
            return spawn('i.spacer', {
                style: {
                    display: 'inline-block',
                    width: width + 'px'
                }
            })
        }
        
        const refreshTable = async () => {
            const profiles = await XNAT.plugin.jupyterhub.profiles.getAll();
            
            if (profiles.length === 0) {
                clearContainer();
                containerEl.innerHTML = '<p>No profiles found. Click the "New Profile" button to create one.</p>';
                return;
            }
            
            const profilesTable = XNAT.ui.panel.dataTable({
                name: 'profile-table',
                data: profiles,
                items: {
                    id: '~!',
                    name: {
                        label: ' Profile Name',
                        sort: true
                    },
                    image: {
                        label: 'Docker Image',
                        apply: function () {
                            return spawn('div.center', this['task_template']['container_spec']['image'])
                        }
                    },
                    cpu: {
                        label: 'CPU Res / Lim',
                        apply: function () {
                            let cpuReservation =
                                    this.task_template &&
                                    this.task_template.resources &&
                                    this.task_template.resources.cpu_reservation || '-';
                            let cpuLimit =
                                    this.task_template &&
                                    this.task_template.resources &&
                                    this.task_template.resources.cpu_limit || '-';
                            let cpu = cpuReservation + ' / ' + cpuLimit;
                            return spawn('div.center', cpu);
                        }
                    },
                    memory: {
                        label: 'Memory Res / Lim',
                        apply: function () {
                            let memReservation =
                                    this.task_template &&
                                    this.task_template.resources &&
                                    this.task_template.resources.mem_reservation || '-';
                            let memLimit =
                                    this.task_template &&
                                    this.task_template.resources &&
                                    this.task_template.resources.mem_limit || '-';
                            let mem = memReservation + ' / ' + memLimit;
                            return spawn('div.center', mem);
                        }
                    },
                    enables: {
                        label: 'Enabled',
                        apply: function () {
                            return spawn('div.center', [enabledToggle(this)]);
                        }
                    },
                    actions: {
                        label: 'Actions',
                        apply: function () {
                            return spawn('div.center', [
                                editButton(this),
                                spacer(4),
                                deleteButton(this)
                            ]);
                        }
                    }
                },
            })
            
            // Clear the container and add the table
            clearContainer();
            containerEl.append(profilesTable.element);
            const elementWrapper = profilesTable.element.querySelector('.element-wrapper')
            if (elementWrapper) {
                // Make the table full width, the default is too narrow
                elementWrapper.style.width = '100%';
            }
        }
        
        const editor = (profile) => {
            const doWhat = profile ? 'Edit' : 'Create';
            const isNew = !profile;
            profile = profile || XNAT.plugin.jupyterhub.profiles.empty();
            
            const editorDialogId = 'editor-dialog';
            
            XNAT.dialog.open({
                title: `${doWhat} JupyterHub Profile`,
                content: spawn(`form#${editorDialogId}`),
                maxBtn: true,
                width: 600,
                beforeShow: () => {
                    const formEl = document.getElementById(editorDialogId);
                    formEl.classList.add('panel');
                    
                    let profileName = isNew ? '' : profile.name || '';
                    let profileDescription = isNew ? '' : profile.description || '';
                    let profileImage = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.container_spec &&
                        profile.task_template.container_spec.image || '';
                    let profileCpuReservation = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.resources &&
                        profile.task_template.resources.cpu_reservation || '';
                    let profileCpuLimit = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.resources &&
                        profile.task_template.resources.cpu_limit || '';
                    let profileMemoryReservation = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.resources &&
                        profile.task_template.resources.mem_reservation || '';
                    let profileMemoryLimit = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.resources &&
                        profile.task_template.resources.mem_limit || '';
                    let profilePlacementConstraints = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.placement &&
                        profile.task_template.placement.constraints ?
                        profile.task_template.placement.constraints.join(',\r\n') : '';
                    let profileGenericResources = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.resources &&
                        profile.task_template.resources.generic_resources ?
                            Object.entries(profile.task_template.resources.generic_resources).map(([k, v]) => `${k}=${v}`).join(',\r\n') : '';
                    let profileEnv = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.container_spec &&
                        profile.task_template.container_spec.env ?
                            Object.entries(profile.task_template.container_spec.env).map(([k, v]) => `${k}=${v}`).join(',\r\n') : '';
                    let profileMounts = isNew ? '' :
                        profile.task_template &&
                        profile.task_template.container_spec &&
                        profile.task_template.container_spec.mounts ?
                            profile.task_template.container_spec.mounts.map(m => `${m['source']}:${m['target']}:${m['read_only'] ? 'ro' : 'rw'}`).join(',\r\n') : '';
                    
                    formEl.append(spawn('!', [
                        XNAT.ui.panel.input.text({
                            name: 'name',
                            id: 'name',
                            label: 'Name *',
                            description: 'The name of this profile. This will be displayed to users when they select this configuration.',
                            value: profileName,
                        }).element,
                        XNAT.ui.panel.input.textarea({
                            name: 'description',
                            id: 'description',
                            label: 'Description *',
                            description: 'A description of this profile. This will be displayed to users when they select this configuration.',
                            value: profileDescription,
                            rows: 5,
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'image',
                            id: 'image',
                            label: 'Docker Image *',
                            description: 'The Docker image to use for this profile. This should be the full image name, including the tag. Example: jupyter/scipy-notebook:hub-version',
                            value: profileImage,
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'cpu-reservation',
                            id: 'cpu-reservation',
                            label: 'CPU Reservation',
                            description: 'The number of CPUs that this container will reserve. Fractional values  (e.g. 0.5) are allowed.',
                            value: profileCpuReservation,
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'cpu-limit',
                            id: 'cpu-limit',
                            label: 'CPU Limit',
                            description: 'The maximum number of CPUs that this container can use. Fractional values (e.g. 3.5) are allowed.',
                            value: profileCpuLimit,
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'memory-reservation',
                            id: 'memory-reservation',
                            label: 'Memory Reservation',
                            description: 'The amount of memory that this container will reserve. Allows for suffixes like "2G" or "256M".',
                            value: profileMemoryReservation,
                        }).element,
                        XNAT.ui.panel.input.text({
                            name: 'memory-limit',
                            id: 'memory-limit',
                            label: 'Memory Limit',
                            description: 'The maximum amount of memory that this container can use. Allows for suffixes like "4G" or "512M".',
                            value: profileMemoryLimit,
                        }).element,
                        XNAT.ui.panel.input.textarea({
                            name: 'placement-constraints',
                            id: 'placement-constraints',
                            label: 'Placement Constraints',
                            description: 'Enter a list of placement constraints in the form of comma-separated strings. For example: node.role==worker, node.labels.type==gpu',
                            value: profilePlacementConstraints,
                            rows: 5,
                        }).element,
                        XNAT.ui.panel.input.textarea({
                            name: 'generic-resources',
                            id: 'generic-resources',
                            label: 'Generic Resources',
                            description: 'Enter a list of generic resources in the form of comma-separated strings. For example: GPU=2, FPGA=1',
                            value: profileGenericResources,
                            rows: 5,
                        }).element,
                        XNAT.ui.panel.input.textarea({
                            name: 'environment-variables',
                            id: 'environment-variables',
                            label: 'Environment Variables',
                            description: 'Enter a list of environment variables in the form of comma-separated strings. For example: MY_ENV_VAR=foo, ANOTHER_ENV_VAR=bar',
                            // join with newlines and carriage returns to make it easier to edit
                            value: profileEnv,
                            rows: 5,
                        }).element,
                        XNAT.ui.panel.input.textarea({
                            name: 'mounts',
                            id: 'mounts',
                            label: 'Bind Mounts',
                            description: 'Enter a list of additional bind mounts in the form of comma-separated strings. For example: /tools:/tools:ro, /home:/home:rw',
                            // join with newlines and carriage returns to make it easier to edit
                            value: profileMounts,
                            rows: 5,
                        }).element,
                    ]))
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function () {
                            const formEl = document.getElementById(editorDialogId);
                            
                            const nameEl = formEl.querySelector('#name');
                            const descriptionEl = formEl.querySelector('#description');
                            const imageEl = formEl.querySelector('#image');
                            const cpuLimitEl = formEl.querySelector('#cpu-limit');
                            const cpuReservationEl = formEl.querySelector('#cpu-reservation');
                            const memoryLimitEl = formEl.querySelector('#memory-limit');
                            const memoryReservationEl = formEl.querySelector('#memory-reservation');
                            const placementConstraintsEl = formEl.querySelector('#placement-constraints');
                            const genericResourcesEl = formEl.querySelector('#generic-resources');
                            const environmentVariablesEl = formEl.querySelector('#environment-variables');
                            const mountsEl = formEl.querySelector('#mounts');
                            
                            let validateNameEl = XNAT.validate(nameEl).reset().chain();
                            validateNameEl.is('notEmpty').failure('Name is required');
                            
                            let validateDescriptionEl = XNAT.validate(descriptionEl).reset().chain();
                            validateDescriptionEl.is('notEmpty').failure('Description is required');
                            
                            let validateImageEl = XNAT.validate(imageEl).reset().chain();
                            validateImageEl.is('notEmpty').failure('Image is required');
                            // must be in the format of image:tag
                            validateImageEl.is('regex', /^.+:.+$/).failure('Image must be in the format of image:tag');
                            
                            let validateCpuLimitEl = XNAT.validate(cpuLimitEl).reset().chain();
                            validateCpuLimitEl.is('allow-empty')
                                              .is('decimal')
                                              .is('greater-than', 0)
                                              .failure('CPU Limit must be a number greater than 0 or be empty');
                            
                            let validateCpuReservationEl = XNAT.validate(cpuReservationEl).reset().chain();
                            validateCpuReservationEl.is('allow-empty')
                                                    .is('decimal')
                                                    .is('greater-than', 0)
                                                    .failure('CPU Reservation must be a number greater than 0 or be empty');
    
                            let validateMemoryLimitEl = XNAT.validate(memoryLimitEl).reset().chain();
                            validateMemoryLimitEl.is('allow-empty')
                                                 .is('regex', /^$|^([1-9]+[0-9]*)+[KMGT]$/) // 512M, 2G, etc.
                                                 .failure('Memory Limit must be a number followed by a suffix of K, M, G, or T or be empty');
                            
                            let validateMemoryReservationEl = XNAT.validate(memoryReservationEl).reset().chain();
                            validateMemoryReservationEl.is('allow-empty')
                                                       .is('regex', /^$|^([1-9]+[0-9]*)+[KMGT]$/) // 512M, 2G, etc.
                                                       .failure('Memory Reservation must be a number followed by a suffix of K, M, G, or T or be empty');
                            
                            let validatePlacementConstraintsEl = XNAT.validate(placementConstraintsEl).reset().chain();
                            validatePlacementConstraintsEl.is('allow-empty')
                                                          .is('regex', /^$|^([A-Za-z0-9_\.]+(==|!=)[A-Za-z0-9_\.]+,?\s*)+$/) // node.role == worker, node.labels.type == gpu
                                                          .failure('Placement Constraints must be in the form of KEY==VALUE, KEY!=VALUE or be empty');
                            
                            let validateGenericResourcesEl = XNAT.validate(genericResourcesEl).reset().chain();
                            validateGenericResourcesEl.is('allow-empty')
                                                      .is('regex', /^$|^([A-Za-z_][A-Za-z0-9_]*=[^,]+,?\s*)+$/)
                                                      .failure('Generic Resources must be in the form of KEY=VALUE or be empty');
                            
                            let validateEnvironmentVariablesEl = XNAT.validate(environmentVariablesEl).reset().chain();
                            validateEnvironmentVariablesEl.is('allow-empty')
                                                          .is('regex', /^$|^([A-Za-z_][A-Za-z0-9_]*=[^,]+,?\s*)+$/) // MY_ENV_VAR=foo, ANOTHER_ENV_VAR=bar
                                                          .failure('Environment Variables must be in the form of KEY=VALUE or be empty');
                            
                            let validateMountsEl = XNAT.validate(mountsEl).reset().chain();
                            validateMountsEl.is('allow-empty')
                                            .is('regex', /^$|^\/([A-Za-z0-9_\/-]+:\/[A-Za-z0-9_\/-]+:(ro|rw),?\s*)+$/) // /tools:/tools:ro, /home:/home:rw
                                            .failure('Mounts must be in the form of /source:/target:(ro|rw) or be empty');
                            
                            // validate fields
                            let errorMessages = [];
                            let validators = [validateNameEl, validateImageEl, validateDescriptionEl, validateCpuLimitEl,
                                validateCpuReservationEl, validateMemoryLimitEl, validateMemoryReservationEl,
                                validatePlacementConstraintsEl, validateGenericResourcesEl,
                                validateEnvironmentVariablesEl, validateMountsEl];
                            
                            validators.forEach(validator => {
                                if (!validator.check()) {
                                    validator.messages.forEach(message => errorMessages.push(message));
                                }
                            })
    
                            if (errorMessages.length) {
                                // errors?
                                XNAT.dialog.open({
                                    title: 'Validation Error',
                                    width: 500,
                                    content: '<ul><li>' + errorMessages.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }
                            
                            profile['name'] = nameEl.value;
                            profile['description'] = descriptionEl.value;
                            profile['task_template']['container_spec']['image'] = imageEl.value;
                            
                            profile['task_template']['resources']['cpu_limit'] = cpuLimitEl.value;
                            profile['task_template']['resources']['cpu_reservation'] = cpuReservationEl.value;
                            profile['task_template']['resources']['mem_limit'] = memoryLimitEl.value;
                            profile['task_template']['resources']['mem_reservation'] = memoryReservationEl.value;
                            profile['task_template']['placement']['constraints'] = placementConstraintsEl.value ? placementConstraintsEl.value.split(',').map(s => s.trim()).filter(s => s) : [];
                            profile['task_template']['resources']['generic_resources'] = genericResourcesEl.value ?  Object.fromEntries(new Map(genericResourcesEl.value.split(',').map(s => s.trim().split('=')))) : {};
                            profile['task_template']['container_spec']['env'] = environmentVariablesEl.value ? Object.fromEntries(new Map(environmentVariablesEl.value.split(',').map(s => s.trim().split('=')))) : {};
                            
                            if (mountsEl.value) {
                                profile['task_template']['container_spec']['mounts'] = mountsEl.value.split(',').map(s => {
                                    const [source, target, readonly] = s.trim().split(':');
                                    return {
                                        'source': source,
                                        'target': target,
                                        'read_only': readonly === 'ro',
                                        'type': 'bind'
                                    }
                                });
                            } else {
                                profile['task_template']['container_spec']['mounts'] = [];
                            }
                            
                            if (isNew) {
                                XNAT.plugin.jupyterhub.profiles.create(profile)
                                    .then(() => {
                                        refreshTable();
                                        XNAT.dialog.closeAll();
                                        XNAT.ui.banner.top(2000, 'Jupyter profile created', 'success');
                                    })
                                    .catch((err) => {
                                        console.error(err);
                                        XNAT.ui.banner.top(2000, 'Error creating profile', 'error');
                                    });
                            } else {
                                XNAT.plugin.jupyterhub.profiles.update(profile)
                                    .then(() => {
                                        refreshTable();
                                        XNAT.dialog.closeAll();
                                        XNAT.ui.banner.top(2000, 'Jupyter profile updated', 'success');
                                    })
                                    .catch((err) => {
                                        console.error(err);
                                        XNAT.ui.banner.top(2000, 'Error updating profile', 'error');
                                    });
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
        
        const newProfileButton = spawn('button.new-profile.btn.btn-sm.submit', {
            html: 'New Profile',
            onclick: () => {
                editor();
            }
        })
        
        footerEl.append(spawn('div.pull-right', [newProfileButton]));
        footerEl.append(spawn('div.clear.clearfix'));
        
        return refreshTable().then(() => {
            return {
                refreshTable: refreshTable
            }
        });
    }
    
}));