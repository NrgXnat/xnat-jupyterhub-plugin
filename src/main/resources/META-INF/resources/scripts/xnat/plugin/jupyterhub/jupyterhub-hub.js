/*!
 * JupyterHub Plugin - Hub functions
 */

console.debug('jupyterhub-hub.js');

var XNAT = getObject(XNAT || {});
XNAT.app = getObject(XNAT.app || {});
XNAT.app.activityTab = getObject(XNAT.app.activityTab || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.hub = getObject(XNAT.plugin.jupyterhub.hub || {});

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

    XNAT.plugin.jupyterhub.hub.infoUrl = function() {
        return XNAT.url.restUrl(`/xapi/jupyterhub/info`)
    }

    XNAT.plugin.jupyterhub.hub.versionUrl = function() {
        return XNAT.url.restUrl(`/xapi/jupyterhub/version`)
    }

    XNAT.plugin.jupyterhub.hub.getInfo = async function() {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.getInfo`);

        const response = await fetch(XNAT.plugin.jupyterhub.hub.infoUrl(), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            console.error(`HTTP error getting JupyterHub site info: ${response.status}`);
            return {}
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.hub.getVersion = async function() {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.getVersion`);

        const response = await fetch(XNAT.plugin.jupyterhub.hub.versionUrl(), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error(`HTTP error getting JupyterHub version: ${response.status}`);
        }

        return await response.json();
    }

    XNAT.plugin.jupyterhub.hub.renderSetupForm = function(container_id) {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.setupForm`);

        XNAT.spawner
            .resolve('jupyterhub:siteSettings/jupyterhubPreferences')
            .ok(function(){ this.render(`#${container_id}`) });
    }

    XNAT.plugin.jupyterhub.hub.setupTable = function(hubSetupContainerId) {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.setupTable`);

        // initialize the table
        const hubTable = XNAT.table({
            className: 'hub xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        })

        // add table header row
        hubTable.tr()
            .th({addClass: 'left', html: '<b>API Path</b>'})
            .th('<b>Status</b>')
            .th('<b>Version</b>')
            .th('<b>Actions</b>')

        function editButton() {
            return spawn('button.btn.sm.edit', {
                onclick: function(e) {
                    e.preventDefault();
                    let dialog = XNAT.dialog.open({
                        title: '',
                        content: spawn('div#jupyterhub-setup-form'),
                        maxBtn: true,
                        footer: false,
                        width: 750,
                        beforeShow: function(obj) {
                            console.log(obj)
                            let serverFormContainerEl = document.getElementById(`jupyterhub-setup-form`);
                            serverFormContainerEl.innerHTML = '';
                            XNAT.plugin.jupyterhub.hub.renderSetupForm(`jupyterhub-setup-form`);
                        },
                        buttons: []
                    })
                    let closeButton = dialog.$modal[0].querySelector('b.close');
                    closeButton.addEventListener("click", () => XNAT.plugin.jupyterhub.hub.refreshSetupTable(hubSetupContainerId))
                }
            }, 'Edit');
        }

        // TODO Break into two
        Promise.all([
            XNAT.plugin.jupyterhub.hub.getInfo(),
            XNAT.plugin.jupyterhub.preferences.getAll()
        ]).then(([hubInfo, hubPreferences]) => {
            let status = 'version' in hubInfo;
            hubTable.tr()
                .td([spawn('div.left', [hubPreferences['jupyterHubApiUrl']])])
                .td([spawn(status ? 'div.center.success' : 'div.center.warning', [status ? 'OK' : 'Down'])])
                .td([spawn('div.center', [hubInfo['version']])])
                .td([spawn('div.center', [editButton()])]);
        }).catch(e => {
            console.error("Error fetching hub info and preferences", e);

            hubTable.tr()
                .td([spawn('div.left', ['Unable to fetch JupyterHub setup preferences'])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [])])
                .td([spawn('div.center', [editButton()])]);
        })

        return hubTable.table;
    }

    XNAT.plugin.jupyterhub.hub.refreshSetupTable = function(hubSetupContainerId) {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.refreshSetupTable`);

        // Create hub setup table
        let hubSetupTable = XNAT.plugin.jupyterhub.hub.setupTable(hubSetupContainerId)

        // Clear container and insert activity table
        let containerEl = document.getElementById(hubSetupContainerId);
        if (containerEl && hubSetupTable) {
            containerEl.innerHTML = "";
            containerEl.append(hubSetupTable);
        }
    }

    XNAT.plugin.jupyterhub.hub.initSetupTable = function(hubSetupContainerId = 'jupyterhub-setup-table') {
        console.debug(`jupyterhub-hub.js: XNAT.plugin.jupyterhub.hub.initSetupTable`);

        XNAT.plugin.jupyterhub.hub.refreshSetupTable(hubSetupContainerId);
    }

}));