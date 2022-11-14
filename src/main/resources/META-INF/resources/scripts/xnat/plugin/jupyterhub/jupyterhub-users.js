/*!
 * JupyterHub user functions
 */

console.debug('jupyterhub-users.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.users = getObject(XNAT.plugin.jupyterhub.users || {});
XNAT.plugin.jupyterhub.users.activity = getObject(XNAT.plugin.jupyterhub.users.activity || {});
XNAT.plugin.jupyterhub.users.tokens = getObject(XNAT.plugin.jupyterhub.users.tokens || {});

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

    let userUrl = XNAT.plugin.jupyterhub.userUrl = function(username = window.username) {
        let url = `/xapi/jupyterhub/users/${username}`
        return restUrl(url)
    }

    XNAT.plugin.jupyterhub.users.getUser = XNAT.plugin.jupyterhub.users.get = async function(username = window.username,
                                                                                             timeout  = 2000) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.getUser`);

        let url = userUrl(username);
        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
            timeout: timeout,
        })

        if (!response.ok) {
            if (response.status === 404) {
                // User does not exist on JupyterHub, create the user then try again.
                return XNAT.plugin.jupyterhub.users.createUser(username).then(() => XNAT.plugin.jupyterhub.users.getUser(username));
            }

            throw new Error(`HTTP error getting JupyterHub user ${username}: ${response.status}`);
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.users.getUsers = XNAT.plugin.jupyterhub.users.getAll = async function(timeout = 2000) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.getUsers`);

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(XNAT.url.restUrl(`/xapi/jupyterhub/users`), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
            timeout: timeout
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting JupyterHub users: ${response.status}`);
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.users.createUser = XNAT.plugin.jupyterhub.users.create = async function(username = window.username,
                                                                                                   timeout  = 2000) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.createUser`);

        let url = userUrl(username);
        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(url, {
            method: 'POST',
            timeout: timeout,
        })

        if (!response.ok) {
            throw new Error(`HTTP error creating JupyterHub user ${username}: ${response.status}`);
        }
    }

    XNAT.plugin.jupyterhub.users.activity.table = function(activityTableContainerId) {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.users.activity.table`);

        // initialize the table
        const usersTable = XNAT.table({
            className: 'users xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        usersTable.tr()
            .th({addClass: 'left', html: '<b>User</b>'})
            .th('<b>Admin</b>')
            .th('<b>Server</b>')
            .th('<b>Ready</b>')
            .th('<b>Started</b>')
            .th('<b>Last Activity</b>')
            .th('<b>Actions</b>')

        // Single Server Details Dialog
        function serverDialog(username, server) {
            // initialize the server table
            const serverTable = XNAT.table({
                className: 'server-table xnat-table',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            })

            // JupyterHub server
            serverTable.tr()
                .td([spawn('b', 'server')])
                .td([ spawn('pre', {style: {'white-space': 'pre-wrap'}},  [spawn('code', [JSON.stringify(server, undefined, 2)])]) ])

            // link onclick -> server details dialog
            return spawn('a.link|href=#!', {
                onclick: function (e) {
                    e.preventDefault();

                    // XNAT user_options
                    XNAT.plugin.jupyterhub.servers.user_options.get(username, server['name']).then(user_options => {
                        serverTable.tr()
                            .td([spawn('b', 'user_options')])
                            .td([ spawn('pre', {style: {'white-space': 'pre-wrap'}},  [spawn('code', [JSON.stringify(user_options, undefined, 2)])]) ])
                    }).catch(e => {
                        console.error("Error fetching user_options: ", e);
                    })

                    XNAT.dialog.open({
                        title: 'Server',
                        content: spawn('div#server-table-container'),
                        maxBtn: true,
                        width: 750,
                        beforeShow: function() {
                            let serverTableContainerEl = document.getElementById("server-table-container");
                            serverTableContainerEl.innerHTML = '';
                            serverTableContainerEl.append(serverTable.table);
                        },
                        buttons: [
                            {
                                label: 'OK',
                                isDefault: true,
                                close: true,
                            }
                        ]
                    })
                }
            }, 'Details')
        }

        function stopServerButton(username, servername) {
            return spawn('button.btn.sm.delete', {
                onclick: function() {
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            "<p>Are you sure you'd like to stop this Jupyer server?</p>" +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            const eventTrackingId = XNAT.plugin.jupyterhub.servers.generateEventTrackingId()
                            XNAT.plugin.jupyterhub.servers.stopServer(username, servername, eventTrackingId).then(() => {
                                const delay = (time) => new Promise(resolve => setTimeout(resolve, time));
                                delay(500).then(() => XNAT.plugin.jupyterhub.users.activity.refresh(activityTableContainerId));
                            }).catch(error => {
                                console.error(error);
                                XNAT.dialog.alert(`Failed to stop Jupyter server: ${error}`)
                            });
                        }
                    })
                }
            }, 'Stop Server');
        }

        XNAT.plugin.jupyterhub.users.getUsers().then(users => {
            users.forEach(user => {
                let name = user['name'];
                let admin = user['admin'] ? 'admin' : '';
                let servers = user['servers'];
                let hasServer = '' in servers;
                let url = hasServer ? servers['']['url'] : '';
                let ready = hasServer ? servers['']['ready'] : '';
                let started = hasServer ? new Date(servers['']['started']) : '';
                let lastActivity = hasServer ? new Date(servers['']['last_activity']) : '';

                usersTable.tr()
                    .td([spawn('div.left', [name])])
                    .td([spawn('div.center', [admin])])
                    .td([spawn('div.center', [hasServer ? serverDialog(user['name'], servers['']) : ''])])
                    .td([spawn('div.center', [ready])])
                    .td([spawn('div.center', [started.toLocaleString()])])
                    .td([spawn('div.center', [lastActivity.toLocaleString()])])
                    .td([spawn('div.center', [hasServer ? stopServerButton(name, '') : ''])]);
            })
        }).catch(e => {
            console.error("Unable to fetch user activity.", e);

            usersTable.tr()
                .td([spawn('div.left', ["Unable to connect to JupyterHub"])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])]);
        })

        return usersTable.table;
    }

    XNAT.plugin.jupyterhub.users.activity.refresh = function(activityTableContainerId) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.activity.refresh`);

        // Create activity table
        let activityTable = XNAT.plugin.jupyterhub.users.activity.table(activityTableContainerId)

        // Clear container and insert activity table
        let containerEl = document.getElementById(activityTableContainerId);
        if (containerEl && activityTable) {
            containerEl.innerHTML = "";
            containerEl.append(activityTable);
        }
    }

    XNAT.plugin.jupyterhub.users.activity.init = function(activityTableContainerId = 'jupyterhub-user-activity-table') {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.activity.init`);

        let containerEl = document.getElementById(activityTableContainerId);
        let footerEl = containerEl.parentElement.parentElement.querySelector(".panel-footer")

        XNAT.plugin.jupyterhub.users.activity.refresh(activityTableContainerId);

        const refreshButton = spawn('button.btn.btn-sm', {
            html: 'Refresh',
            onclick: function() {
                XNAT.plugin.jupyterhub.users.activity.refresh(activityTableContainerId)
            }
        });

        // add the 'refresh' button to the panel footer
        footerEl.append(spawn('div.pull-right', [refreshButton]));
        footerEl.append(spawn('div.clear.clearFix'));
    }

    XNAT.plugin.jupyterhub.users.tokens.create = async function(token = {
                                                                    username: window.username,
                                                                    note: "XNAT.plugin.jupyterhub.users.tokens.create",
                                                                    expires_in: 172800 // 2 days in seconds
                                                                },
                                                                timeout = 375) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.tokens.create`);

        let username = token['username'];
        let expires_in = token['expires_in'];
        let note = token['note'];

        let resource = XNAT.url.restUrl(`/xapi/jupyterhub/users/${username}/tokens?username=${username}&expiresIn=${expires_in}&note=${note}`);
        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(resource, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(token),
            timeout: timeout
        })

        if (!response.ok) {
            throw new Error(`HTTP error creating token for user ${username}: ${response.status}`);
        }

        return await response.json();
    }
}));