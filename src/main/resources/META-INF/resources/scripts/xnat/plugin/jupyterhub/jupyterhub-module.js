/*!
 * JupyterHub Plugin Module
 * Contains commonly used functions
 */

console.debug('jupyterhub-module.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.utils = getObject(XNAT.plugin.jupyterhub.utils || {});

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

    XNAT.plugin.jupyterhub.utils.fetchWithTimeout = async function(resource, options = {}) {
        const { timeout = 8000 } = options;

        const controller = new AbortController();
        const id = setTimeout(() => controller.abort(), timeout);
        const response = await fetch(resource, {
            ...options,
            signal: controller.signal
        });
        clearTimeout(id);
        return response;
    }

    XNAT.plugin.jupyterhub.isSharedItem = function() {
        console.debug(`jupyterhub-module.js: XNAT.plugin.jupyterhub.isSharedItem`);
        return 'parentProjectID' in XNAT.data.context && XNAT.data.context['parentProjectID'] !== XNAT.data.context['projectID']
    };

}));