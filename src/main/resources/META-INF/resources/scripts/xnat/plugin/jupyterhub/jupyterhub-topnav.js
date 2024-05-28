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
            .th({addClass: 'left', style: {width: '75px'}, html: '<b>Application</b>'})
            .th({addClass: 'left', style: {width: '200px'}, html: '<b>Context</b>'})
            .th({addClass: 'center', style: {width: '100px'}, html: '<b>Actions</b>'})

        function application(server) {
            return server['user_options']['dashboardConfigId'] != null &&
                   server['user_options']['dashboardConfigId'] !== '' ? 'Dashboard' : 'Notebook';
        }

        function xnatItem(server) {
            const userOptions = server['user_options'];
            const xsiType = userOptions['xsiType'];
            const itemId = userOptions['itemId'];
            const itemLabel = userOptions['itemLabel'];
            const projectId = userOptions['projectId'];

            switch(xsiType) {
                case 'xnat:projectData':
                    return spawn('a', {
                        href: restUrl(`/data/projects/${itemId}`),
                        title: itemLabel,
                        html: itemLabel,
                    });
                case 'xnat:subjectData':
                    return spawn('a', {
                        href: restUrl(`/data/subjects/${itemId}`),
                        title: itemLabel,
                        html: itemLabel,
                    });
                case 'xdat:stored_search':
                    if (itemId.startsWith('@')) {
                        if (projectId == null) {// Site wide data bundle
                            return spawn('a', {
                                href: restUrl(`/app/template/Search.vm/node/d.${itemId.substring(1)}`),
                                title: itemLabel,
                                html: itemLabel,
                            });
                        } else { // Project search bundle
                            return spawn('a', {
                                href: restUrl(`/data/projects/${projectId}`),
                                title: itemLabel,
                                html: itemLabel,
                            });
                        }
                    } else { // Stored search
                        if (projectId == null) { // Site level stored search
                            return spawn('a', {
                                href: restUrl(`/app/template/Search.vm/node/ss.${itemId}`),
                                title: itemLabel,
                                html: itemLabel,
                            });
                        } else { // Project level stored search
                            return spawn('a', {
                                href: restUrl(`/data/projects/${projectId}`),
                                title: itemLabel,
                                html: itemLabel,
                            });
                        }
                    }
                // If not project, subject, or stored search, default to experiment
                case 'xnat:experimentData':
                default:
                    return spawn('a', {
                        href: restUrl(`/data/experiments/${itemId}?format=html`),
                        title: itemLabel,
                        html: itemLabel,
                    });
            }
        }

        function isDashboard(server) {
            return server?.user_options?.dashboardConfigId != null && server?.user_options?.dashboardConfigId !== '';
        }

        function gotoServerButton(server) {
            return spawn('button.btn.sm', {
                onclick: function(e) {
                    e.preventDefault();
                    XNAT.plugin.jupyterhub.servers.goTo(server['url'])
                }
            }, [ spawn(`i.fa.fa-external-link|title="Go to ${isDashboard(server) ? 'dashboard' : 'Jupyter Notebook Server'}"`) ])
        }

        function stopServerButton(server) {
            return spawn('button.btn.sm', {
                onclick: function(e) {
                    e.preventDefault();
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                            `<p>Are you sure you'd like to stop this ${isDashboard(server) ? 'dashboard' : 'Jupyter Notebook Server'}?</p>` +
                            "<p><b>This action cannot be undone.</b></p>",
                        okAction: function() {
                            const eventTrackingId = XNAT.plugin.jupyterhub.servers.generateEventTrackingId()
                            XNAT.plugin.jupyterhub.servers.stopServer(window.username, server['name'], eventTrackingId).then(() => {
                                XNAT.app.activityTab.start(
                                    `Stop Jupyter ${isDashboard(server) ? 'Dashboard' : 'Notebook'}`,
                                    eventTrackingId,
                                    'XNAT.plugin.jupyterhub.servers.activityTabCallback',
                                    2000);
                            }).catch(error => {
                                console.error(error);
                                XNAT.dialog.alert(`Failed to stop ${isDashboard(server) ? 'dashboard' : 'Jupyter Notebook Server'}.`);
                            });
                        }
                    })
                }
            }, [ spawn(`i.fa.fa-ban|title="Stop ${isDashboard(server) ? 'Dashboard' : 'Jupyter Notebook Server'}"`) ])
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

            const isEmpty = (obj) => Object.keys(obj).length === 0;

            isEmpty(servers) ?
                jupyterServerTable.tr()
                    .td({colSpan: '3'}, [ spawn('div.left', {style: {'font-size': '12px'}}, ['No running Jupyter notebooks or dashboards. Go to a project, subject, or experiment to start one.']) ]):
                Object.values(servers).forEach(server => {
                    jupyterServerTable.tr()
                        .td([ spawn('div.left', {style: {'font-size': '12px', 'font-weight': 'bold'}}, [application(server)]) ])
                        .td([ spawn('div.left', [xnatItem(server)]) ])
                        .td([ spawn('div.center', [gotoServerButton(server), spacer(), stopServerButton(server)]) ])
                });

        }).catch(() => {
            jupyterServerTable.tr()
                .td({colSpan: '3'}, [ spawn('div.left', {style: {'font-size': '12px'}}, ['Unable to connect to JupyterHub']) ])
                .td([ spawn('div.center', ['']) ]);
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
    }

    let topnavEl = document.getElementById("jupyter-topnav");
    topnavEl.addEventListener("click", () => XNAT.plugin.jupyterhub.topnav.refresh());

}));