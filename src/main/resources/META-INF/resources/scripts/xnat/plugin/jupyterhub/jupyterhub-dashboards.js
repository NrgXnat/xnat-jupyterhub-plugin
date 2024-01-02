/**
 * JupyterHub Dashboard Functions
 */
console.debug('Loading jupyterhub-dashboards.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.dashboards = getObject(XNAT.plugin.jupyterhub.dashboards || {});
XNAT.plugin.jupyterhub.dashboards.configs = getObject(XNAT.plugin.jupyterhub.dashboards.configs || {});
XNAT.plugin.jupyterhub.dashboards.dataTypes = getObject(XNAT.plugin.jupyterhub.dashboards.dataTypes || {});
XNAT.plugin.jupyterhub.dashboards.frameworks = getObject(XNAT.plugin.jupyterhub.dashboards.frameworks || {});

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

    XNAT.plugin.jupyterhub.dashboards.dataTypes = {
        getAll: async function () {
            return new Promise((resolve, reject) => XNAT.app.dataTypeAccess.getElements.createable.ready(resolve, reject));
        },
        getSome: async function () {
            // Get only the data types that can be used to create dashboards
            return this.getAll().then((dataTypes) => {
                return dataTypes.elements.filter((dataType) => {
                    return dataType['experiment'] === true ||
                           dataType['elementName'] === "xnat:subjectData" ||
                           dataType['elementName'] === "xnat:projectData"
                });
            });
        }
    }

    XNAT.plugin.jupyterhub.dashboards.configs = {
        url: `/xapi/jupyterhub/dashboards/configs`,
        get: async function (id) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to get dashboard config with id ${id}`);
            }
        },
        getAll: async function () {
            const url = XNAT.url.csrfUrl(`${this.url}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (response.ok) {
                const dashboardConfigs = await response.json();
                dashboardConfigs.sort((a, b) => a.dashboard.name.localeCompare(b.dashboard.name));
                return dashboardConfigs;
            } else {
                throw new Error(`Failed to get dashboard configs`);
            }
        },
        create: async function (dashboardConfig) {
            const url = XNAT.url.csrfUrl(`${this.url}`);

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(dashboardConfig)
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to create dashboard config`);
            }
        },
        update: async function (dashboardConfig) {
            const url = XNAT.url.csrfUrl(`${this.url}/${dashboardConfig.id}`);

            const response = await fetch(url, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(dashboardConfig)
            });

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to update dashboard config`);
            }
        },
        delete: async function (id) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}`);

            const response = await fetch(url, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to delete dashboard config with id ${id}`);
            }
        },
        available: async function (executionScope) {
            console.debug(`Getting available dashboard configs for execution scope ${executionScope}`);

            let url = XNAT.url.csrfUrl(`${this.url}/available?`);

            // add each scope to the url as a query parameter
            for (const scope in executionScope) {
                if (executionScope.hasOwnProperty(scope)) {
                    const value = executionScope[scope];
                    if (value) {
                        url += `&${scope}=${value}`;
                    }
                }
            }

            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to get available dashboard configs for execution scope ${executionScope}`);
            }

            // Sort by name
            const dashboardConfigs = await response.json();
            dashboardConfigs.sort((a, b) => a.dashboard.name.localeCompare(b.dashboard.name));
            return dashboardConfigs;
        },
        enableForSite: async function (id) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}/scope/site`);

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to enable dashboard config with id ${id} for site`);
            }
        },
        disableForSite: async function (id) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}/scope/site`);

            const response = await fetch(url, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to disable dashboard config with id ${id} for site`);
            }
        },
        enableForProject: async function (id, projectId) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}/scope/project/${projectId}`);

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to enable dashboard config with id ${id} for project ${projectId}`);
            }
        },
        disableForProject: async function (id, projectId) {
            const url = XNAT.url.csrfUrl(`${this.url}/${id}/scope/project/${projectId}`);

            const response = await fetch(url, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Failed to disable dashboard config with id ${id} for project ${projectId}`);
            }
        },
        editor: async function(dashboardConfig, action, onSaved) {
            let isNew  = action === 'create',
                isCopy = action === 'copy',
                isEdit = action === 'edit',
                title  = isNew || isCopy ? 'New Dashboard' : 'Edit Dashboard';

            XNAT.dialog.open({
                title: title,
                content: spawn('div.dashboard-editor'),
                width: 750,
                maxBtn: true,
                beforeShow: () => {
                    // Create form

                    const formContainer = document.querySelector(`.dashboard-editor`);
                    formContainer.classList.add('panel');

                    dashboardConfig = dashboardConfig ? dashboardConfig : {};
                    let dashboard = dashboardConfig?.dashboard ? dashboardConfig?.dashboard : {};

                    let id = isNew || isCopy ? '' : dashboardConfig?.id ?? '',
                        name = isCopy ? '' : dashboard?.name ?? '',
                        description = dashboard?.description ?? '',
                        framework = dashboard?.framework ?? '',
                        command = dashboard?.command ?? '',
                        gitRepoUrl = dashboard?.gitRepoUrl ?? '',
                        gitRepoBranch = dashboard?.gitRepoBranch ?? '',
                        mainFilePath = dashboard?.mainFilePath ?? '',
                        computeEnvironmentId = dashboardConfig?.computeEnvironmentConfig?.id ?? '',
                        hardwareId = dashboardConfig?.hardwareConfig?.id ?? '';

                    let form = spawn('form.dashboard-edit-form', [
                        spawn('style|type=text/css', `
                            .panel .panel-element input[type="text"],
                            .panel .panel-element select,
                            .panel .panel-element textarea,
                            .panel .panel-element .description  {
                                width: 400px;
                            }
                            
                            .panel .panel-element select[multiple] {
                                max-width: 400px;
                                height: 150px;
                            }
                            
                            hr {
                                margin: 15px 25px;
                            }
                            
                            code {
                                white-space: pre-wrap;
                            }
                        `),
                        spawn('input#id', { type: 'hidden', value: id }),
                        spawn('div.panel-element|data-name=name', [
                            spawn('label.element-label|for=name', 'Name'),
                            spawn('div.element-wrapper', [
                                spawn(`input#name|type=text`, { value: name }),
                                spawn('div.description', 'Provide a name for the dashboard. This will be displayed to users.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element|data-name=description', [
                            spawn('label.element-label|for=description', 'Description'),
                            spawn('div.element-wrapper', [
                                spawn(
                                    `textarea#description|rows=3`,
                                    { style: { fontFamily: 'sans-serif' } },
                                    description
                                ),
                                spawn('div.description', '(Optional) Provide a description for the dashboard. This will be displayed to users.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('hr'),
                        spawn('div.panel-element.file-source.git|data-name=git-repo-url', [
                            spawn('label.element-label|for=git-repo-url', 'Git Repo URL'),
                            spawn('div.element-wrapper', [
                                spawn(`input#git-repo-url|type=text`, { value: gitRepoUrl }),
                                spawn('div.description', 'Enter the URL of the Git repo that contains the dashboard code.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element.file-source.git|data-name=git-repo-branch', [
                            spawn('label.element-label|for=git-repo-branch', 'Branch'),
                            spawn('div.element-wrapper', [
                                spawn(`input#git-repo-branch|type=text`, { value: gitRepoBranch }),
                                spawn('div.description', 'Enter the branch containing the dashboard code.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element.file-source.git|data-name=main-file-path', [
                            spawn('label.element-label|for=main-file-path', 'Main File Path'),
                            spawn('div.element-wrapper', [
                                spawn(`input#main-file-path|type=text`, { value: mainFilePath }),
                                spawn('div.description', 'Enter the path to the main file in the Git repository.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('hr'),
                        spawn('div.panel-element|data-name=framework', [
                            spawn('label.element-label|for=framework', 'Framework'),
                            spawn('div.element-wrapper', [
                                spawn('select#framework',
                                    {
                                        onchange: (e) => {
                                            const advanced = document.querySelectorAll('.panel-element.advanced');
                                            advanced.forEach((element) => {
                                                if (e.target.value.toLowerCase() === 'custom') {
                                                    element.style.display = 'block';
                                                } else {
                                                    element.style.display = 'none';
                                                }
                                            })
                                        }
                                    }, [
                                    spawn('option', { value: '', disabled: true, selected: true }, 'Select a dashboard framework'),
                                ]),
                                spawn('div.description', 'Select which dashboard framework is used to run the dashboard.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element.advanced|data-name=command', {style: { display: 'none' } }, [
                            spawn('label.element-label|for=command', 'Command'),
                            spawn('div.element-wrapper', [
                                spawn(
                                    `textarea#command|rows=6`,
                                    { style: { fontFamily: 'sans-serif' } },
                                    command
                                ),
                                spawn('div.description',
                                    'Enter the command that will be executed to start the dashboard. ' +
                                    'Use the following placeholders to insert values into the command: ' +
                                    '<ul><li><b>{repo}</b> - The URL of the Git repository</li><li><b>{repobranch}</b> - ' +
                                    'The branch of the Git repository</li><li><b>{mainFilePath}</b> - The path to the' +
                                    ' main file in the Git repository</li></ul>' +
                                    'Use the <code>jh-single-native-proxy</code> python package to proxy the dashboard ' +
                                    'container through JupyterHub.' +
                                    '</br></br>' +
                                    'Example:</br>' +
                                    '<code>' +
                                    'jhsingle-native-proxy\n' +
                                    '\t--destport 8505\n' +
                                    '\t--repo {repo}\n' +
                                    '\t--repobranch {repobranch}\n' +
                                    '\t--repofolder /home/jovyan/dashboards\n' +
                                    'streamlit run\n' +
                                    '\t/home/jovyan/dashboards/{mainFilePath}\n' +
                                    '\t{--}server.port 8505\n' +
                                    '\t{--}server.headless True\n' +
                                    '</code>' +
                                    '</br></br>' +
                                    'See documentation for more details.'
                                )
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('hr'),
                        spawn('div.panel-element|data-name=jupyter-environment', [
                            spawn('label.element-label|for=jupyter-environment', 'Jupyter Environment'),
                            spawn('div.element-wrapper', [
                                spawn('select#jupyter-environment', [
                                    spawn('option', { value: '', disabled: true, selected: true }, 'Select a Jupyter environment')
                                ]),
                                spawn('div.description', 'The dashboard will be started in this Jupyter environment.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element|data-name=hardware', [
                            spawn('label.element-label|for=hardware', 'Hardware'),
                            spawn('div.element-wrapper', [
                                spawn('select#hardware', [
                                    spawn('option', { value: '', disabled: true, selected: true }, 'Select hardware')
                                ]),
                                spawn('div.description', 'The dashboard will be started with this hardware configuration.')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('hr'),
                        spawn('div.panel-element|data-name=data-types', [
                            spawn('label.element-label|for=data-types', 'Data Types'),
                            spawn('div.element-wrapper', [
                                spawn('select#data-types', { multiple: true }, []),
                                spawn('div.description', 'Select the XNAT datatypes that this dashboard can be started from.')
                            ]),
                            spawn('div.clear')
                        ]),
                    ]);

                    formContainer.appendChild(form);

                    // Handle Jupyter Environments and Hardware
                    let computeEnvironments = document.querySelector('#jupyter-environment');
                    let hardwares = document.querySelector('#hardware');

                    XNAT.compute.computeEnvironmentConfigs.getAll("JUPYTERHUB").then((computeEnvironmentConfigs) => {
                        computeEnvironmentConfigs.forEach((config) => {
                            let option = document.createElement('option');
                            option.value = config.id;
                            option.text = config.computeEnvironment.name;
                            option.selected = config.id === computeEnvironmentId;
                            computeEnvironments.add(option);
                        })

                        return computeEnvironmentConfigs;
                    }).then((computeEnvironmentConfigs) => {
                        computeEnvironments.addEventListener('change', () => {
                            hardwares.innerHTML = '';
                            hardwares.appendChild(spawn('option', {value: ''}, 'Select hardware'));
                            let computeEnvironmentConfig = computeEnvironmentConfigs.filter(c => c['id'].toString() === computeEnvironments.value)[0];
                            console.debug(computeEnvironmentConfig);
                            let hardwareConfigs = computeEnvironmentConfig['hardwareOptions']['hardwareConfigs'];
                            console.debug(hardwareConfigs);
                            hardwareConfigs.forEach(h => {
                                let option = document.createElement('option');
                                option.value = h['id'];
                                option.text = h['hardware']['name'];
                                option.selected = h['id'] === hardwareId;
                                console.debug(option)
                                hardwares.add(option);
                            });
                        })

                        // Trigger change event to populate hardware options if compute environment
                        // is already selected (i.e. editing)
                        if (computeEnvironmentId) {
                            computeEnvironments.dispatchEvent(new Event('change'));
                        }

                        // Add dashboard frameworks
                        let frameworksSelector = document.querySelector('#framework');
                        XNAT.plugin.jupyterhub.dashboards.frameworks.getAll().then((frameworks) => {
                            frameworks.forEach((framework) => {
                                let option = document.createElement('option');
                                option.value = framework['name'];
                                option.text = framework['name'];
                                option.selected = dashboardConfig?.dashboard?.framework === framework['name'];
                                frameworksSelector.add(option);
                            })

                            // Add Custom option
                            let option = document.createElement('option');
                            option.value = 'Custom';
                            option.text = 'Custom';
                            option.selected =
                                dashboardConfig?.dashboard?.framework === 'Custom' ||
                                dashboardConfig?.dashboard?.framework === 'custom' ||
                                ((isEdit || isCopy) && !dashboardConfig?.dashboard?.framework);
                            frameworksSelector.add(option);

                            // Trigger change event to populate custom command if framework is already selected (i.e. editing)
                            document.querySelector('#framework').dispatchEvent(new Event('change'));
                        });

                        // Add data types
                        let dataTypesSelector = document.querySelector('#data-types');
                        XNAT.plugin.jupyterhub.dashboards.dataTypes.getSome().then((dataTypes) => {
                            dataTypes.forEach((dataType) => {
                                let option = document.createElement('option');
                                option.value = dataType['elementName'];
                                option.text = dataType['plural'];
                                option.selected = dashboardConfig?.scopes?.DataType?.ids?.includes(dataType['elementName']) ?? false;
                                dataTypesSelector.add(option);
                            })
                        });
                    });
                },
                buttons: [
                    {
                        label: 'Cancel',
                        isDefault: false,
                        close: false,
                        action: function () {
                            XNAT.dialog.closeAll();
                        }
                    },
                    {
                        label: isNew || isCopy ? 'Create Dashboard' : 'Save Dashboard',
                        isDefault: true,
                        close: false,
                        action: function (obj) {
                            let form = document.querySelector('.dashboard-edit-form');

                            const validators = [];

                            validators.push(
                                XNAT.validate(form.querySelector('#name'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Name is required')
                            );

                            validators.push(
                                XNAT.validate(form.querySelector('#framework'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Framework is required')
                            );

                            if (form.querySelector('#framework').value.toLowerCase() === 'custom') {
                                // command is required for custom frameworks
                                validators.push(
                                    XNAT.validate(form.querySelector('#command'))
                                        .reset().chain()
                                        .required()
                                        .is('notEmpty')
                                        .failure('Command is required')
                                );
                            } else {
                                // repo, branch and main file path are required for non-custom frameworks
                                validators.push(
                                    XNAT.validate(form.querySelector('#git-repo-url'))
                                        .reset().chain()
                                        .required()
                                        .is('notEmpty')
                                        .failure('Git Repo URL is required')
                                );

                                validators.push(
                                    XNAT.validate(form.querySelector('#git-repo-url'))
                                        .reset().chain()
                                        .is('url')
                                        .failure('Git Repo must be a URL')
                                )

                                validators.push(
                                    XNAT.validate(form.querySelector('#git-repo-branch'))
                                        .reset().chain()
                                        .required()
                                        .is('notEmpty')
                                        .failure('Branch is required')
                                );

                                validators.push(
                                    XNAT.validate(form.querySelector('#main-file-path'))
                                        .reset().chain()
                                        .required()
                                        .is('notEmpty')
                                        .failure('Main File Path is required')
                                );
                            }

                            validators.push(
                                XNAT.validate(form.querySelector('#jupyter-environment'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Jupyter Environment is required')
                            );

                            validators.push(
                                XNAT.validate(form.querySelector('#hardware'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Hardware is required')
                            );

                            validators.push(
                                XNAT.validate(form.querySelector('#data-types'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('At least one data type is required')
                            );

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

                            (async () => {

                                const config = {
                                    id: form.querySelector('#id').value,
                                    dashboard: {
                                        name: form.querySelector('#name').value,
                                        description: form.querySelector('#description').value,
                                        framework: form.querySelector('#framework').value,
                                        command: form.querySelector('#command').value,
                                        fileSource: 'git',
                                        gitRepoUrl: form.querySelector('#git-repo-url').value,
                                        gitRepoBranch: form.querySelector('#git-repo-branch').value,
                                        mainFilePath: form.querySelector('#main-file-path').value,
                                    },
                                    scopes: {
                                        Site: {
                                            scope: 'Site',
                                            enabled: isEdit ? dashboardConfig?.scopes?.Site?.enabled ?? false : true,
                                            ids: dashboardConfig?.scopes?.Site?.ids ?? [],
                                        },
                                        Project: {
                                            scope: 'Project',
                                            enabled: false,
                                            ids: isEdit ? dashboardConfig?.scopes?.Project?.ids ?? [] : [],
                                        },
                                        DataType: {
                                            scope: 'DataType',
                                            enabled: false,
                                            ids: Array.from(form.querySelector('#data-types').selectedOptions).map(o => o.value),
                                        }
                                    },
                                    computeEnvironmentConfig: {
                                        id: form.querySelector('#jupyter-environment').value,
                                    },
                                    hardwareConfig: {
                                        id: form.querySelector('#hardware').value,
                                    }
                                };

                                let response;
                                if (isNew || isCopy) {
                                    response = XNAT.plugin.jupyterhub.dashboards.configs.create(config);
                                } else if (isEdit) {
                                    response = XNAT.plugin.jupyterhub.dashboards.configs.update(config);
                                }

                                response.then(() => {
                                    XNAT.ui.banner.top(2000, 'Dashboard saved.', 'success');
                                    obj.close();
                                    if (onSaved) {
                                        onSaved();
                                    }
                                }).catch((error) => {
                                    XNAT.ui.banner.top(2000, 'Failed to save dashboard', 'error',);
                                    console.error(error);
                                });
                            })();
                        }
                    }
                ]
            })
        },
        table: async function(querySelector, scope) {
            if (!scope) {
                console.error('Scope is required');
                return;
            } else if (!['Site', 'Project'].includes(scope)) {
                console.error(`Invalid scope ${scope}`);
                return;
            }

            const projectId = XNAT.data.context.project;

            let container, footer;

            const init = (querySelector) => {
                container = document.querySelector(querySelector);
                container.innerHTML = '<div class="loading"><i class="fa fa-spinner fa-spin"></i> Loading...</div>'

                container.style.display = 'flex';
                container.style.flexDirection = 'row';
                container.style.justifyContent = 'center';

                if (scope === 'Site') {
                    footer = container.closest('.panel').querySelector('.panel-footer');
                    footer.innerHTML = '';
                    footer.appendChild(newButton());
                }

                refresh();

                // Refresh table when dashboard framework is saved. Framework names may have changed.
                document.addEventListener('dashboard-framework-saved', () => {
                    refresh();
                });
            }

            const clear = () => {
                container.innerHTML = '';
            }

            const newButton = () => {
                return  spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm', { html: 'New Dashboard' , onclick: () => XNAT.plugin.jupyterhub.dashboards.configs.editor(null, 'create', refresh)}),
                    ]),
                    spawn('div.clear.clearFix')
                ]);
            }

            const refresh = async () => {
                XNAT.plugin.jupyterhub.dashboards.configs.getAll().then((dashboardConfigs) => {
                    clear();

                    if (dashboardConfigs.length === 0) {
                        container.innerHTML = `<div class="loading">No dashboards found</div>`;
                    } else {
                        return table(dashboardConfigs);
                    }
                }).catch((error) => {
                    console.error(error);
                    container.innerHTML = `<div class="loading">Failed to load dashboards. See console and system logs for details.</div>`;
                });
            }

            const remove = async (id) => {
                XNAT.dialog.open({
                    title: 'Confirm',
                    content: 'Are you sure you want to delete this dashboard?',
                    width: 400,
                    buttons: [
                        {
                            label: 'Cancel',
                            isDefault: false,
                            close: false,
                            action: function () {
                                XNAT.dialog.closeAll();
                            }
                        },
                        {
                            label: 'Delete',
                            isDefault: true,
                            close: false,
                            action: function (obj) {
                                XNAT.plugin.jupyterhub.dashboards.configs.delete(id).then(() => {
                                    XNAT.ui.banner.top(2000, 'Dashboard deleted.', 'success');
                                    refresh();
                                    XNAT.dialog.closeAll();
                                }).catch((error) => {
                                    XNAT.ui.banner.top(2000, 'Failed to delete dashboard', 'error',);
                                    console.error(error);
                                });
                            }
                        }
                    ]
                })
            }

            const enableToggle = (config, scope, id) => {
                if (scope === 'Project' && !config?.scopes?.Site?.enabled) {
                    // If the dashboard is not enabled for the site, it cannot be enabled for a project
                    return spawn('div.center', ['Disabled for site']);
                }

                const scopeActions = {
                    'Site': {
                        getEnabled: () => config?.scopes?.Site?.enabled ?? undefined,
                        enableAction: () => XNAT.plugin.jupyterhub.dashboards.configs.enableForSite(config['id']),
                        disableAction: () => XNAT.plugin.jupyterhub.dashboards.configs.disableForSite(config['id'])
                    },
                    'Project': {
                        getEnabled: () => config?.scopes?.Project?.ids?.includes(id) ?? false,
                        enableAction: () => XNAT.plugin.jupyterhub.dashboards.configs.enableForProject(config['id'], id),
                        disableAction: () => XNAT.plugin.jupyterhub.dashboards.configs.disableForProject(config['id'], id)
                    }
                };

                let scopeAction = scopeActions[scope];

                if (!scopeAction) {
                    console.error(`Invalid scope ${scope}`);
                    return;
                }

                let enabled = scopeAction.getEnabled();

                let ckbox = spawn('input', {
                    type: 'checkbox',
                    checked: enabled,
                    value: enabled ? 'true' : 'false',
                    data: { checked: enabled },
                    onchange: () => {
                        let enabled = ckbox.checked;
                        let action = enabled ? scopeAction.enableAction : scopeAction.disableAction;

                        action()
                            .then(() => {
                                XNAT.ui.banner.top(2000, `Dashboard ${enabled ? 'enabled' : 'disabled'}.`, 'success');
                            })
                            .catch((error) => {
                                XNAT.ui.banner.top(2000, `Failed to ${enabled ? 'enable' : 'disable'} dashboard.`, 'error');
                                console.error(error);
                                toggleCheckbox(!enabled);
                            });
                    }
                });

                let toggleCheckbox = (enabled) => {
                    ckbox.checked = enabled;
                    ckbox.value = enabled ? 'true' : 'false';
                    ckbox.dataset.checked = enabled;
                };

                return spawn('label.switchbox', [
                    ckbox,
                    ['span.switchbox-outer', [['span.switchbox-inner']]]
                ]);
            };

            const table = async (dashboardConfigs) => {
                const tableColumns = {
                    name: {
                        label: 'Dashboard',
                        filter: true,
                        th: { className: 'left' },
                        apply: function () {
                            return spawn('div.left', [
                                spawn('span', {}, `<b>${this.dashboard?.name}</b><br/>${this.dashboard?.description}`)
                            ]);
                        }
                    },
                    gitRepoUrl: {
                        label: 'Source',
                        th: { style: { width: '75px' } },
                        apply: function () {
                            const isGithub = this.dashboard?.gitRepoUrl.includes('github.com');

                            // if no git repo url, show nothing
                            if (!this.dashboard?.gitRepoUrl || this.dashboard?.gitRepoUrl === '') {
                                return spawn('div.center', ['']);
                            }

                            // otherwise, show github icon or code icon with link to git repo
                            return spawn('div.center', [
                                spawn('a', { href: this.dashboard?.gitRepoUrl ?? '', target: '_blank', style: { color: 'black' }
                                }, isGithub ? `<i class="fa fa-github fa-lg"></i>` : `<i class="fa fa-code fa-lg"></i>`)
                            ]);
                        }
                    },
                    dataTypes: {
                        label: 'Data Types',
                        th: { style: { width: '150px' } },
                        filter: true,
                        apply: function () {
                            let display = '';

                            if (this.scopes?.DataType?.enabled) {
                                display = 'All';
                            } else {
                                // Sort ids alphabetically
                                let sortedIds = this.scopes?.DataType?.ids?.sort((a, b) => a.localeCompare(b));
                                display = sortedIds?.join(', ');

                                // if display is empty, show 'None'
                                if (display === '' || display === ' ') {
                                    display = 'None';
                                }
                            }

                            return spawn('div.left', [
                                spawn('span', {}, display)
                            ]);
                        }
                    },
                    isEnabled: {
                        label: 'Enabled',
                        th: { style: { width: '50px' } },
                        apply: function() {
                            return spawn('div.center', [enableToggle(this, scope, projectId)]);
                        }
                    },
                    actions: {
                        label: 'Actions',
                        th: { style: { width: '150px' } },
                        apply: function() {
                            return spawn('div.center', [
                                spawn('button.btn.btn-sm', { onclick: () => XNAT.plugin.jupyterhub.dashboards.configs.editor(this, 'edit', refresh) }, '<i class="fa fa-pencil" title="Edit"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => XNAT.plugin.jupyterhub.dashboards.configs.editor(this, 'copy', refresh) }, '<i class="fa fa-clone" title="Duplicate"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => remove(this['id'])}, '<i class="fa fa-trash" title="Delete"></i>')
                            ]);
                        }
                    }
                };

                if (scope === 'Project') {
                    delete tableColumns['actions'];
                }

                const table = XNAT.table.dataTable(dashboardConfigs, {
                    header: true,
                    sortable: 'name, version, framework, fileSource, computeEnvironment, hardware, isPublic, isEnabled',
                    columns: tableColumns
                });

                clear()
                table.render(`${querySelector}`);
            }

            init(querySelector);

            return {
                init: init,
                refresh: refresh,
            }
        }
    }

    XNAT.plugin.jupyterhub.dashboards.frameworks = {
        url: `/xapi/jupyterhub/dashboards/frameworks`,
        get: async function (name) {
            const url = XNAT.url.csrfUrl(`${this.url}/${name}`);

            const response = await fetch(url, {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            });

            if (!response.ok) {
                throw new Error(`HTTP error getting dashboard framework ${name}: ${response.status}`);
            }

            return response.json();
        },
        getAll: async function () {
            const url = XNAT.url.csrfUrl(this.url);

            const response = await fetch(url, {
                method: 'GET',
                headers: {'Content-Type': 'application/json'}
            });

            if (!response.ok) {
                throw new Error(`HTTP error getting dashboard frameworks: ${response.status}`);
            }

            let frameworks = await response.json();
            frameworks.sort((a, b) => a.name.localeCompare(b.name));
            return frameworks;
        },
        create: async function (framework) {
            const url = XNAT.url.csrfUrl(this.url);

            const response = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(framework)
            });

            if (!response.ok) {
                throw new Error(`HTTP error creating dashboard framework: ${response.status}`);
            }

            return response.json();
        },
        update: async function (name, framework) {
            name = encodeURIComponent(name);
            const url = XNAT.url.csrfUrl(`${this.url}/${name}`);

            const response = await fetch(url, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(framework)
            });

            if (!response.ok) {
                throw new Error(`HTTP error updating dashboard framework ${name}: ${response.status}`);
            }

            return response.json();
        },
        delete: async function (name) {
            name = encodeURIComponent(name);
            const url = XNAT.url.csrfUrl(`${this.url}/${name}`);

            const response = await fetch(url, {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'}
            });

            if (!response.ok) {
                throw new Error(`HTTP error deleting dashboard framework ${name}: ${response.status}`);
            }
        },
        editor: async function(framework, action, onSaved) {
            let isNew  = action === 'create',
                isCopy = action === 'copy',
                isEdit = action === 'edit',
                title  = isNew || isCopy ? 'Add Dashboard Framework' : 'Edit Dashboard Framework';

            XNAT.dialog.open({
                title: title,
                content: spawn('div.dashboard-framework-editor'),
                width: 750,
                maxBtn: true,
                beforeShow: () => {
                    // Create form
                    const formContainer = document.querySelector(`.dashboard-framework-editor`);
                    formContainer.classList.add('panel');

                    framework = framework ? framework : {};

                    let id = isNew || isCopy ? '' : framework?.id ?? '',
                        name = isCopy ? '' : framework?.name ?? '',
                        commandTemplate = framework?.commandTemplate ?? '';

                    let form = spawn('form.dashboard-framework-edit-form', [
                        spawn('style|type=text/css', `
                            .panel .panel-element input[type="text"],
                            .panel .panel-element select,
                            .panel .panel-element textarea,
                            .panel .panel-element .description  {
                                width: 400px;
                            }
                            
                            .panel .panel-element select[multiple] {
                                max-width: 400px;
                                height: 150px;
                            }
                            
                            code {
                                white-space: pre-wrap;
                            }
                        `),
                        spawn('input#id', { type: 'hidden', value: id }),
                        spawn('div.panel-element|data-name=name', [
                            spawn('label.element-label|for=name', 'Name'),
                            spawn('div.element-wrapper', [
                                spawn(`input#name|type=text`, { value: name }),
                                spawn('div.description', 'The name of the dashboard framework')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element|data-name=command-template', [
                            spawn('label.element-label|for=command-template', 'Command Template'),
                            spawn('div.element-wrapper', [
                                spawn(
                                    `textarea#command-template|rows=10`,
                                    { style: { fontFamily: 'sans-serif' } },
                                    commandTemplate
                                ),
                                spawn('div.description',
                                    'Enter a template for the command that will be executed to start the dashboard container. ' +
                                    'Use the following placeholders to insert values into the command: ' +
                                    '<ul><li><b>{repo}</b> - The URL of the Git repository</li><li><b>{repobranch}</b> - ' +
                                    'The branch of the Git repository</li><li><b>{mainFilePath}</b> - The path to the' +
                                    ' main file in the Git repository</li></ul>' +
                                    'Use the <code>jh-single-native-proxy</code> python package to proxy the dashboard ' +
                                    'container through JupyterHub.' +
                                    '</br></br>' +
                                    'Example:</br>' +
                                    '<code>' +
                                    'jhsingle-native-proxy\n' +
                                    '\t--destport 8505\n' +
                                    '\t--repo {repo}\n' +
                                    '\t--repobranch {repobranch}\n' +
                                    '\t--repofolder /home/jovyan/dashboards\n' +
                                    'streamlit run\n' +
                                    '\t/home/jovyan/dashboards/{mainFilePath}\n' +
                                    '\t{--}server.port 8505\n' +
                                    '\t{--}server.headless True\n' +
                                    '</code>' +
                                    '</br></br>' +
                                    'See documentation for more details.'
                                )
                            ]),
                            spawn('div.clear')
                        ]),
                    ]);

                    formContainer.appendChild(form);
                },
                buttons: [
                    {
                        label: 'Cancel',
                        isDefault: false,
                        close: false,
                        action: function () {
                            XNAT.dialog.closeAll();
                        }
                    },
                    {
                        label: isNew || isCopy ? 'Add Framework' : 'Save Framework',
                        isDefault: true,
                        close: false,
                        action: function (obj) {
                            let form = document.querySelector('.dashboard-framework-edit-form');

                            const validators = [];

                            validators.push(
                                XNAT.validate(form.querySelector('#name'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Name is required')
                            );

                            validators.push(
                                XNAT.validate(form.querySelector('#command-template'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Command template is required')
                            );

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

                            const frameworkToSave = {
                                id: form.querySelector('#id').value,
                                name: form.querySelector('#name').value,
                                commandTemplate: form.querySelector('#command-template').value
                            };

                            let response;
                            if (isNew || isCopy) {
                                response = XNAT.plugin.jupyterhub.dashboards.frameworks.create(frameworkToSave);
                            } else if (isEdit) {
                                response = XNAT.plugin.jupyterhub.dashboards.frameworks.update(framework['name'], frameworkToSave);
                            }

                            response.then(() => {
                                XNAT.ui.banner.top(2000, 'Dashboard framework saved.', 'success');
                                obj.close();
                                if (onSaved) {
                                    onSaved();
                                }

                                // Dispatch event to refresh dashboard configs table
                                document.dispatchEvent(new Event('dashboard-framework-saved'));
                            }).catch((error) => {
                                XNAT.ui.banner.top(2000, 'Failed to save dashboard framework', 'error',);
                                console.error(error);
                            });
                        }
                    }
                ],
            });
        },
        table: async function(querySelector) {
            let container, footer;

            const init = (querySelector) => {
                container = document.querySelector(querySelector);
                container.innerHTML = '<div class="loading"><i class="fa fa-spinner fa-spin"></i> Loading...</div>'

                container.style.display = 'flex';
                container.style.flexDirection = 'row';
                container.style.justifyContent = 'center';

                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                footer.appendChild(newButton());

                refresh();
            }

            const clear = () => {
                container.innerHTML = '';
            }

            const refresh = async () => {
                const frameworks = await XNAT.plugin.jupyterhub.dashboards.frameworks.getAll();

                clear();

                if (Object.keys(frameworks).length === 0) {
                    container.innerHTML = `<div class="loading">No frameworks found</div>`;
                } else {
                    return table(frameworks);
                }
            }

            const newButton = () => {
                return  spawn('div', [
                    spawn('div.pull-right', [
                        spawn('button.btn.btn-sm', { html: 'Add Framework' , onclick: () => XNAT.plugin.jupyterhub.dashboards.frameworks.editor(null, 'create', refresh)}),
                    ]),
                    spawn('div.clear.clearFix')
                ]);
            }

            const remove = async (name) => {
                XNAT.dialog.open({
                    title: 'Confirm',
                    content: 'Are you sure you want to delete this dashboard framework?',
                    width: 400,
                    buttons: [
                        {
                            label: 'Cancel',
                            isDefault: false,
                            close: false,
                            action: function () {
                                XNAT.dialog.closeAll();
                            }
                        },
                        {
                            label: 'Delete',
                            isDefault: true,
                            close: false,
                            action: function (obj) {
                                XNAT.plugin.jupyterhub.dashboards.frameworks.delete(name).then(() => {
                                    XNAT.ui.banner.top(2000, 'Dashboard framework deleted.', 'success');
                                    refresh();
                                    XNAT.dialog.closeAll();
                                }).catch((error) => {
                                    XNAT.ui.banner.top(2000, 'Failed to delete dashboard framework', 'error',);
                                    console.error(error);
                                });
                            }
                        }
                    ]
                });
            }

            const table = async (frameworks) => {
                const tableColumns = {
                    name: {
                        label: 'Name',
                        filter: true,
                        th: { className: 'left' },
                        apply: function () {
                            return spawn('div.left', [
                                spawn('span', {}, this['name'])
                            ]);
                        }
                    },
                    actions: {
                        label: 'Actions',
                        th: { style: { width: '150px' } },
                        apply: function() {
                            return spawn('div.center', [
                                spawn('button.btn.btn-sm', { onclick: () => XNAT.plugin.jupyterhub.dashboards.frameworks.editor(this, 'edit', refresh) }, '<i class="fa fa-pencil" title="Edit"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => XNAT.plugin.jupyterhub.dashboards.frameworks.editor(this, 'copy', refresh) }, '<i class="fa fa-clone" title="Duplicate"></i>'),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm', { onclick: () => remove(this['name'])}, '<i class="fa fa-trash" title="Delete"></i>')
                            ]);
                        }
                    }
                };

                const table = XNAT.table.dataTable(frameworks, {
                    header: true,
                    sortable: 'name',
                    columns: tableColumns
                });

                clear()
                table.render(`${querySelector}`);
            }

            init(querySelector);
        }
    }

}));