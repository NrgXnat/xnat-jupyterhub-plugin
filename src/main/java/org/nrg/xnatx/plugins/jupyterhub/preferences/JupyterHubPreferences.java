package org.nrg.xnatx.plugins.jupyterhub.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;

@NrgPreferenceBean(toolId = JupyterHubPreferences.TOOL_ID,
                   toolName = "JupyterHub Preferences",
                   description = "Manages preferences for JupyterHub plugin")
@Slf4j
public class JupyterHubPreferences extends AbstractPreferenceBean {

    public static final String TOOL_ID = "jupyterhub";

    @Autowired
    protected JupyterHubPreferences(NrgPreferenceService preferenceService, ConfigPaths configFolderPaths, OrderedProperties initPrefs) {
        super(preferenceService, configFolderPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "http://host.docker.internal/jupyterhub/hub/api")
    public String getJupyterHubApiUrl() {
        return getValue("jupyterHubApiUrl");
    }

    public void setJupyterHubApiUrl(final String jupyterHubApiUrl) {
        try {
            set(jupyterHubApiUrl, "jupyterHubApiUrl");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'jupyterHubApiUrl': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "secret-token")
    public String getJupyterHubToken() {
        return getValue("jupyterHubToken");
    }

    public void setJupyterHubToken(final String jupyterHubToken) {
        try {
            set(jupyterHubToken, "jupyterHubToken");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'jupyterHubToken': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "")
    public String getPathTranslationXnatPrefix() {
        return getValue("pathTranslationXnatPrefix");
    }

    public void setPathTranslationXnatPrefix(final String pathTranslationXnatPrefix) {
        try {
            set(pathTranslationXnatPrefix, "pathTranslationXnatPrefix");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'pathTranslationXnatPrefix': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "")
    public String getPathTranslationDockerPrefix() {
        return getValue("pathTranslationDockerPrefix");
    }

    public void setPathTranslationDockerPrefix(final String pathTranslationDockerPrefix) {
        try {
            set(pathTranslationDockerPrefix, "pathTranslationDockerPrefix");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'pathTranslationDockerPrefix': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "/data/xnat/workspaces")
    public String getWorkspacePath() {
        return getValue("workspacePath");
    }

    public void setWorkspacePath(final String workspacePath) {
        try {
            set(workspacePath, "workspacePath");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'workspacePath': something is very wrong here.", e);
        }
    }
}
