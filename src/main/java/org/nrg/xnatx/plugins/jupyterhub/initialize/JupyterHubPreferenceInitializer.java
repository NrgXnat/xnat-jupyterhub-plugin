package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.SystemHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used to initialize JupyterHub plugin preferences.
 */
@Component
@Slf4j
public class JupyterHubPreferenceInitializer extends AbstractInitializingTask {

    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final JupyterHubPreferences jupyterHubPreferences;
    private final SiteConfigPreferences siteConfigPreferences;
    private final SystemHelper systemHelper;

    @Autowired
    public JupyterHubPreferenceInitializer(final XFTManagerHelper xftManagerHelper,
                                           final XnatAppInfo appInfo,
                                           final JupyterHubPreferences jupyterHubPreferences,
                                           final SiteConfigPreferences siteConfigPreferences,
                                           final SystemHelper systemHelper) {
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.siteConfigPreferences = siteConfigPreferences;
        this.systemHelper = systemHelper;
    }

    @Override
    public String getTaskName() {
        return "JupyterHubPreferenceInitializer";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        log.info("Initializing JupyterHub preferences.");

        if (!xftManagerHelper.isInitialized()) {
            log.info("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!appInfo.isInitialized()) {
            log.info("XNAT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        // Initialize the JupyterHub preferences.
        initializeJupyterHubHostUrl();
    }

    /**
     * Initialize the JupyterHub Host URL preference.
     * If the JupyterHub Host URL preference is not set, check for the JH_HOST_URL environment variable. If the
     * environment variable is not set, default to the XNAT site url.
     */
    protected void initializeJupyterHubHostUrl() {
        // Check if the JupyterHub host url preference is set.
        if (jupyterHubPreferences.getJupyterHubHostUrl() == null || jupyterHubPreferences.getJupyterHubHostUrl().isEmpty()) {
            // Check for env variable or default to site url.
            String jupyterHubHostUrl = systemHelper.getEnv("JH_HOST_URL");
            if (jupyterHubHostUrl == null || jupyterHubHostUrl.isEmpty()) {
                jupyterHubHostUrl = siteConfigPreferences.getSiteUrl();
            }
            jupyterHubPreferences.setJupyterHubHostUrl(jupyterHubHostUrl);
            log.info("JupyterHub host url initialized to: " + jupyterHubHostUrl);
        } else {
            log.info("JupyterHub host url already set. Skipping initialization.");
        }
    }
}
