package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initializes the default dashboard frameworks (Panel, Streamlit, Voilà, Dash).
 */
@Component
@Slf4j
public class DashboardFrameworkInitializer extends AbstractInitializingTask {

    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final DashboardFrameworkService dashboardFrameworkService;

    @Autowired
    public DashboardFrameworkInitializer(final XFTManagerHelper xftManagerHelper,
                                         final XnatAppInfo appInfo,
                                         final DashboardFrameworkService dashboardFrameworkService) {
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.dashboardFrameworkService = dashboardFrameworkService;
    }

    /**
     * Returns the name of the task.
     * @return The name of the task.
     */
    @Override
    public String getTaskName() {
        return "DashboardFrameworkInitializer";
    }

    /**
     * Creates the default dashboard frameworks (Panel, Streamlit, Voilà, Dash) if they do not already exist.
     * @throws InitializingTaskException When the XFTManagerHelper or XnatAppInfo is not initialized.
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
        log.info("Initializing default dashboard framework.");

        if (!xftManagerHelper.isInitialized() || !appInfo.isInitialized()) {
            log.debug("XFTManagerHelper or XnatAppInfo is not initialized, skipping creation.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        final DashboardFramework panel, streamlit, voila, dash;

        panel = DashboardFramework.builder()
                                  .name("Panel")
                                  .commandTemplate(
                                     "jhsingle-native-proxy " +
                                             "--port 8888 " +
                                             "--destport 5006 " +
                                             "--repo {repo} " +
                                             "--repobranch {repobranch} " +
                                             "--repofolder /home/jovyan/dashboards " +
                                             "bokeh-root-cmd /home/jovyan/dashboards/{mainFilePath} " +
                                                 "{--}port={port} " +
                                                 "{--}allow-websocket-origin={origin_host} " +
                                                 "{--}prefix={base_url} " +
                                                 "{--}server=panel"
                                  ).build();

        streamlit = DashboardFramework.builder()
                                      .name("Streamlit")
                                      .commandTemplate(
                                          "jhsingle-native-proxy " +
                                                  "--port 8888 " +
                                                  "--destport 8501 " +
                                                  "--repo {repo} " +
                                                  "--repobranch {repobranch} " +
                                                  "--repofolder /home/jovyan/dashboards " +
                                                  "streamlit run /home/jovyan/dashboards/{mainFilePath} " +
                                                      "{--}server.port {port} " +
                                                      "{--}server.headless True " +
                                                      "{--}server.fileWatcherType none"
                                      ).build();

        voila = DashboardFramework.builder()
                                  .name("Voila")
                                  .commandTemplate(
                                        "jhsingle-native-proxy " +
                                            "--port 8888 " +
                                            "--destport 0 " +
                                            "--repo {repo} " +
                                            "--repobranch {repobranch} " +
                                            "--repofolder /home/jovyan/dashboards " +
                                            "voila /home/jovyan/dashboards/{mainFilePath} " +
                                                "{--}port {port} " +
                                                "{--}no-browser " +
                                                "{--}Voila.base_url={base_url}/ " +
                                                "{--}Voila.server_url=/ " +
                                                "{--}Voila.ip=0.0.0.0 " +
                                                "{--}Voila.tornado_settings allow_origin={origin_host} " +
                                                "--progressive"
                                  ).build();

        dash = DashboardFramework.builder()
                                 .name("Dash")
                                 .commandTemplate(
                                    "jhsingle-native-proxy " +
                                            "--port=8888 " +
                                            "--destport=8050 " +
                                            "--repo={repo} " +
                                            "--repobranch={repobranch} " +
                                            "--repofolder=/home/jovyan/dashboards " +
                                            "plotlydash-tornado-cmd /home/jovyan/dashboards/{mainFilePath} " +
                                                "{--}port={port} " +
                                                "{--}ip 0.0.0.0"
                                 ).build();

        commitDashboardFramework(panel);
        commitDashboardFramework(streamlit);
        commitDashboardFramework(voila);
        commitDashboardFramework(dash);
    }

    /**
     * Commits the dashboard framework to the database.
     * @param framework The dashboard framework to commit.
     */
    protected void commitDashboardFramework(DashboardFramework framework) {
        try {
            if (!dashboardFrameworkService.get(framework.getName()).isPresent()) {
                dashboardFrameworkService.create(framework);
            } else {
                log.info("Dashboard framework {} already exists, skipping creation.", framework.getName());
            }
        } catch (Exception e) {
            log.error("Error creating default dashboard framework {}", framework.getName(), e);
        }
    }

}
