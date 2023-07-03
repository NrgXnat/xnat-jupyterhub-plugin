/*!
 * JupyterHub user authorization functions
 */

console.debug('jupyterhub-user-auth.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.users = getObject(XNAT.plugin.jupyterhub.users || {});
XNAT.plugin.jupyterhub.users.authorization = getObject(XNAT.plugin.jupyterhub.users.authorization || {});
XNAT.plugin.jupyterhub.users.authorization.roles = getObject(XNAT.plugin.jupyterhub.users.authorization.roles || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.plugin.jupyterhub.users.authorization.roles.jupyter = 'Jupyter';

    XNAT.plugin.jupyterhub.users.authorization.isAuthorized = async function (username = window.username) {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.isAuthorized`);

        if (username === 'guest') {
            return false;
        }

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users/${username}/roles`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting user roles for user ${username}`);
        }

        let roles = await response.json();

        if (roles.contains(XNAT.plugin.jupyterhub.users.authorization.roles.jupyter)) {
            return true;
        }

        let preferences = await XNAT.plugin.jupyterhub.preferences.getAll();
        return preferences['allUsersCanStartJupyter'];
    }

    XNAT.plugin.jupyterhub.users.authorization.getAuthorized = async function () {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.getAuthorized`);

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users/roles/${XNAT.plugin.jupyterhub.users.authorization.roles.jupyter}`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting user roles for user ${username}`);
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.users.authorization.getUnauthorized = async function () {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.getUnauthorized`);

        let authorizedUsers = await XNAT.plugin.jupyterhub.users.authorization.getAuthorized();

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users`, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting user roles for user ${username}`);
        }

        let allUsers = await response.json();

        return allUsers.filter(user => !authorizedUsers.contains(user) && user !== 'jupyterhub' && user !== 'guest');
    }

    XNAT.plugin.jupyterhub.users.authorization.add = async function (username) {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.add`);

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users/${username}/roles`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify([XNAT.plugin.jupyterhub.users.authorization.roles.jupyter]),
        })

        if (!response.ok) {
            throw new Error(`Error adding jupyter role to user ${username}`);
        }
    }

    XNAT.plugin.jupyterhub.users.authorization.remove = async function (username) {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.remove`);

        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(`/xapi/users/${username}/roles`, {
            method: 'DELETE',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify([XNAT.plugin.jupyterhub.users.authorization.roles.jupyter]),
        })

        if (!response.ok) {
            throw new Error(`Error adding jupyter role to user ${username}`);
        }
    }

    XNAT.plugin.jupyterhub.users.authorization.refreshTable = function (containerId) {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.refreshTable`);

        const containerEl = document.getElementById(containerId);
        containerEl.innerHTML = '';

        function removeButton(user) {
            return spawn('button.btn.sm.delete', {
                onclick: function () {
                    XNAT.plugin.jupyterhub.users.authorization.remove(user).then(() => {
                        XNAT.ui.banner.top(1500, '<b>' + user + '</b> removed from Jupyter users list.', 'success');
                        XNAT.plugin.jupyterhub.users.authorization.refreshTable(containerId);
                    }).catch(() => {
                        XNAT.ui.banner.top(2000, 'Failed to remove <b>' + user + '</b> from Jupyter users list.', 'error');
                        XNAT.plugin.jupyterhub.users.authorization.refreshTable(containerId);
                    });
                },
                title: "Remove User",
            }, [spawn('i.fa.fa-ban')]);
        }

        XNAT.plugin.jupyterhub.users.authorization.getAuthorized().then(users => {
            users.sort();

            const noUsers = users.length === 0;
            if (noUsers) {
                users.push('No users have been authorized.');
            }

            XNAT.table.dataTable(users, {
                header: true,
                sortable: 'user',
                columns: {
                    user: {
                        label: 'User',
                        apply: function () {
                            return this.toString();
                        }
                    },
                    actions: {
                        label: 'Actions',
                        th: {style: {width: '150px'}},
                        apply: function () {
                            return noUsers ? '' : spawn('div.center', [removeButton(this.toString())]);
                        }
                    }
                }
            }).render(`#${containerId}`)
        })
    }

    XNAT.plugin.jupyterhub.users.authorization.initTable = function (containerId) {
        console.debug(`XNAT.plugin.jupyterhub.users.authorization.initTable`);

        const containerEl = document.getElementById(containerId);
        const allowAllEl = document.getElementById('allowAll');
        const hrEl = document.getElementById('jupyter-user-auth-hr');

        // Place 'Add User' button in the panel footer
        let footerEl = containerEl.parentElement.parentElement.querySelector(".panel-footer")

        const button = spawn('button.btn.btn-sm', {
            html: 'Add User',
            style: {
                display: 'none'
            },
            id: 'add-jupyter-user-btn',
            onclick: function () {
                XNAT.dialog.open({
                    title: 'Add Jupyter Users',
                    content: spawn('form'),
                    width: 600,
                    beforeShow: function (obj) {
                        const formContainer$ = obj.$modal.find('.xnat-dialog-content');
                        formContainer$.addClass('panel');
                        XNAT.plugin.jupyterhub.users.authorization.getUnauthorized().then(users => {
                            obj.$modal.find('form').append(
                                spawn('!', [
                                    XNAT.ui.panel.select.multiple({
                                        name: 'jupyter-users-to-authorize',
                                        id: 'jupyter-users-to-authorize',
                                        label: 'Select Users',
                                        options: users
                                    }).element
                                ])
                            );

                            const selectEl = document.getElementById('jupyter-users-to-authorize');
                            selectEl.style.minWidth = '175px';
                            selectEl.style.minHeight = '125px';
                        })
                    },
                    buttons: [
                        {
                            label: 'Submit',
                            isDefault: true,
                            close: false,
                            action: function () {
                                const newUsersEl = document.getElementById("jupyter-users-to-authorize");
                                const newUsers = []

                                for (let option of newUsersEl.options) {
                                    if (option.selected) {
                                        newUsers.push(option.value);
                                    }
                                }

                                Promise.allSettled(newUsers.map(user => {
                                    XNAT.plugin.jupyterhub.users.authorization.add(user);
                                })).then(results => {
                                    let bannerMessage, bannerClass;
                                    let fulfilled = results.map(result => result.status).filter(status => status === 'fulfilled');

                                    if (fulfilled.length === newUsers.length) {
                                        if (newUsers.length === 1) {
                                            bannerMessage = '<b>' + newUsers[0] + '</b> added to Jupyter users list.';
                                            bannerClass = 'success';
                                        } else if (newUsers.length > 1) {
                                            bannerMessage = '<b>' + newUsers.length + ' users</b> added to Jupyter users list.';
                                            bannerClass = 'success';
                                        }
                                    } else {
                                        bannerMessage = '<b>' + fulfilled.length + '/' + newUsers.length + ' users</b> added to Jupyter users list.'
                                        bannerClass = 'warning';
                                    }

                                    XNAT.ui.banner.top(2000, bannerMessage, bannerClass);
                                    xmodal.closeAll();
                                    XNAT.ui.dialog.closeAll();
                                }).then(() => {
                                    // Table refresh is delayed to allow time for the user authorization to be added
                                    setTimeout(() => {
                                        XNAT.plugin.jupyterhub.users.authorization.refreshTable(containerId);
                                    }, 500);
                                })
                            }
                        },
                        {
                            label: 'Cancel',
                            close: true
                        }
                    ]
                })
            }
        });

        footerEl.append(spawn('div.pull-right', [button]));
        footerEl.append(spawn('div.clear.clearFix'));

        XNAT.plugin.jupyterhub.preferences.get('allUsersCanStartJupyter')
            .then(preference => {
                // Set allow all check box
                allowAllEl.checked = preference;

                if (!allowAllEl.checked) {
                    button.style.display = '';
                    hrEl.style.display = '';
                    XNAT.plugin.jupyterhub.users.authorization.refreshTable(containerId);
                }

                // Only display user table when all users are not enabled
                allowAllEl.addEventListener('change', () => {
                    XNAT.plugin.jupyterhub.preferences.set('allUsersCanStartJupyter', allowAllEl.checked);

                    if (allowAllEl.checked) {
                        containerEl.innerHTML = '';
                        button.style.display = 'none';
                        hrEl.style.display = 'none';
                    } else {
                        button.style.display = '';
                        hrEl.style.display = '';
                        XNAT.plugin.jupyterhub.users.authorization.refreshTable(containerId);
                    }
                });
            })
    }
}));