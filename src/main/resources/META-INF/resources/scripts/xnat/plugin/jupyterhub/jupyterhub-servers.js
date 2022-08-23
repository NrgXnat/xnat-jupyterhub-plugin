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

    let newServerUrl = XNAT.plugin.jupyterhub.servers.newServerUrl = function(username, servername, xsiType, itemId, projectId, eventTrackingId) {
        let url = `/xapi/jupyterhub/users/${username}/server`;

        // if (servername !== '') {
        //     url = `${url}/${servername}`;
        // }

        url = `${url}?xsiType=${xsiType}&itemId=${itemId}&projectId=${projectId}&eventTrackingId=${eventTrackingId}`

        return restUrl(url);
    }

    let serverUrl = XNAT.plugin.jupyterhub.servers.serverUrl = function(username, servername, eventTrackingId) {
        let url = `/xapi/jupyterhub/users/${username}/server/${servername}?eventTrackingId=${eventTrackingId}`;
        return restUrl(url);
    }

    XNAT.plugin.jupyterhub.servers.startServerForProject = function(username = window.username,
                                                            servername = XNAT.data.context.projectID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            itemId = XNAT.data.context.projectID,
                                                            projectId = XNAT.data.context.projectID,
                                                            eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForProject`);
        startServer(username, servername, xsiType, itemId, projectId, eventTrackingId)
    }

    XNAT.plugin.jupyterhub.servers.startServerForSubject = function(username = window.username,
                                                            servername = XNAT.data.context.ID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            itemId = XNAT.data.context.ID,
                                                            projectId = XNAT.data.context.projectID,
                                                            eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForSubject`);
        startServer(username, servername, xsiType, itemId, projectId, eventTrackingId)
    }

    XNAT.plugin.jupyterhub.servers.startServerForExperiment = function(username = window.username,
                                                               servername = XNAT.data.context.ID,
                                                               xsiType = "xnat:experimentData",
                                                               itemId = XNAT.data.context.ID,
                                                               projectId = XNAT.data.context.projectID,
                                                               eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForExperiment`);
        startServer(username, servername, xsiType, itemId, projectId, eventTrackingId)
    }

    XNAT.plugin.jupyterhub.servers.startServerForStoredSearch = function(username = window.username,
                                                                 servername = '', // this is not in XNAT.data.context.ID
                                                                 xsiType = "xdat:stored_search",
                                                                 itemId = '', // this is not in XNAT.data.context.ID
                                                                 eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForStoredSearch`);
        startServer(username, servername, xsiType, itemId, undefined, eventTrackingId)
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

    let startServer = XNAT.plugin.jupyterhub.servers.startServer = function(username, servername, xsiType, itemId, projectId, eventTrackingId) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServer`);
        console.debug(`Launching jupyter server. User: ${username}, Server Name: ${servername}, XSI Type: ${xsiType}, ID: ${itemId}, Project ID: ${projectId}, eventTrackingId: ${eventTrackingId}`);

        return XNAT.xhr.ajax({
            url: newServerUrl(username, servername, xsiType, itemId, projectId, eventTrackingId),
            method: 'POST',
            contentType: 'application/json',
            beforeSend: function () {
                XNAT.app.activityTab.start('Start Jupyter Notebook Server', eventTrackingId, 'XNAT.plugin.jupyterhub.servers.activityTabCallback', 2000);
            },
            fail: function (error) {
                console.error(`Failed to send : ${error}`)
            }
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
        return {succeeded: succeeded, lastProgressIdx: lastProgressIdx};
    }

    function parseFinalMessage(message, succeeded) {
        if (succeeded) {
            // Surround link to Jupyter server in message with <a> tag
            return '<div class="prog success">' + message.replace(/(\/.*$)/, "<a target='_blank' href='$1'>$1</a>") + '</div>';
        } else {
            return '<div class="prog error">' + message + '</div>'
        }
    }

    let stopServer = XNAT.plugin.jupyterhub.servers.stopServer = function(username = window.username,
                                                                          servername = '',
                                                                          eventTrackingId = generateEventTrackingId()) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.stopServer`);

        return XNAT.xhr.ajax({
            url: serverUrl(username, servername, eventTrackingId),
            data : {
                "eventTrackingId": eventTrackingId
            },
            method: 'DELETE',
            beforeSend: function() {
                XNAT.app.activityTab.start('Stop Jupyter Notebook Server',  eventTrackingId, 'XNAT.plugin.jupyterhub.servers.activityTabCallback', 2000);
            },
            success: function () {
                console.log(`Jupyter server ${servername} for user ${username} stopped`);
            },
            fail: function (e) {
                XNAT.dialog.open({
                    width: 450,
                    title: "Failed to stop Jupyter Server",
                    buttons: [
                        {
                            label: 'OK',
                            isDefault: true,
                            close: true,
                            action: function() {
                                XNAT.ui.dialog.closeAll();
                            }
                        }
                    ]
                });
            }
        });
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