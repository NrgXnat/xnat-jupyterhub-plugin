package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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

        // Initialize the JupyterHub plugin preferences.
        initializeJupyterHubHostUrl();
        initializeJupyterHubApiUrlFromEnv();
        initializeJupyterHubTokenFromEnv();
        initializeStartTimeoutFromEnv();
        initializeStopTimeoutFromEnv();
        initializePathTranslationArchivePrefixFromEnv();
        initializePathTranslationArchiveDockerPrefixFromEnv();
        initializePathTranslationWorkspacePrefixFromEnv();
        initializePathTranslationWorkspaceDockerPrefixFromEnv();
        initializeWorkspacePathFromEnv();
        initializeInactivityTimeoutFromEnv();
        initializeMaxServerLifetimeFromEnv();
        initializeAllUsersCanStartJupyterFromEnv();
    }

    /**
     * Initialize the JupyterHub Host URL preference.
     * <p>
     * The JupyterHub Host URL preference is initialized in the following order:
     * 1. If the JH_XNAT_JUPYTERHUB_HOST_URL environment variable is set, use that.
     * 2. If the JupyterHub Host URL preference is not set, try to set it to the XNAT site url.
     * 3. If the JupyterHub Host URL preference is already set, do nothing.
     */
    protected void initializeJupyterHubHostUrl() {
        String jupyterHubHostUrl = systemHelper.getEnv("JH_XNAT_JUPYTERHUB_HOST_URL");

        if (StringUtils.isNotBlank(jupyterHubHostUrl)) {
            // Try to set from the environment variable.
            jupyterHubPreferences.setJupyterHubHostUrl(jupyterHubHostUrl);
            log.info("JupyterHub Host URL initialized from environment variable to: " + jupyterHubHostUrl);
        } else if (StringUtils.isBlank(jupyterHubPreferences.getJupyterHubHostUrl())) {
            // Try to set from the XNAT site url.
            String siteUrl = siteConfigPreferences.getSiteUrl();
            if (StringUtils.isNotBlank(siteUrl)) {
                jupyterHubPreferences.setJupyterHubHostUrl(siteUrl);
                log.info("JupyterHub Host URL preference initialized to site URL: " + siteUrl);
            } else {
                log.info("JupyterHub Host URL not set and XNAT site url not set. Skipping initialization.");
            }
        } else {
            log.info("JupyterHub Host URL already set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub API URL preference from the JH_XNAT_JUPYTERHUB_API_URL environment variable.
     */
    protected void initializeJupyterHubApiUrlFromEnv() {
        String jupyterHubApiUrlEnv = systemHelper.getEnv("JH_XNAT_JUPYTERHUB_API_URL");

        if (StringUtils.isNotBlank(jupyterHubApiUrlEnv)) {
            jupyterHubPreferences.setJupyterHubApiUrl(jupyterHubApiUrlEnv);
            log.info("JupyterHub API URL preference initialized from environment variable to: " + jupyterHubApiUrlEnv);
        } else {
            log.debug("JupyterHub API URL environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub token preference from the JH_XNAT_SERVICE_TOKEN environment variable.
     */
    protected void initializeJupyterHubTokenFromEnv() {
        String jupyterHubTokenEnv = systemHelper.getEnv("JH_XNAT_SERVICE_TOKEN");

        if (StringUtils.isNotBlank(jupyterHubTokenEnv)) {
            jupyterHubPreferences.setJupyterHubToken(jupyterHubTokenEnv);
            log.info("JupyterHub Token preference initialized from environment variable.");
        } else {
            log.debug("JupyterHub Token environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub start timeout preference from the JH_XNAT_START_TIMEOUT environment variable.
     */
    protected void initializeStartTimeoutFromEnv() {
        String jupyterHubStartTimeoutEnv = systemHelper.getEnv("JH_XNAT_START_TIMEOUT");

        if (StringUtils.isNotBlank(jupyterHubStartTimeoutEnv)) {
            try { // Try to convert to an integer.
                int jupyterHubStartTimeout = Integer.parseInt(jupyterHubStartTimeoutEnv);
                jupyterHubPreferences.setStartTimeout(jupyterHubStartTimeout);
                log.info("JupyterHub Start Timeout preference initialized from environment variable to: " + jupyterHubStartTimeout);
            } catch (NumberFormatException e) {
                log.error("JupyterHub Start Timeout environment variable is not an integer. Skipping initialization.");
            }
        } else {
            log.debug("JupyterHub Start Timeout environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub stop timeout preference from the JH_XNAT_STOP_TIMEOUT environment variable.
     */
    protected void initializeStopTimeoutFromEnv() {
        String jupyterHubStopTimeoutEnv = systemHelper.getEnv("JH_XNAT_STOP_TIMEOUT");

        if (StringUtils.isNotBlank(jupyterHubStopTimeoutEnv)) {
            try { // Try to convert to an integer.
                int jupyterHubStopTimeout = Integer.parseInt(jupyterHubStopTimeoutEnv);
                jupyterHubPreferences.setStopTimeout(jupyterHubStopTimeout);
                log.info("JupyterHub Stop Timeout preference initialized from environment variable to: " + jupyterHubStopTimeout);
            } catch (NumberFormatException e) {
                log.error("JupyterHub stop timeout environment variable is not an integer. Skipping initialization.");
            }
        } else {
            log.debug("JupyterHub Stop Timeout environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub path translation archive prefix preference from the JH_XNAT_PT_ARCHIVE_PREFIX environment variable.
     */
    protected void initializePathTranslationArchivePrefixFromEnv() {
        String jupyterHubPathTranslationArchivePrefixEnv = systemHelper.getEnv("JH_XNAT_PT_ARCHIVE_PREFIX");

        if (StringUtils.isNotBlank(jupyterHubPathTranslationArchivePrefixEnv)) {
            jupyterHubPreferences.setPathTranslationArchivePrefix(jupyterHubPathTranslationArchivePrefixEnv);
            log.info("JupyterHub Path Translation Archive Prefix preference initialized from environment variable to: " + jupyterHubPathTranslationArchivePrefixEnv);
        } else {
            log.debug("JupyterHub Path Translation Archive Prefix environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub path translation archive docker prefix preference from the JH_XNAT_PT_ARCHIVE_DOCKER_PREFIX environment variable.
     */
    protected void initializePathTranslationArchiveDockerPrefixFromEnv() {
        String jupyterHubPathTranslationArchiveDockerPrefixEnv = systemHelper.getEnv("JH_XNAT_PT_ARCHIVE_DOCKER_PREFIX");

        if (StringUtils.isNotBlank(jupyterHubPathTranslationArchiveDockerPrefixEnv)) {
            jupyterHubPreferences.setPathTranslationArchiveDockerPrefix(jupyterHubPathTranslationArchiveDockerPrefixEnv);
            log.info("JupyterHub Path Translation Archive Docker Prefix preference initialized from environment variable to: " + jupyterHubPathTranslationArchiveDockerPrefixEnv);
        } else {
            log.debug("JupyterHub Path Translation Archive Docker Prefix environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub path translation workspace prefix preference from the JH_XNAT_PT_WORKSPACE_PREFIX environment variable.
     */
    protected void initializePathTranslationWorkspacePrefixFromEnv() {
        String jupyterHubPathTranslationWorkspacePrefixEnv = systemHelper.getEnv("JH_XNAT_PT_WORKSPACE_PREFIX");

        if (StringUtils.isNotBlank(jupyterHubPathTranslationWorkspacePrefixEnv)) {
            jupyterHubPreferences.setPathTranslationWorkspacePrefix(jupyterHubPathTranslationWorkspacePrefixEnv);
            log.info("JupyterHub Path Translation Workspace Prefix preference initialized from environment variable to: " + jupyterHubPathTranslationWorkspacePrefixEnv);
        } else {
            log.debug("JupyterHub Path Translation Workspace Prefix environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub path translation workspace docker prefix preference from the JH_XNAT_PT_WORKSPACE_DOCKER_PREFIX environment variable.
     */
    protected void initializePathTranslationWorkspaceDockerPrefixFromEnv() {
        String jupyterHubPathTranslationWorkspaceDockerPrefixEnv = systemHelper.getEnv("JH_XNAT_PT_WORKSPACE_DOCKER_PREFIX");

        if (StringUtils.isNotBlank(jupyterHubPathTranslationWorkspaceDockerPrefixEnv)) {
            jupyterHubPreferences.setPathTranslationWorkspaceDockerPrefix(jupyterHubPathTranslationWorkspaceDockerPrefixEnv);
            log.info("JupyterHub Path Translation Workspace Docker Prefix preference initialized from environment variable to: " + jupyterHubPathTranslationWorkspaceDockerPrefixEnv);
        } else {
            log.debug("JupyterHub Path Translation Workspace Docker Prefix environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub workspace path preference from the JH_XNAT_WORKSPACE_PATH environment variable.
     */
    protected void initializeWorkspacePathFromEnv() {
        String jupyterHubWorkspacePathEnv = systemHelper.getEnv("JH_XNAT_WORKSPACE_PATH");

        if (StringUtils.isNotBlank(jupyterHubWorkspacePathEnv)) {
            jupyterHubPreferences.setWorkspacePath(jupyterHubWorkspacePathEnv);
            log.info("JupyterHub Workspace Path preference initialized from environment variable to: " + jupyterHubWorkspacePathEnv);
        } else {
            log.debug("JupyterHub Workspace Path environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub inactivity timeout preference from the JH_XNAT_INACTIVITY_TIMEOUT environment variable.
     */
    protected void initializeInactivityTimeoutFromEnv() {
        String jupyterHubInactivityTimeoutEnv = systemHelper.getEnv("JH_XNAT_INACTIVITY_TIMEOUT");

        if (StringUtils.isNotBlank(jupyterHubInactivityTimeoutEnv)) {
            try { // Try to convert to a long
                long jupyterHubInactivityTimeout = Long.parseLong(jupyterHubInactivityTimeoutEnv);
                jupyterHubPreferences.setInactivityTimeout(jupyterHubInactivityTimeout);
                log.info("JupyterHub Inactivity Timeout preference initialized from environment variable to: " + jupyterHubInactivityTimeout);
            } catch (NumberFormatException e) {
                log.error("JupyterHub Inactivity Timeout environment variable is not a long. Skipping initialization.");
            }
        } else {
            log.debug("JupyterHub Inactivity Timeout environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub max server lifetime preference from the JH_XNAT_MAX_SERVER_LIFETIME environment variable.
     */
    protected void initializeMaxServerLifetimeFromEnv() {
        String jupyterHubMaxServerLifetimeEnv = systemHelper.getEnv("JH_XNAT_MAX_SERVER_LIFETIME");

        if (StringUtils.isNotBlank(jupyterHubMaxServerLifetimeEnv)) {
            try { // Try to convert to a long
                long jupyterHubMaxServerLifetime = Long.parseLong(jupyterHubMaxServerLifetimeEnv);
                // If it is set, set the max server lifetime preference.
                jupyterHubPreferences.setMaxServerLifetime(jupyterHubMaxServerLifetime);
                log.info("JupyterHub Max Server Lifetime preference initialized from environment variable to: " + jupyterHubMaxServerLifetime);
            } catch (NumberFormatException e) {
                log.error("JupyterHub Max Server Lifetime environment variable is not a long. Skipping initialization.");
            }
        } else {
            log.debug("JupyterHub Max Server Lifetime environment variable not set. Skipping initialization.");
        }
    }

    /**
     * Initialize the JupyterHub all users can start Jupyter preference from the JH_XNAT_ALL_USERS_CAN_START_JUPYTER environment variable.
     */
    protected void initializeAllUsersCanStartJupyterFromEnv() {
        String jupyterHubAllUsersCanStartJupyterEnv = systemHelper.getEnv("JH_XNAT_ALL_USERS_CAN_START_JUPYTER");

        if (StringUtils.isNotBlank(jupyterHubAllUsersCanStartJupyterEnv)) {
            // Convert to a boolean, string must be "true" or anything else is false.
            boolean jupyterHubAllUsersCanStartJupyter = Boolean.parseBoolean(jupyterHubAllUsersCanStartJupyterEnv);
            jupyterHubPreferences.setAllUsersCanStartJupyter(jupyterHubAllUsersCanStartJupyter);
            log.info("JupyterHub All Users Can Start Jupyter preference initialized from environment variable to: " + jupyterHubAllUsersCanStartJupyter);
        } else {
            log.debug("JupyterHub All Users Can Start Jupyter environment variable not set. Skipping initialization.");
        }
    }

}
