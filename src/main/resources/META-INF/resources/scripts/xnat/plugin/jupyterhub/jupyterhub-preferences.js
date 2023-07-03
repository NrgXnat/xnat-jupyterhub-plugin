/*!
 * JupyterHub preference functions
 */

console.debug('jupyterhub-preferences.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.preferences = getObject(XNAT.plugin.jupyterhub.preferences || {});

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

    XNAT.plugin.jupyterhub.preferences.getAll = async function() {
        console.debug(`jupyterhub-preferences.js: XNAT.plugin.jupyterhub.preferences.getAll`);

        let url = XNAT.url.restUrl('/xapi/jupyterhub/preferences');
        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'},
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting preferences: ${response.status}`);
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.preferences.get = async function(preference) {
        console.debug(`jupyterhub-preferences.js: XNAT.plugin.jupyterhub.preferences.get`);

        let preferences = await XNAT.plugin.jupyterhub.preferences.getAll();
        return preferences[preference];
    }

    XNAT.plugin.jupyterhub.preferences.set = async function(preference, value) {
        console.debug(`jupyterhub-preferences.js: XNAT.plugin.jupyterhub.preferences.set`);

        let url = XNAT.url.restUrl(`/xapi/jupyterhub/preferences/${preference}`);
        const response = await XNAT.plugin.jupyterhub.utils.fetchWithTimeout(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(value),
        })

        if (!response.ok) {
            throw new Error(`Error setting preference ${preference}`);
        }
    }

}))