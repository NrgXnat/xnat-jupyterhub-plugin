/**
 * JupyterHub Dashboard Functions
 */

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.dashboards = getObject(XNAT.plugin.jupyterhub.dashboards || {});
XNAT.plugin.jupyterhub.dashboards.configs = getObject(XNAT.plugin.jupyterhub.dashboards.configs || {});
XNAT.plugin.jupyterhub.dashboards.dataTypes = getObject(XNAT.plugin.jupyterhub.dashboards.dataTypes || {});

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
        url: `/xapi/jupyterhub/dashboard-configs`,
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

            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`Failed to get available dashboard configs for execution scope ${executionScope}`);
            }
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
                            .panel .panel-element textarea {
                                width: 400px;
                            }
                            
                            .panel .panel-element select[multiple] {
                                max-width: 400px;
                                height: 150px;
                            }
                            
                            hr {
                                margin: 15px 25px;
                            }
                        `),
                        spawn('input#id', { type: 'hidden', value: id }),
                        spawn('div.panel-element|data-name=name', [
                            spawn('label.element-label|for=name', 'Name'),
                            spawn('div.element-wrapper', [
                                spawn(`input#name|type=text`, { value: name }),
                                spawn('div.description', '')
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
                                spawn('div.description', '(Optional)')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('hr'),
                        spawn('div.panel-element.file-source.git|data-name=git-repo-url', [
                            spawn('label.element-label|for=git-repo-url', 'Git Repo URL'),
                            spawn('div.element-wrapper', [
                                spawn(`input#git-repo-url|type=text`, { value: gitRepoUrl }),
                                spawn('div.description', '')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element.file-source.git|data-name=git-repo-branch', [
                            spawn('label.element-label|for=git-repo-branch', 'Branch'),
                            spawn('div.element-wrapper', [
                                spawn(`input#git-repo-branch|type=text`, { value: gitRepoBranch }),
                                spawn('div.description', '')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element.file-source.git|data-name=main-file-path', [
                            spawn('label.element-label|for=main-file-path', 'Main File Path'),
                            spawn('div.element-wrapper', [
                                spawn(`input#main-file-path|type=text`, { value: mainFilePath }),
                                spawn('div.description', '')
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
                                    spawn('option', { value: 'Voila', selected: framework.toLowerCase() === 'voila' }, 'Voila'),
                                    spawn('option', { value: 'Dash', selected: framework.toLowerCase() === 'dash' }, 'Dash'),
                                    spawn('option', { value: 'Streamlit', selected: framework.toLowerCase() === 'streamlit' }, 'Streamlit'),
                                    spawn('option', { value: 'Panel', selected: framework.toLowerCase() === 'panel' }, 'Panel'),
                                    spawn('option', { value: 'Custom', selected: framework.toLowerCase() === 'custom' }, 'Custom')
                                ]),
                                spawn('div.description', '')
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
                                spawn('div.description', 'Enter the command that will be executed to start the dashboard')
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
                                spawn('div.description', '')
                            ]),
                            spawn('div.clear')
                        ]),
                        spawn('div.panel-element|data-name=hardware', [
                            spawn('label.element-label|for=hardware', 'Hardware'),
                            spawn('div.element-wrapper', [
                                spawn('select#hardware', [
                                    spawn('option', { value: '', disabled: true, selected: true }, 'Select hardware')
                                ]),
                                spawn('div.description', '')
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

                    XNAT.compute.computeEnvironmentConfigs.available("JUPYTERHUB").then((computeEnvironmentConfigs) => {
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

                        // Trigger change event to populate custom command if framework is already selected (i.e. editing)
                        if (framework) {
                            document.querySelector('#framework').dispatchEvent(new Event('change'));
                        }

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
                                XNAT.validate(form.querySelector('#git-repo-url'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Git Repo URL is required')
                            );

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

                            validators.push(
                                XNAT.validate(form.querySelector('#framework'))
                                    .reset().chain()
                                    .required()
                                    .is('notEmpty')
                                    .failure('Framework is required')
                            );

                            if (form.querySelector('#framework').value.toLowerCase() === 'custom') {
                                validators.push(
                                    XNAT.validate(form.querySelector('#command'))
                                        .reset().chain()
                                        .required()
                                        .is('notEmpty')
                                        .failure('Command is required')
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
                                            enabled: dashboardConfig?.scopes?.Site?.enabled ?? true,
                                            ids: dashboardConfig?.scopes?.Site?.ids ?? [],
                                        },
                                        Project: {
                                            scope: 'Project',
                                            enabled: false,
                                            ids: dashboardConfig?.scopes?.Project?.ids ?? [],
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
                                    XNAT.ui.banner.top(2000, 'success', 'Dashboard saved.');
                                    obj.close();
                                    if (onSaved) {
                                        onSaved();
                                    }
                                }).catch((error) => {
                                    XNAT.ui.banner.top(2000, 'error', 'Failed to save dashboard');
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

                footer = container.closest('.panel').querySelector('.panel-footer');
                footer.innerHTML = '';
                footer.appendChild(newButton());

                refresh();
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
                const dashboardConfigs = await XNAT.plugin.jupyterhub.dashboards.configs.getAll();

                clear();

                if (dashboardConfigs.length === 0) {
                    container.innerHTML = `<div class="loading">No dashboards found</div>`;
                } else {
                    return table(dashboardConfigs);
                }
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
                                display = this.scopes?.DataType?.ids?.join(' ') ?? '';

                                // if display is empty, show 'None'
                                if (display === '' || display === ' ') {
                                    display = 'None';
                                }
                            }

                            return spawn('div.center', [
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
}));