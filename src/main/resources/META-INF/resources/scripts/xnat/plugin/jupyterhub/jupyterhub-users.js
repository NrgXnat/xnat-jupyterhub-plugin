/*!
 * JupyterHub user functions
 */

console.debug('jupyterhub-users.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.users = getObject(XNAT.plugin.jupyterhub.users || {});

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

    XNAT.plugin.jupyterhub.users.getUser = XNAT.plugin.jupyterhub.users.get = function(username = window.username) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.getUser`);

        return XNAT.xhr.ajax({
            url: userUrl(username),
            method: 'GET',
            contentType: 'application/json',
            success: function (user) {
                console.debug(`JupyterHub user ${username} exists.`);
                return user;
            },
            fail: function (e) {
                console.error(`User ${username} does not exist on JupyterHub.`);
                return e;
            }
        })
    }

    XNAT.plugin.jupyterhub.users.createUser = XNAT.plugin.jupyterhub.users.create = function(username = window.username) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.createUser`);

        return XNAT.xhr.ajax({
            url: userUrl(username),
            method: 'POST',
            contentType: 'application/json',
            success: function (user) {
                console.debug(`JupyterHub user ${username} created.`);
                return user;
            },
            fail: function (e) {
                console.error(`Unable to create JupyterHub user ${username}`);
                return e;
            }
        })
    }

    XNAT.plugin.jupyterhub.users.init = function(username = window.username) {
        console.debug(`jupyterhub-users.js: XNAT.plugin.jupyterhub.users.init`);

        XNAT.plugin.jupyterhub.users.getUser(username).then(user => {
            console.debug(`User ${username} exists on JupyterHub`)
            XNAT.plugin.jupyterhub.users.isEnabled = true;
        }).catch(() => {
            console.debug(`User ${username} does not exists on JupyterHub. Trying to create user.`)

            XNAT.plugin.jupyterhub.users.createUser(username).then(() => {
                XNAT.plugin.jupyterhub.users.isEnabled = true;
            }).catch(error => {
                console.error(error)
                XNAT.plugin.jupyterhub.users.isEnabled = false;
            })
        })
    }

    XNAT.plugin.jupyterhub.users.init();

}));