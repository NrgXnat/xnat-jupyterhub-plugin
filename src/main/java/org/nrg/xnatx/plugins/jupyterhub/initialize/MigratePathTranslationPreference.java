package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Migrate the pathTranslationXnatPrefix preferences to the new pathTranslationArchivePrefix and pathTranslationWorkspacePrefix preferences.
 * Migrate the pathTranslationDockerPrefix preferences to the new pathTranslationArchiveDockerPrefix and pathTranslationWorkspaceDockerPrefix preferences.
 */
@Component
@Slf4j
public class MigratePathTranslationPreference extends AbstractInitializingTask {

    private final NrgPreferenceService nrgPreferenceService;
    private final JupyterHubPreferences jupyterHubPreferences;
    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;

    @Autowired
    public MigratePathTranslationPreference(final XFTManagerHelper xftManagerHelper,
                                            final XnatAppInfo appInfo,
                                            final NrgPreferenceService nrgPreferenceService,
                                            final JupyterHubPreferences jupyterHubPreferences) {
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.nrgPreferenceService = nrgPreferenceService;
        this.jupyterHubPreferences = jupyterHubPreferences;
    }

    @Override
    public String getTaskName() {
        return "MigratePathTranslationPreference";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing JupyterHub preferences.");

        // Not sure if I need to check both of these or just one.
        if (!xftManagerHelper.isInitialized()) {
            log.debug("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!appInfo.isInitialized()) {
            log.debug("XNAT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        // Migrate the old pathTranslation preferences to the new ones.
        // Delete the old preferences after migrating them.
        try {
            Properties properties = nrgPreferenceService.getToolProperties(JupyterHubPreferences.TOOL_ID);
            if (!properties.isEmpty()) {
                for(String key: properties.stringPropertyNames()) {
                    if (key.startsWith("pathTranslationXnatPrefix")) {
                        if (StringUtils.isBlank(properties.getProperty(key))) {
                            // No need to migrate if the value is blank, but we should delete the old preference.
                            nrgPreferenceService.deletePreference(JupyterHubPreferences.TOOL_ID, key);
                            continue;
                        }

                        jupyterHubPreferences.setPathTranslationArchivePrefix(properties.getProperty(key) + "/archive");
                        jupyterHubPreferences.setPathTranslationWorkspacePrefix(properties.getProperty(key) + "/workspaces");
                        log.debug("Migrated pathTranslationXnatPrefix preference.");

                        nrgPreferenceService.deletePreference(JupyterHubPreferences.TOOL_ID, key);
                        log.debug("Deleted pathTranslationXnatPrefix preference.");
                    } else if (key.startsWith("pathTranslationDockerPrefix")) {
                        if (StringUtils.isBlank(properties.getProperty(key))) {
                            // No need to migrate if the value is blank, but we do need to delete the preference.
                            nrgPreferenceService.deletePreference(JupyterHubPreferences.TOOL_ID, key);
                            continue;
                        }

                        jupyterHubPreferences.setPathTranslationArchiveDockerPrefix(properties.getProperty(key) + "/archive");
                        jupyterHubPreferences.setPathTranslationWorkspaceDockerPrefix(properties.getProperty(key) + "/workspaces");
                        log.debug("Migrated pathTranslationDockerPrefix preference.");

                        nrgPreferenceService.deletePreference(JupyterHubPreferences.TOOL_ID, key);
                        log.debug("Deleted pathTranslationDockerPrefix preference.");
                    }
                }
            }
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Error migrating pathTranslation preferences.", e);
        }

    }
}
