/*!
 * JupyterHub TopNav functions
 */

console.debug('jupyterhub-topnav.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.jupyterhub = getObject(XNAT.plugin.jupyterhub || {});
XNAT.plugin.jupyterhub.topnav = getObject(XNAT.plugin.jupyterhub.topnav || {});

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

    let getSubjectLabel = async function(subjectId) {
        const response = await fetch(XNAT.url.restUrl(`/data/subjects/${subjectId}`), {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (response.ok) {
            let result = await response.json()
            return result['items'][0]['data_fields']['label']
        }
    }

    XNAT.plugin.jupyterhub.topnav.refresh = function(containerId = 'my-jupyter-servers') {
        console.debug(`jupyterhub-topnav.js: XNAT.plugin.jupyterhub.topnav.refresh`);

        // initialize the table
        const jupyterServerTable = XNAT.table({
            className: 'data-table xnat-table no-body',
            style: {
                width: '300px',
                tableLayout: 'fixed'
            }
        })

        // add table header row
        jupyterServerTable.tr()
            .th({addClass: 'left', style: {width: '200px'}, html: '<b>Item</b>'})
            .th({addClass: 'center', style: {width: '100px'}, html: '<b>Actions</b>'})

        function xnatItem(server) {
            const userOptions = server['user_options'];
            const xsiType = userOptions['xsiType'];
            const itemId = userOptions['itemId'];

            switch(xsiType) {
                case 'xnat:projectData':
                    return spawn('a', {
                        href: restUrl(`/data/projects/${itemId}`),
                        title: itemId,
                        html: itemId,
                    });
                case 'xnat:subjectData':
                    return spawn('a', {
                        href: restUrl(`/data/subjects/${itemId}`),
                        title: itemId,
                        html: itemId,
                    });
                case 'xnat:experimentData':
                    return spawn('a', {
                        href: restUrl(`/data/experiments/${itemId}`),
                        title: itemId,
                        html: itemId,
                    });
                case 'xdat:stored_search':
                    if (itemId.startsWith('@')) {// Site wide data bundle
                        return spawn('a', {
                            href: restUrl(`/app/template/Search.vm/node/d.${itemId.substring(1)}`),
                            title: itemId,
                            html: itemId,
                        });
                    } else if (itemId.includes('@')) { // Project data bundle
                        let project = itemId.substring(0, itemId.indexOf('@'))
                        return spawn('a', {
                            href: restUrl(`/data/projects/${project}`),
                            title: itemId,
                            html: itemId,
                        });
                    } else { // Stored search
                        return spawn('a', {
                            href: restUrl(`/app/template/Search.vm/node/ss.${itemId}`),
                            title: itemId,
                            html: itemId,
                        });
                    }
            }
        }

        function gotoServerButton(server) {
            return spawn('button.btn.sm', {
                onclick: function(e) {
                    e.preventDefault();
                    window.open('/jupyterhub', '_blank')
                }
            }, [ spawn('i.fa.fa-sign-in') ])
        }

        function stopServerButton(server) {
            return spawn('button.btn.sm', {
                onclick: function(e) {
                    e.preventDefault();
                    const eventTrackingId = XNAT.plugin.jupyterhub.servers.generateEventTrackingId()
                    XNAT.plugin.jupyterhub.servers.stopServer(window.username, server['name'], eventTrackingId).then(() => {
                        XNAT.plugin.jupyterhub.topnav.refresh();
                    });

                }
            }, [ spawn('i.fa.fa-trash') ])
        }

        function spacer(width = 10) {
            return spawn('i.spacer', {
                style: {
                    display: 'inline-block',
                    width: width + 'px'
                }
            })
        }

        XNAT.plugin.jupyterhub.users.getUser().then(user => {
            let servers = user['servers'];

            Object.values(servers).forEach(server => {
                jupyterServerTable.tr()
                    .td([ spawn('div.left', [xnatItem(server)]) ])
                    .td([ spawn('div.center', [gotoServerButton(server), spacer(), stopServerButton(server)]) ])
            })

            let tableWrapper = spawn('div.data-table-wrapper no-body', {
                style: {
                    'height': 'auto',
                    'min-height': 'auto',
                    'width': '100%',
                    'overflow-y': 'auto',
                }
            })

            tableWrapper.append(jupyterServerTable.table)

            const menuWrapper = spawn('li.table-list', [tableWrapper])
            let containerEl = document.getElementById(containerId);
            containerEl.innerHTML = "";
            containerEl.appendChild(menuWrapper)
        })
    }

    let topnavEl = document.getElementById("jupyter-topnav");
    topnavEl.addEventListener("click", () => XNAT.plugin.jupyterhub.topnav.refresh());

}));