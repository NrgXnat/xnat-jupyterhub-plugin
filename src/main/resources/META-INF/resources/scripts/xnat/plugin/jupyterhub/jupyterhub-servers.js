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
XNAT.compute = getObject(XNAT.compute || {});
XNAT.compute.computeEnvironmentConfigs = getObject(XNAT.compute.computeEnvironmentConfigs || {});

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

    let newServerUrl = XNAT.plugin.jupyterhub.servers.newServerUrl = function (username, servername) {
        let url = `/xapi/jupyterhub/users/${username}/server`;

        // if (servername && servername !== "") {
        //     url = `${url}/${servername}`;
        // }
        
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

    XNAT.plugin.jupyterhub.servers.startDashboardForProject = function(username = window.username,
                                                                       servername = XNAT.data.context.projectID,
                                                                       xsiType = XNAT.data.context.xsiType,
                                                                       itemId = XNAT.data.context.projectID,
                                                                       projectId = XNAT.data.context.projectID,
                                                                       eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startDashboardForProject`);
        startDashboard(username, servername, xsiType, itemId, projectId, projectId, eventTrackingId);
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

    XNAT.plugin.jupyterhub.servers.startDashboardForSubject = function(username = window.username,
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
                content: "Cannot start a dashboard on a shared subject.",
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
            getSubjectLabel(itemId).then(subjectLabel => {
                startDashboard(username, servername, xsiType, itemId, subjectLabel, projectId, eventTrackingId)
            }).catch(e => {
                console.error(`Error getting subject label: ${e}`);
            });
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

    XNAT.plugin.jupyterhub.servers.startDashboardForExperiment = function(username = window.username,
                                                                          servername = XNAT.data.context.ID,
                                                                          xsiType = XNAT.data.context.xsiType,
                                                                          itemId = XNAT.data.context.ID,
                                                                          projectId = XNAT.data.context.projectID,
                                                                          eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startDashboardForExperiment`);

        if (XNAT.plugin.jupyterhub.isSharedItem()) {
            XNAT.dialog.open({
                width: 450,
                title: "Unsupported Operation",
                content: "Cannot start a dashboard on a shared experiment.",
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
            getExperimentLabel(itemId).then(experimentLabel => {
                startDashboard(username, servername, xsiType, itemId, experimentLabel, projectId, eventTrackingId)
            }).catch(e => {
                console.error(`Error getting experiment label: ${e}`);
            });
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

        const executionScope = {
            'site': 'XNAT',
            'user': username,
            'prj': projectId,
        }

        XNAT.compute.computeEnvironmentConfigs.available("JUPYTERHUB", executionScope).then(computeEnvironmentConfigs => {
            const cancelButton = {
                label: 'Cancel',
                isDefault: false,
                close: true,
            }
            
            const startButton = {
                label: 'Start Notebook',
                isDefault: true,
                close: false,
                action: function(obj) {
                    const computeEnvironmentConfigId = document.querySelector('select#compute-environment-config').value;
                    const hardwareConfigId = document.querySelector('select#hardware-config').value;
                    
                    if (!computeEnvironmentConfigId) {
                        XNAT.dialog.open({
                            width: 450,
                            title: "Error",
                            content: "Please select a Jupyter environment.",
                            buttons: [
                                {
                                    label: 'OK',
                                    isDefault: true,
                                    close: true,
                                }
                            ]
                        });
                        
                        return;
                    }
                    
                    if (!hardwareConfigId) {
                        XNAT.dialog.open({
                            width: 450,
                            title: "Error",
                            content: "Please select a hardware configuration.",
                            buttons: [
                                {
                                    label: 'OK',
                                    isDefault: true,
                                    close: true,
                                }
                            ]
                        });
                        
                        return;
                    }
                    
                    const serverStartRequest = {
                        'username': username,
                        'servername': '', // Not supported yet
                        'xsiType': xsiType,
                        'itemId': itemId,
                        'itemLabel': itemLabel,
                        'projectId': projectId,
                        'eventTrackingId': eventTrackingId,
                        'computeEnvironmentConfigId': computeEnvironmentConfigId,
                        'hardwareConfigId': hardwareConfigId,
                    }
        
                    XNAT.xhr.ajax({
                        url: newServerUrl(username, servername),
                        method: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify(serverStartRequest),
                        beforeSend: function () {
                            XNAT.app.activityTab.start(
                                'Start Jupyter Notebook' +
                                `<div class="actions"><a id="open-nb-${eventTrackingId}" class="icn open" style="display: none;"><i class="fa fa-external-link"></i></a>`,
                                eventTrackingId,
                                'XNAT.plugin.jupyterhub.servers.activityTabCallback', 1000);
                        },
                        fail: function (error) {
                            console.error(`Failed to send Jupyter server request: ${error}`)
                        }
                    });
                    
                    xmodal.closeAll();
                    XNAT.ui.dialog.closeAll();
                }
            }
            
            const buttons = (computeEnvironmentConfigs.length === 0) ? [cancelButton] : [cancelButton, startButton];
            
            XNAT.dialog.open({
                title: 'Start Jupyter Notebook',
                content: spawn('form#server-start-request-form'),
                maxBtn: true,
                width: 500,
                beforeShow: function(obj) {
                    const form = document.getElementById('server-start-request-form');
                    form.classList.add('panel');
                    
                    if (computeEnvironmentConfigs.length === 0) {
                        obj.$modal.find('.xnat-dialog-content').html('<div class="error">No compute environments are available for Jupyter. Please contact your administrator.</div>');
                        return;
                    }
                    
                    let xnatData = spawn('div.xnat-data', {
                        style: {
                            marginTop: '10px',
                            marginBottom: '40px',
                        }
                    }, [
                        spawn('h2', 'XNAT Data'),
                        spawn('p.xnat-data-row.description', 'The following data will be available to your Jupyter Notebook Server'),
                        spawn('p.xnat-data-row.project', [
                            spawn('strong', 'Project '), projectId,
                        ]),
                        spawn('p.xnat-data-row.subject', [
                            spawn('strong', 'Subject '), itemLabel,
                        ]),
                        spawn('p.xnat-data-row.experiment', [
                            spawn('strong', 'Experiment '), itemLabel,
                        ]),
                    ]);
                    
                    xnatData.querySelectorAll('strong').forEach(s => {
                        s.style.display = 'inline-block';
                        s.style.width = '90px';
                    });
                    
                    if (xsiType === 'xnat:experimentData') {
                        xnatData.querySelector('p.project').remove();
                        xnatData.querySelector('p.subject').remove();
                    } else if (xsiType === 'xnat:subjectData') {
                        xnatData.querySelector('p.project').remove();
                        xnatData.querySelector('p.experiment').remove();
                    } else if (xsiType === 'xnat:projectData') {
                        xnatData.querySelector('p.subject').remove();
                        xnatData.querySelector('p.experiment').remove();
                    }
                    
                    let computeEnvironmentConfigSelect = spawn('div', { style : { marginTop: '20px', marginBottom: '40px', } }, [
                        spawn('h2', 'Jupyter Environment'),
                        spawn('p.description', 'Select from the list of Jupyter environments available to you. This determines the software available to your Jupyter notebook server.'),
                        spawn('select#compute-environment-config', [
                            spawn('option', {value: ''}, 'Select a Jupyter environment'),
                            ...computeEnvironmentConfigs.map(c => spawn('option', {value: c['id']}, c['computeEnvironment']['name'])),
                        ])]
                    );

                    let hardwareConfigSelect = spawn('div', { style : { marginTop: '20px', marginBottom: '40px', } }, [
                        spawn('h2', 'Hardware'),
                        spawn('p.description', 'Select from the list of available Hardware. This determines the memory, CPU and other hardware resources available to your Jupyter notebook server.'),
                        spawn('select#hardware-config', [
                            spawn('option', {value: ''}, 'Select Hardware'),
                        ])]
                    );
                    
                    form.appendChild(spawn('!', [
                        xnatData,
                        computeEnvironmentConfigSelect,
                        hardwareConfigSelect
                    ]));
                    
                    computeEnvironmentConfigSelect.querySelector('select').addEventListener('change', () => {
                        let hardwareSelect = document.getElementById('hardware-config');
                        hardwareSelect.innerHTML = '';
                        hardwareSelect.appendChild(spawn('option', {value: ''}, 'Select Hardware'));
                        let computeEnvironmentConfig = computeEnvironmentConfigs.filter(c => c['id'].toString() === computeEnvironmentConfigSelect.querySelector('select').value)[0];
                        let hardwareConfigs = computeEnvironmentConfig['hardwareOptions']['hardwareConfigs'];
                        hardwareConfigs.forEach(h => {
                            hardwareSelect.appendChild(spawn('option', {value: h['id']}, h['hardware']['name']));
                        });
                    });
                    
                    form.style.marginLeft = '30px';
                    form.style.marginRight = '30px';
                    
                    form.querySelectorAll('p.description').forEach(p => {
                        p.style.marginTop = '0';
                        p.style.marginBottom = '15px';
                        p.style.color = '#777';
                    });
                    
                    form.querySelectorAll('h2').forEach(h => {
                        h.style.marginTop = '0';
                        h.style.marginBottom = '5px';
                        h.style.color = '#222';
                    });
                    
                    form.querySelectorAll('select').forEach(s => {
                        s.style.width = '100%';
                    });
                },
                buttons: buttons
            });
        });
    }

    let startDashboard = XNAT.plugin.jupyterhub.servers.startDashboard = function(username, servername, xsiType, itemId, itemLabel, projectId, eventTrackingId) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startDashboard`);
        console.debug(`Starting dashboard. User: ${username}, Server Name: ${servername}, XSI Type: ${xsiType}, ID: ${itemId}, Label: ${itemLabel}, Project ID: ${projectId}, eventTrackingId: ${eventTrackingId}`);

        const executionScope = {
            'site': 'XNAT',
            'user': username,
            'prj': projectId,
            'datatype': xsiType,
        }

        XNAT.plugin.jupyterhub.dashboards.configs.available(executionScope).then(dashboardConfigs => {
            const cancelButton = {
                label: 'Cancel',
                isDefault: false,
                close: true,
            }

            const startButton = {
                label: 'Start Dashboard',
                isDefault: true,
                close: false,
                action: function(obj) {
                    const dashboardConfigId = document.querySelector('#dashboard-config').value;
                    const computeEnvironmentConfigId = document.querySelector('#compute-environment-config').value;
                    const hardwareConfigId = document.querySelector('#hardware-config').value;

                    if (!dashboardConfigId) {
                        XNAT.dialog.open({
                            width: 450,
                            title: "Error",
                            content: "Please select a Dashboard.",
                            buttons: [
                                {
                                    label: 'OK',
                                    isDefault: true,
                                    close: true,
                                }
                            ]
                        });

                        return;
                    }

                    const serverStartRequest = {
                        'username': username,
                        'servername': '', // Not supported yet
                        'xsiType': xsiType,
                        'itemId': itemId,
                        'itemLabel': itemLabel,
                        'projectId': projectId,
                        'eventTrackingId': eventTrackingId,
                        'dashboardConfigId': dashboardConfigId,
                        'computeEnvironmentConfigId': computeEnvironmentConfigId,
                        'hardwareConfigId': hardwareConfigId,
                    }

                    XNAT.xhr.ajax({
                        url: newServerUrl(username, servername),
                        method: 'POST',
                        contentType: 'application/json',
                        data: JSON.stringify(serverStartRequest),
                        beforeSend: function () {
                            XNAT.app.activityTab.start(
                                'Start Jupyter Dashboard' +
                                `<div class="actions"><a id="open-nb-${eventTrackingId}" class="icn open" style="display: none;"><i class="fa fa-external-link"></i></a>`,
                                eventTrackingId,
                                'XNAT.plugin.jupyterhub.servers.activityTabCallback', 1000);
                        },
                        fail: function (error) {
                            console.error(`Failed to send Jupyter server request: ${error}`)
                        }
                    });

                    xmodal.closeAll();
                    XNAT.ui.dialog.closeAll();
                }
            }

            const buttons = (dashboardConfigs.length === 0) ? [cancelButton] : [cancelButton, startButton];

            XNAT.dialog.open({
                title: 'Start Jupyter Dashboard',
                content: spawn('form#server-start-request-form'),
                maxBtn: true,
                width: 400,
                height: 500,
                beforeShow: function(obj) {
                    const form = document.getElementById('server-start-request-form');
                    form.classList.add('panel');

                    if (dashboardConfigs.length === 0) {
                        obj.$modal.find('.xnat-dialog-content').html('<div class="error">No dashboards are available. Please contact your administrator.</div>');
                        return;
                    }

                    let xnatData = spawn('div.xnat-data', {
                        style: {
                            marginTop: '10px',
                            marginBottom: '40px',
                        }
                    }, [
                        spawn('h2', 'XNAT Data'),
                        spawn('p.xnat-data-row.description', 'The following data will be available to your dashboard.'),
                        spawn('p.xnat-data-row.project', [
                            spawn('strong', 'Project '), projectId,
                        ]),
                        spawn('p.xnat-data-row.subject', [
                            spawn('strong', 'Subject '), itemLabel,
                        ]),
                        spawn('p.xnat-data-row.experiment', [
                            spawn('strong', 'Experiment '), itemLabel,
                        ]),
                    ]);

                    xnatData.querySelectorAll('strong').forEach(s => {
                        s.style.display = 'inline-block';
                        s.style.width = '90px';
                    });

                    if (xsiType === 'xnat:projectData') {
                        xnatData.querySelector('p.subject').remove();
                        xnatData.querySelector('p.experiment').remove();
                    } else if (xsiType === 'xnat:subjectData') {
                        xnatData.querySelector('p.project').remove();
                        xnatData.querySelector('p.experiment').remove();
                    } else { // xsiType is an xnat:experimentData
                        xnatData.querySelector('p.project').remove();
                        xnatData.querySelector('p.subject').remove();
                    }

                    let dashboardConfigSelect = spawn('div', [
                        spawn('h2', 'Dashboard'),
                        spawn('p.description', 'Select from the list of available dashboards.'),
                        spawn('div.form-group', [
                            spawn('select#dashboard-config | size=5', [
                                ...dashboardConfigs.map(c => spawn('option', {value: c['id']}, c['dashboard']['name'])),
                            ]),
                            spawn('div#dashboard-description.description', ''),
                        ]),
                        spawn('input#hardware-config', {type: 'hidden', value: ''}),
                        spawn('input#compute-environment-config', {type: 'hidden', value: ''}),
                        spawn('style', {type: 'text/css'}, `
                        
                            div.form-group {
                                display: flex;
                                flex-direction: column;
                                align-items: flex-start;
                                justify-content: flex-start;
                                gap: 10px;
                            }
                            
                            div.form-group select {
                                width: 100%;
                                font-size: 1.1em;
                            }
                            
                            div.form-group div.description {
                                font-size: 1em;
                                color: #555;
                                font-weight: bold;
                            }
                            
                            p.description {
                                font-size: .9em;
                                color: #777;
                            }
                            
                            `)
                    ]);

                    form.appendChild(spawn('!', [
                        xnatData,
                        dashboardConfigSelect
                    ]));

                    dashboardConfigSelect.querySelector('select').addEventListener('change', () => {
                        const dashboardConfig = dashboardConfigs.filter(c => c['id'].toString() === dashboardConfigSelect.querySelector('select').value)[0];
                        const dashboard = dashboardConfig['dashboard'];
                        document.getElementById('hardware-config').value = dashboardConfig['hardwareConfig']['id'];
                        document.getElementById('compute-environment-config').value = dashboardConfig['computeEnvironmentConfig']['id'];
                        document.getElementById('dashboard-description').innerHTML = dashboard['description'];
                    });
                },
                buttons: buttons
            });
        })
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
        Promise.all([
            XNAT.plugin.jupyterhub.preferences.get('jupyterHubHostUrl'),
            XNAT.plugin.jupyterhub.users.tokens.create()
        ]).then(([jupyterHubHostUrl, token]) => {
            // Remove trailing slash from jupyterHubHostUrl (if present)
            jupyterHubHostUrl = jupyterHubHostUrl.replace(/\/$/, "");

            // open new tab to Jupyter notebook server
            window.open(`${jupyterHubHostUrl}${server_url}?token=${token['token']}`, '_blank');
        }).catch(() => {
            window.open(server_url, '_blank');
        });
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