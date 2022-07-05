/*!
 * JupyterHub server functions
 */

console.debug('jupyterhub-servers.js');

var XNAT = getObject(XNAT || {});
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

    let newServerUrl = XNAT.plugin.jupyterhub.servers.newServerUrl = function(username, servername, xsiType, id) {
        let url = `/xapi/jupyterhub/users/${username}/server/${xsiType}/${id}`;
        return restUrl(url);
    }

    let serverUrl = XNAT.plugin.jupyterhub.servers.serverUrl = function(username, servername) {
        let url = `/xapi/jupyterhub/users/${username}/server/${servername}`;
        return restUrl(url);
    }

    XNAT.plugin.jupyterhub.servers.startServerForProject = function(username = window.username,
                                                            servername = XNAT.data.context.projectID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            id = XNAT.data.context.projectID) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForProject`);
        startServer(username, servername, xsiType, id)
    }

    XNAT.plugin.jupyterhub.servers.startServerForSubject = function(username = window.username,
                                                            servername = XNAT.data.context.ID,
                                                            xsiType = XNAT.data.context.xsiType,
                                                            id = XNAT.data.context.ID) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForSubject`);
        startServer(username, servername, xsiType, id)
    }

    XNAT.plugin.jupyterhub.servers.startServerForExperiment = function(username = window.username,
                                                               servername = XNAT.data.context.ID,
                                                               xsiType = "xnat:experimentData",
                                                               id = XNAT.data.context.ID) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForExperiment`);
        startServer(username, servername, xsiType, id)
    }

    XNAT.plugin.jupyterhub.servers.startServerForStoredSearch = function(username = window.username,
                                                                 servername = '', // TODO this is not in XNAT.data.context.ID
                                                                 xsiType = "xdat:stored_search",
                                                                 id = '') { // TODO this is not in XNAT.data.context.ID
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServerForStoredSearch`);
        startServer(username, servername, xsiType, id)
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

    let startServer = XNAT.plugin.jupyterhub.servers.startServer = function(username, servername, xsiType, id) {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.startServer`);
        console.log(`Initiating jupyter server. User: ${username}, Server Name: ${servername}, XSI Type: ${xsiType}, ID: ${id}`);

        XNAT.ui.dialog.static.wait("Starting Jupyter Server", {id: 'start_jupyter_server'})

        XNAT.xhr.ajax({
            url: newServerUrl(username, servername, xsiType, id),
            method: 'POST',
            contentType: 'application/json',
            success: function (serverUrl) {
                console.log(`Jupyter server is available at: ${serverUrl}`);
                window.open(serverUrl, '_blank');
                XNAT.ui.dialog.close('start_jupyter_server');
            },
            fail: function (error) {
                if (error.status === 409) { // Resource / server already exists
                    XNAT.ui.dialog.close('start_jupyter_server');
                    XNAT.dialog.open({
                        width: 450,
                        title: "Failed to launch Jupyter Server",
                        content: "A Jupyter Server is already running. Please shutdown the running instance before launching a new Jupyter Server.",
                        buttons: [
                            {
                                label: 'OK',
                                isDefault: true,
                                close: true,
                            }
                        ]
                    });
                } else {
                    handleServerError(error, "Failed to launch Jupyter Server", true);
                    XNAT.ui.dialog.close('start_jupyter_server');
                }
            }
        })
    }

    let stopServer = XNAT.plugin.jupyterhub.servers.stopServer = function(username = window.username,
                                                                          servername = '') {
        console.debug(`jupyterhub-servers.js: XNAT.plugin.jupyterhub.servers.stopServer`);

        return XNAT.xhr.ajax({
            url: serverUrl(username, servername),
            method: 'DELETE',
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

}));