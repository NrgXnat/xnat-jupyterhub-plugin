console.debug("Loading compute-spec-configs.js");

var XNAT = getObject(XNAT || {});
XNAT.compute = getObject(XNAT.compute|| {});
XNAT.compute.computeSpecConfigs = getObject(XNAT.compute.computeSpecConfigs || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.compute.computeSpecConfigs.get = async (id) => {
        console.debug("Fetching compute spec config " + id)
        const url = XNAT.url.csrfUrl(`/xapi/compute-spec-configs/${id}`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Error fetching compute spec config ${id}`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.getAll = async (type) => {
        console.debug("Fetching compute spec configs")
        
        const url = type ?
            XNAT.url.csrfUrl(`/xapi/compute-spec-configs?type=${type}`) :
            XNAT.url.csrfUrl(`/xapi/compute-spec-configs`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Error fetching compute spec configs`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.create = async (computeSpec) => {
        console.debug("Creating compute spec config")
        const url = XNAT.url.csrfUrl(`/xapi/compute-spec-configs`);
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(computeSpec)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Error creating compute spec config`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.update = async (computeSpec) => {
        console.debug("Updating compute spec config")
        const id = computeSpec['id'];
        
        if (!id) {
            throw new Error(`Cannot update compute spec config without an ID`);
        }
        
        const url = XNAT.url.csrfUrl(`/xapi/compute-spec-configs/${id}`);
        
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(computeSpec)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Error updating compute spec config ${id}`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.save = async (computeSpec) => {
        console.debug("Saving compute spec config")
        const id = computeSpec['id'];
        
        if (id) {
            return XNAT.compute.computeSpecConfigs.update(computeSpec);
        } else {
            return XNAT.compute.computeSpecConfigs.create(computeSpec);
        }
    }
    
    XNAT.compute.computeSpecConfigs.delete = async (id) => {
        console.debug("Deleting compute spec config " + id)
        const url = XNAT.url.csrfUrl(`/xapi/compute-spec-configs/${id}`);
        
        const response = await fetch(url, {
            method: 'DELETE',
        });
        
        if (!response.ok) {
            throw new Error(`Error deleting compute spec config ${id}`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.available = async (type, user, project) => {
        console.debug(`Fetching available compute spec configs for type ${type} and user ${user} and project ${project}`);
        const url = type ?
            XNAT.url.csrfUrl(`/xapi/compute-spec-configs/available?type=${type}&user=${user}&project=${project}`) :
            XNAT.url.csrfUrl(`/xapi/compute-spec-configs/available?user=${user}&project=${project}`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Error fetching available compute spec configs`);
        }
    }
    
    XNAT.compute.computeSpecConfigs.manager = async (containerId, computeSpecType) => {
        console.debug("Initializing compute spec manager")
        
        let container,
            footer,
            computeSpecConfigs = [],
            hardwareConfigs = [],
            users = [],
            projects = [];
        
        computeSpecType = computeSpecType || '';
        
        const init = () => {
            container = document.getElementById(containerId);
            
            if (!container) {
                throw new Error(`Cannot find container with id ${containerId}`);
            }
            
            clearContainer();
            renderNewButton();
            refreshTable();
            
            getUsers().then(u => users = u.filter(u => u !== 'jupyterhub' && u !== 'guest'));
            getProjects().then(p => projects = p);
            getHardwareConfigs()
        }
        
        const getUsers = async () => {
            let response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users`);
            
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Error fetching users`);
            }
        }
        
        const getProjects = async () => {
            let response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/data/projects`);
            
            if (response.ok) {
                let projects = await response.json();
                projects = projects.ResultSet?.Result || [];
                projects = projects.map(p => p['ID']);
                return projects;
            } else {
                throw new Error(`Error fetching projects`);
            }
        }
        
        const getHardwareConfigs = () => {
            XNAT.compute.hardwareConfigs.getAll().then(h => hardwareConfigs = h);
        }
        
        const clearContainer = () => {
            container.innerHTML = '';
        }
        
        const renderNewButton = () => {
            footer = container.closest('.panel').querySelector('.panel-footer');
            footer.innerHTML = '';
            
            let button = spawn('div', [
                spawn('div.pull-right', [
                    spawn('button.btn.btn-sm.submit', { html: 'New ' + (computeSpecType === 'JUPYTERHUB' ? 'Jupyter Environment' : 'ComputeSpec'), onclick: () => editor(null, 'new') })
                ]),
                spawn('div.clear.clearFix')
            ])
            
            footer.appendChild(button);
        }
        
        const enabledToggle = (config) => {
            let enabled = config['scopes']['Site']['enabled'];
            let ckbox = spawn('input', {
                type: 'checkbox',
                checked: enabled,
                value: enabled ? 'true' : 'false',
                data: {checked: enabled},
                onchange: () => {
                    let enabled = ckbox.checked;
                    config['scopes']['Site']['enabled'] = enabled;
                    
                    XNAT.compute.computeSpecConfigs.update(config).then(() => {
                        XNAT.ui.banner.top(2000, `Compute Spec ${enabled ? 'Enabled' : 'Disabled'}`, 'success');
                    }).catch((err) => {
                        console.error(err);
                        XNAT.ui.banner.top(4000, `Error ${enabled ? 'Enabling' : 'Disabling'} Compute Spec`, 'error');
                        toggleCheckbox(!enabled);
                    });
                }
            });
            
            let toggleCheckbox = (enabled) => {
                ckbox.checked = enabled;
                ckbox.value = enabled ? 'true' : 'false';
                ckbox.dataset.checked = enabled;
            }
            
            return spawn('label.switchbox', [ckbox, ['span.switchbox-outer', [['span.switchbox-inner']]]]);
        }
        
        const displayUsers = (config) => {
            let isAllUsersEnabled = config['scopes']['User']['enabled'];
            let users = config['scopes']['User']['ids'];
            
            if (isAllUsersEnabled) {
                return 'All Users';
            } else {
                if (users.length > 4) {
                    function showUserModal(){
                        XNAT.dialog.message.open({
                            title: 'Enabled Users',
                            content: '<ul><li>' + users.join('</li><li>') + '</li></ul>',
                        });
                    }
                    
                    return spawn('span.show-enabled-users',{
                            style: {
                                'border-bottom': '1px #ccc dashed',
                                'cursor': 'pointer'
                            },
                            data: { 'users': users.join(',') },
                            title: 'Click to view users',
                            onclick: showUserModal
                        },
                        `${users.length} Users Enabled`
                    );
                } else {
                    if (users.length === 0) {
                        return 'No Users Enabled';
                    }
                    
                    return users.sort().join(', ');
                }
            }
        }
        
        const displayProjects = (config) => {
            let isAllProjectsEnabled = config['scopes']['Project']['enabled'];
            let projects = config['scopes']['Project']['ids'];
            
            if (isAllProjectsEnabled) {
                return 'All Projects';
            } else {
                if (projects.length > 4) {
                    function showProjectModal(){
                        XNAT.dialog.message.open({
                            title: 'Enabled Projects',
                            content: '<ul><li>' + projects.join('</li><li>') + '</li></ul>',
                        });
                    }
                    
                    return spawn('span.show-enabled-projects',{
                            style: {
                                'border-bottom': '1px #ccc dashed',
                                'cursor': 'pointer'
                            },
                            data: { 'projects': projects.join(',') },
                            title: 'Click to view projects',
                            onclick: showProjectModal
                        },
                        `${projects.length} Projects Enabled`
                    );
                } else {
                    if (projects.length === 0) {
                        return 'No Projects Enabled';
                    }
                    
                    return projects.sort().join(', ');
                }
            }
        }
        
        const displayHardware = (config) => {
            let isAllHardwareEnabled = config['hardwareOptions']['allowAllHardware'];
            let hardwareConfigs = config['hardwareOptions']['hardwareConfigs'];
            let hardwareConfigNames = hardwareConfigs.map((config) => config['hardware']['name']);
            
            if (isAllHardwareEnabled) {
                return 'All Hardware';
            } else {
                if (hardwareConfigs.length > 4) {
                    function showHardwareModal(){
                        XNAT.dialog.message.open({
                            title: 'Enabled Hardware',
                            content: '<ul><li>' + hardwareConfigNames.join('</li><li>') + '</li></ul>',
                        });
                    }
                    
                    return spawn('span.show-enabled-hardware',{
                            style: {
                                'border-bottom': '1px #ccc dashed',
                                'cursor': 'pointer'
                            },
                            data: { 'hardware': hardwareConfigNames.join(',') },
                            title: 'Click to view hardware',
                            onclick: showHardwareModal
                        },
                        `${hardwareConfigs.length} Hardware Enabled`
                    );
                } else {
                    if (hardwareConfigs.length === 0) {
                        return 'No Hardware Enabled';
                    }
                    
                    return hardwareConfigNames.join(', ');
                }
            }
        }
        
        /**
         * Open the editor for a compute spec config
         * @param config - the compute spec config to edit
         * @param action - the action to perform (new, edit, or copy)
         */
        const editor = (config, action) => {
            console.debug("Opening compute spec config editor")
            let isNew  = action === 'new',
                isCopy = action === 'copy',
                isEdit = action === 'edit',
                title  = isNew || isCopy ? 'New ' : 'Edit ',
                isJupyter = config ? config.configTypes.includes('JUPYTERHUB') : false;
            
            title = title + (isJupyter ? 'Jupyter Environment' : 'ComputeSpec');
                
            
            XNAT.dialog.open({
                title: title,
                content: spawn('div', { id: 'compute-spec-editor' }),
                width: 650,
                maxBtn: true,
                beforeShow: () => {
                    const formEl = document.getElementById('compute-spec-editor');
                    formEl.classList.add('panel');
                    
                    let id = isNew || isCopy ? '' : config.id;
                    let type = computeSpecType;
                    let name = isNew || isCopy ? '' : config.computeSpec?.name || '';
                    let image = isNew ? '' : config.computeSpec?.image;
                    let environmentVariables = isNew ? [] : config.computeSpec?.environmentVariables;
                    let mounts = isNew ? [] : config.computeSpec?.mounts;
                    
                    let siteEnabled = isNew ? true : config.scopes?.Site?.enabled;
                    let allUsersEnabled = isNew ? true : config.scopes?.User?.enabled;
                    let enabledUserIds = isNew ? [] : config.scopes?.User?.ids;
                    let allProjectsEnabled = isNew ? true : config.scopes?.Project?.enabled;
                    let enabledProjectIds = isNew ? [] : config.scopes?.Project?.ids;
                    
                    let allHardwareEnabled = isNew ? true : config.hardwareOptions?.allowAllHardware;
                    let enabledHardwareConfigs = isNew ? [] : config.hardwareOptions?.hardwareConfigs;
                    
                    let idEl = XNAT.ui.panel.input.text({
                        name: 'id',
                        id: 'id',
                        label: 'ID *',
                        description: 'The ID',
                        value: id,
                    });
                    
                    idEl.style.display = 'none';
                    
                    let typeEl = XNAT.ui.panel.input.text({
                        name: 'type',
                        id: 'type',
                        label: 'Type *',
                        description: 'The type (e.g. JupyterHub or Container Service)',
                        value: type,
                    });
                    
                    typeEl.style.display = 'none';
                    
                    let nameEl = XNAT.ui.panel.input.text({
                        name: 'name',
                        id: 'name',
                        label: 'Name *',
                        description: 'User-friendly display name',
                        value: name,
                    });
                    
                    let imageEl = XNAT.ui.panel.input.text({
                        name: 'image',
                        id: 'image',
                        label: 'Image *',
                        description: 'The Docker image to use. This should be the full image name including the tag. ' +
                            'The image must be available on the Docker host where the container will be run.',
                        value: image,
                    });
                    
                    let environmentVariablesHeaderEl = spawn('div.environment-variables', [
                        spawn('p', '<strong>Environment Variables</strong><br> Use this section to define additional ' +
                            'environment variables for the container.'),
                    ]);
                    
                    let addEnvironmentVariableButtonEl = spawn('button.btn.btn-sm.add-environment-variable', {
                        html: 'Add Environment Variable',
                        style: { 'margin-top': '0.75em' },
                        onclick: () => {
                            addEnvironmentVariable();
                        }
                    });
                    
                    let mountsEl = spawn('div.mounts', [
                        spawn('p', '<strong>Mounts</strong><br> Use this section to define additional ' +
                            'bind mounts to mount into the container.'),
                    ]);
                    
                    let addMountButton = spawn('button.btn.btn-sm.add-mount', {
                        html: 'Add Mount',
                        style: { 'margin-top': '0.75em' },
                        onclick: () => {
                            addMount();
                        }
                    });
                    
                    let userAndProjectScopesDescription = spawn('div.user-and-project-scopes', [
                        spawn('p', '<strong>Projects and Users</strong><br> Use this section to define which projects ' +
                            'and users will have access to this.'),
                    ]);
                    
                    let siteEnabledEl = XNAT.ui.panel.input.checkbox({
                        name: 'siteEnabled',
                        id: 'siteEnabled',
                        label: 'Site Enabled',
                        description: 'Enable this ComputeSpec site-wide.',
                        value: siteEnabled,
                    });
                    
                    siteEnabledEl.style.display = 'none';
                    
                    let allUsersEnabledEl = XNAT.ui.panel.input.checkbox({
                        name: 'allUsersEnabled',
                        id: 'allUsersEnabled',
                        label: 'All Users',
                        description: 'Enable for all users',
                        value: allUsersEnabled,
                    });
                    
                    let userOptions = users.map((user) => {
                        return {
                            value: user,
                            label: user,
                            selected: enabledUserIds.includes(user)
                        }
                    })
                    
                    let userSelectEl = XNAT.ui.panel.select.multiple({
                        name: 'userIds',
                        id: 'userIds',
                        label: 'Users',
                        description: 'Select the users who can access this',
                        options: userOptions,
                    });
                    
                    // Disable the user select if all users are enabled
                    allUsersEnabledEl.querySelector('input').addEventListener('change', (event) => {
                        userSelectEl.querySelector('select').disabled = allUsersEnabledEl.querySelector('input').checked;
                    });
                    
                    userSelectEl.querySelector('select').disabled = allUsersEnabledEl.querySelector('input').checked;
                    
                    // The user select is too small by default
                    userSelectEl.querySelector('select').style.minWidth = '200px';
                    userSelectEl.querySelector('select').style.minHeight = '125px';
                    
                    let allProjectsEnabledEl = XNAT.ui.panel.input.checkbox({
                        name: 'allProjectsEnabled',
                        id: 'allProjectsEnabled',
                        label: 'All Projects',
                        description: 'Enable this for all projects',
                        value: allProjectsEnabled,
                    });
                    
                    let projectOptions = projects.map((project) => {
                        return {
                            value: project,
                            label: project,
                            selected: enabledProjectIds.includes(project)
                        }
                    });
                    
                    let projectSelectEl = XNAT.ui.panel.select.multiple({
                        name: 'projectIds',
                        id: 'projectIds',
                        label: 'Projects',
                        description: 'Select the projects which can access this',
                        options: projectOptions,
                    });
                    
                    // Disable the project select if all projects are enabled
                    allProjectsEnabledEl.querySelector('input').addEventListener('change', (event) => {
                        projectSelectEl.querySelector('select').disabled = allProjectsEnabledEl.querySelector('input').checked;
                    });
                    
                    projectSelectEl.querySelector('select').disabled = allProjectsEnabledEl.querySelector('input').checked;
                    
                    // The project select is too small by default
                    projectSelectEl.querySelector('select').style.minWidth = '200px';
                    projectSelectEl.querySelector('select').style.minHeight = '125px';
                    
                    let hardwareScopesDescription = spawn('div.hardware-scopes', [
                        spawn('p', '<strong>Hardware</strong><br> Use this section to define which Hardware ' +
                            'this can be used with.'),
                    ]);
                    
                    let allHardwareEnabledEl = XNAT.ui.panel.input.checkbox({
                        name: 'allHardwareEnabled',
                        id: 'allHardwareEnabled',
                        label: 'All Hardware',
                        description: 'Allow this to be used with any Hardware.',
                        value: allHardwareEnabled,
                    });
                    
                    let hardwareConfigsEl = XNAT.ui.panel.select.multiple({
                        name: 'hardwareConfigs',
                        id: 'hardwareConfigs',
                        label: 'Hardware Configs',
                        description: 'Select the Hardware that can be used with this',
                        options: hardwareConfigs.map((hardwareConfig) => {
                            return {
                                value: hardwareConfig.id,
                                label: hardwareConfig.hardware?.name,
                                selected: enabledHardwareConfigs.map((enabledHardwareConfig) => enabledHardwareConfig.id).includes(hardwareConfig.id),
                            }
                        }),
                    });
                    
                    // Disable the hardware configs select if all hardware is enabled
                    allHardwareEnabledEl.querySelector('input').addEventListener('change', (event) => {
                        hardwareConfigsEl.querySelector('select').disabled = allHardwareEnabledEl.querySelector('input').checked;
                    });
                    
                    hardwareConfigsEl.querySelector('select').disabled = allHardwareEnabledEl.querySelector('input').checked;
                    
                    // The hardware configs select is too small by default
                    hardwareConfigsEl.querySelector('select').style.minWidth = '200px';
                    hardwareConfigsEl.querySelector('select').style.minHeight = '100px';
                    
                    formEl.appendChild(spawn('!', [
                        idEl,
                        typeEl,
                        nameEl,
                        imageEl,
                        spawn('hr'),
                        environmentVariablesHeaderEl,
                        addEnvironmentVariableButtonEl,
                        spawn('hr'),
                        mountsEl,
                        addMountButton,
                        spawn('hr'),
                        hardwareScopesDescription,
                        allHardwareEnabledEl,
                        hardwareConfigsEl,
                        spawn('hr'),
                        userAndProjectScopesDescription,
                        siteEnabledEl,
                        allProjectsEnabledEl,
                        projectSelectEl,
                        allUsersEnabledEl,
                        userSelectEl,
                    ]));
                    
                    // Add the environment variables
                    environmentVariables.forEach((environmentVariable) => {
                        addEnvironmentVariable(environmentVariable);
                    });
                    
                    // Add the mounts
                    mounts.forEach((mount) => {
                        addMount(mount);
                    });
                },
                buttons: [
                    {
                        label: 'Cancel',
                        isDefault: false,
                        close: true
                    },
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function() {
                            const formEl = document.getElementById('compute-spec-editor');
                            
                            const idEl = formEl.querySelector('#id');
                            const typeEl = formEl.querySelector('#type');
                            const nameEl = formEl.querySelector('#name');
                            const imageEl = formEl.querySelector('#image');
                            const siteEnabledEl = formEl.querySelector('#siteEnabled');
                            const allUsersEnabledEl = formEl.querySelector('#allUsersEnabled');
                            const userIdsEl = formEl.querySelector('#userIds');
                            const allProjectsEnabledEl = formEl.querySelector('#allProjectsEnabled');
                            const projectIdsEl = formEl.querySelector('#projectIds');
                            const allHardwareEnabledEl = formEl.querySelector('#allHardwareEnabled');
                            const hardwareConfigsEl = formEl.querySelector('#hardwareConfigs');
                            
                            const validators = [];
                            
                            let validateNameEl = XNAT.validate(nameEl).reset().chain();
                            validateNameEl.is('notEmpty').failure('Name is required');
                            validators.push(validateNameEl);
                            
                            let validateImageEl = XNAT.validate(imageEl).reset().chain();
                            validateImageEl.is('notEmpty').failure('Image is required');
                            validators.push(validateImageEl);
                            // TODO: Validate image format
                            
                            let envVarsPresent = document.querySelectorAll('input.key').length > 0;
                            if (envVarsPresent) {
                                let validateEnvironmentVariableKeys = XNAT.validate('input.key').chain();
                                validateEnvironmentVariableKeys.is('notEmpty').failure('Keys are required')
                                                               .is('regex', /^[a-zA-Z_][a-zA-Z0-9_]*$/).failure('Keys must be valid environment variable names');
                                validators.push(validateEnvironmentVariableKeys);
                            }
                            
                            let mountsPresent = document.querySelectorAll('.mount-group').length > 0;
                            if (mountsPresent) {
                                let validateLocalPaths = XNAT.validate('.mount-group .local-path').chain();
                                validateLocalPaths.is('notEmpty').failure('Local paths are required')
                                                  .is('uri').failure('Local paths must be a valid URI');
                                validators.push(validateLocalPaths);
                                
                                let validateContainerPaths = XNAT.validate('.mount-group .container-path').chain();
                                validateContainerPaths.is('notEmpty').failure('Container paths are required')
                                                      .is('uri').failure('Container paths must be a valid URI');
                                validators.push(validateContainerPaths);
                            }

                            // validate fields
                            let errorMessages = [];
                            
                            validators.forEach((validator) => {
                                if (!validator.check()) {
                                    validator.messages.forEach(message => errorMessages.push(message));
                                }
                            });
                            
                            if (errorMessages.length > 0) {
                                XNAT.dialog.open({
                                    title: 'Error',
                                    width: 400,
                                    content: '<ul><li>' + errorMessages.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }
                            
                            config = {
                                id: idEl.value,
                                configTypes: [typeEl.value],
                                computeSpec: {
                                    name: nameEl.value,
                                    image: imageEl.value,
                                    environmentVariables: getEnvironmentVariables(),
                                    mounts: getMounts(),
                                },
                                scopes: {
                                    Site: {
                                        scope: 'Site',
                                        enabled: siteEnabledEl.checked,
                                    },
                                    Project: {
                                        scope: 'Project',
                                        enabled: allProjectsEnabledEl.checked,
                                        ids: Array.from(projectIdsEl.selectedOptions).map(option => option.value),
                                    },
                                    User: {
                                        scope: 'User',
                                        enabled: allUsersEnabledEl.checked,
                                        ids: Array.from(userIdsEl.selectedOptions).map(option => option.value),
                                    }
                                },
                                hardwareOptions: {
                                    allowAllHardware: allHardwareEnabledEl.checked,
                                    hardwareConfigs: Array.from(hardwareConfigsEl.selectedOptions).map(option => {
                                        return {
                                            id: option.value,
                                        }
                                    }),
                                }
                            }
                            
                            XNAT.compute.computeSpecConfigs.save(config)
                                .then(() => {
                                    refreshTable();
                                    XNAT.dialog.closeAll();
                                    XNAT.ui.banner.top(2000, 'Saved', 'success');
                                })
                                .catch((err) => {
                                    console.error(err);
                                    XNAT.ui.banner.top(2000, 'Error Saving', 'error');
                                });
                        }
                    }
                ]
            });
        }
        
        const refreshTable = () => {
            XNAT.compute.computeSpecConfigs.getAll(computeSpecType)
                .then((data) => {
                    computeSpecConfigs = data;
                    
                    if (computeSpecConfigs.length === 0) {
                        clearContainer()
                        container.innerHTML = `<p>No Jupyter environments found</p>`
                        return;
                    }
                    
                    table();
                })
                .catch((err) => {
                    console.error(err);
                    clearContainer();
                    container.innerHTML = `<p>Error loading Jupyter environments</p>`
                });
        }
        
        const deleteConfig = (id) => {
            XNAT.compute.computeSpecConfigs.delete(id)
                .then(() => {
                    XNAT.ui.banner.top(2000, 'Delete successful', 'success');
                    refreshTable();
                })
                .catch((err) => {
                    XNAT.ui.banner.top(4000, 'Error deleting', 'error');
                    console.error(err);
                });
        }
        
        const addEnvironmentVariable = (envVar) => {
            const formEl = document.getElementById('compute-spec-editor');
            const environmentVariablesEl = formEl.querySelector('div.environment-variables');
            
            const keyEl = spawn('input.form-control.key', {
                id: 'key',
                placeholder: 'Key',
                type: 'text',
                value: envVar ? envVar.key : '',
            });
            
            const equalsEl = spawn('span.input-group-addon', {
                style: {
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                }
            }, ['=']);
            
            const valueEl = spawn('input.form-control.value', {
                id: 'value',
                placeholder: 'Value',
                type: 'text',
                value: envVar ? envVar.value : '',
            });
            
            const removeButtonEl = spawn('button.btn.btn-danger', {
                type: 'button',
                title: 'Remove',
                onclick: () => {
                    environmentVariableEl.remove();
                }
            }, [
                spawn('i.fa.fa-trash'),
            ]);
            
            const environmentVariableEl = spawn('div.form-group', {
                style: {
                    marginBottom: '5px',
                }
            }, [
                spawn('div.input-group', {
                    style: {
                        display: 'flex',
                        flexDirection: 'row',
                        justifyContent: 'center',
                        columnGap: '10px',
                    }
                }, [
                    keyEl,
                    equalsEl,
                    valueEl,
                    spawn('span.input-group-btn', [
                        removeButtonEl,
                    ]),
                ]),
            ]);
            
            environmentVariablesEl.appendChild(environmentVariableEl);
        }
        
        const getEnvironmentVariables = () => {
            const formEl = document.getElementById('compute-spec-editor');
            const environmentVariablesEl = formEl.querySelector('div.environment-variables');
            
            let environmentVariables = [];
            
            Array.from(environmentVariablesEl.children).forEach((environmentVariableEl) => {
                const keyEl = environmentVariableEl.querySelector('#key');
                const valueEl = environmentVariableEl.querySelector('#value');
                
                if (keyEl === null || valueEl === null) return;
                
                environmentVariables.push({
                    key: keyEl.value,
                    value: valueEl.value,
                });
            });
            
            return environmentVariables;
        }
        
        const addMount = (mount) => {
            const formEl = document.getElementById('compute-spec-editor');
            const mountsEl = formEl.querySelector('div.mounts');
            
            let removeButton = spawn('a.close', {
                html: '<i class="fa fa-close" title="Remove Mount" style="float: right;"></i>',
                onclick: () => {
                    mountGroup.remove();
                }
            })
            
            let localPath = XNAT.ui.panel.input.text({
                name: 'localPath',
                label: 'Local Path',
                classes: 'required local-path',
                description: 'The local path on the host to mount to the container',
                value: mount ? mount.localPath : '',
            });
            
            let containerPath = XNAT.ui.panel.input.text({
                name: 'containerPath',
                label: 'Container Path',
                classes: 'required container-path',
                description: 'The path to mount the local path to in the container',
                value: mount ? mount.containerPath : '',
            });
            
            let readOnly = XNAT.ui.panel.input.checkbox({
                name: 'readOnly',
                label: 'Read Only',
                classes: 'required read-only',
                description: 'Whether or not the mount should be read only',
                value: mount ? mount.readOnly : true,
            });
            
            let mountGroup = spawn('div.mount-group', {
                style: {
                    border: '1px solid #ccc',
                    padding: '5px',
                    margin: '5px',
                    borderRadius: '10px',
                }
            }, [
                removeButton,
                localPath,
                containerPath,
                readOnly,
            ]);
            
            mountsEl.appendChild(mountGroup);
        }
        
        const getMounts = () => {
            const formEl = document.getElementById('compute-spec-editor');
            const mountsEl = formEl.querySelector('div.mounts');
            
            let mounts = [];
            
            Array.from(mountsEl.children).forEach((mountGroup) => {
                const localPathEl = mountGroup.querySelector('.local-path');
                const containerPathEl = mountGroup.querySelector('.container-path');
                const readOnlyEl = mountGroup.querySelector('.read-only');
                
                if (localPathEl === null || containerPathEl === null || readOnlyEl === null) return;
                
                mounts.push({
                    volumeName: '',
                    localPath: localPathEl.value,
                    containerPath: containerPathEl.value,
                    readOnly: readOnlyEl.checked,
                });
            });
            
            return mounts;
        }
        
        const table = () => {
            const computeSpecTable = XNAT.table.dataTable(computeSpecConfigs, {
                header: true,
                sortable: 'name',
                columns: {
                    name: {
                        label: 'Name',
                        filter: true,
                        td: { className: 'word-wrapped align-top' },
                        apply: function () {
                            return this['computeSpec']['name'];
                        }
                    },
                    image: {
                        label: 'Image',
                        filter: true,
                        apply: function () {
                            return this['computeSpec']['image'];
                        },
                    },
                    hardware: {
                        label: 'Hardware',
                        td: { className: 'hardware word-wrapped align-top' },
                        filter: true,
                        apply: function () {
                            return displayHardware(this);
                        },
                    },
                    projects: {
                        label: 'Project(s)',
                        filter: true,
                        td: { className: 'projects word-wrapped align-top' },
                        apply: function () {
                            return displayProjects(this);
                        },
                    },
                    users: {
                        label: 'User(s)',
                        filter: true,
                        td: { className: 'users word-wrapped align-top' },
                        apply: function () {
                            return displayUsers(this);
                        },
                    },
                    enabled: {
                        label: 'Enabled',
                        apply: function () {
                            return spawn('div.center', [enabledToggle(this)]);
                        },
                    },
                    actions: {
                        label: 'Actions',
                        th: { style: { width: '150px' } },
                        apply: function () {
                            return spawn('div.center', [
                                spawn('button.btn.btn-sm', { onclick: () => editor(this, 'edit') }, '<i class="fa fa-pencil" title="Edit"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => editor(this, 'copy') }, '<i class="fa fa-clone" title="Duplicate"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => deleteConfig(this.id) }, '<i class="fa fa-trash" title="Delete"></i>')
                            ]);
                        },
                    }
                },
            })
            
            clearContainer();
            computeSpecTable.render(`#${containerId}`);
        }
        
        init();
        
        return {
            container: container,
            computeSpecConfigs: computeSpecConfigs,
            refresh: refreshTable
        };
    }
    
}));
