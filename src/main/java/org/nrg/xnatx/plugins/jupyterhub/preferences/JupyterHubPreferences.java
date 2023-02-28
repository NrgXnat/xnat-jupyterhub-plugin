package org.nrg.xnatx.plugins.jupyterhub.preferences;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnatx.plugins.jupyterhub.models.DockerImage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
@NrgPreferenceBean(toolId = JupyterHubPreferences.TOOL_ID,
                   toolName = "JupyterHub Preferences",
                   description = "Manages preferences for JupyterHub plugin")
@Slf4j
public class JupyterHubPreferences extends AbstractPreferenceBean {

    public static final String TOOL_ID = "jupyterhub";
    public static final String ALL_USERS_JUPYTER = "allUsersCanStartJupyter";
    public static final String DOCKER_IMAGES_PREF_ID = "dockerImages";
    public static final String CONTAINER_SPEC_LABELS_PREF_ID = "containerSpecLabels";
    public static final String PLACEMENT_SPEC_CONSTRAINTS_PREF_ID = "placementSpecConstraints";
    public static final String RESOURCE_SPEC_CPU_LIMIT_PREF_ID = "resourceSpecCpuLimit";
    public static final String RESOURCE_SPEC_CPU_RESERVATION_PREF_ID = "resourceSpecCpuReservation";
    public static final String RESOURCE_SPEC_MEM_LIMIT_PREF_ID = "resourceSpecMemLimit";
    public static final String RESOURCE_SPEC_MEM_RESERVATION_PREF_ID = "resourceSpecMemReservation";
    public static final String INACTIVITY_TIMEOUT_PREF_ID = "inactivityTimeout";
    public static final String MAX_SERVER_LIFETIME_PREF_ID = "maxServerLifetime";

    @Autowired
    protected JupyterHubPreferences(NrgPreferenceService preferenceService, ConfigPaths configFolderPaths, OrderedProperties initPrefs) {
        super(preferenceService, configFolderPaths, initPrefs);
    }

    @NrgPreference(defaultValue = "http://172.17.0.1/jupyterhub/hub/api")
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

    @Deprecated
    @NrgPreference(defaultValue = "[\n   " +
            "{\"image\": \"jupyter/scipy-notebook:hub-3.0.0\", \"enabled\": \"true\"}\n   " +
            "]",
            key = "image")
    public List<DockerImage> getDockerImages() {
        return getListValue(DOCKER_IMAGES_PREF_ID);
    }

    @Deprecated
    public void setDockerImages(final List<DockerImage> dockerImages) {
        try {
            setListValue(DOCKER_IMAGES_PREF_ID, dockerImages);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'dockerImages': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "180")
    public Integer getStartTimeout() {
        return getIntegerValue("startTimeout");
    }

    public void setStartTimeout(final Integer startTimeout) {
        try {
            setIntegerValue(startTimeout, "startTimeout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'startTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "60")
    public Integer getStopTimeout() {
        return getIntegerValue("stopTimeout");
    }

    public void setStopTimeout(final Integer stopTimeout) {
        try {
            setIntegerValue(stopTimeout, "stopTimeout");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'stopTimeout': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "8")
    public Integer getStartPollingInterval() {
        return getIntegerValue("startPollingInterval");
    }

    public void getStartPollingInterval(final Integer startPollingInterval) {
        try {
            setIntegerValue(startPollingInterval, "startPollingInterval");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'startPollingInterval': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "2")
    public Integer getStopPollingInterval() {
        return getIntegerValue("stopPollingInterval");
    }

    public void getStopPollingInterval(final Integer stopPollingInterval) {
        try {
            setIntegerValue(stopPollingInterval, "stopPollingInterval");
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'stopPollingInterval': something is very wrong here.", e);
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

    @Deprecated
    @NrgPreference(defaultValue = "")
    public Map<String, String> getContainerSpecLabels() {
        return getMapValue(CONTAINER_SPEC_LABELS_PREF_ID);
    }

    @Deprecated
    public void setContainerSpecLabels(final Map<String, String> containerSpecLabels) {
        try {
            setMapValue(CONTAINER_SPEC_LABELS_PREF_ID, containerSpecLabels);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'containerSpecLabels': something is very wrong here.", e);
        }
    }

    @Deprecated
    @NrgPreference(defaultValue = "[]")
    public List<String> getPlacementSpecConstraints() {
        return getListValue(PLACEMENT_SPEC_CONSTRAINTS_PREF_ID);
    }

    @Deprecated
    public void setPlacementSpecConstraints(List<String> placementSpecConstraints) {
        try {
            setListValue(PLACEMENT_SPEC_CONSTRAINTS_PREF_ID, placementSpecConstraints);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'placementSpecConstraints': something is very wrong here.", e);
        }
    }

    @Deprecated
    @NrgPreference(defaultValue = "0")
    public Double getResourceSpecCpuLimit() {
        return getDoubleValue(RESOURCE_SPEC_CPU_LIMIT_PREF_ID);
    }

    @Deprecated
    public void setResourceSpecCpuLimit(final Double resourceSpecCpuLimit) {
        try {
            setDoubleValue(resourceSpecCpuLimit, RESOURCE_SPEC_CPU_LIMIT_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name, something is very wrong here.", e);
        }
    }

    @Deprecated
    @NrgPreference(defaultValue = "0")
    public Double getResourceSpecCpuReservation() {
        return getDoubleValue(RESOURCE_SPEC_CPU_RESERVATION_PREF_ID);
    }

    @Deprecated
    public void setResourceSpecCpuReservation(final Double resourceSpecCpuReservation) {
        try {
            setDoubleValue(resourceSpecCpuReservation, RESOURCE_SPEC_CPU_RESERVATION_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name, something is very wrong here.", e);
        }
    }

    @Deprecated
    @NrgPreference(defaultValue = "")
    public String getResourceSpecMemLimit() {
        return getValue(RESOURCE_SPEC_MEM_LIMIT_PREF_ID);
    }

    @Deprecated
    public void setResourceSpecMemLimit(final String resourceSpecMemLimit) {
        try {
            set(resourceSpecMemLimit, RESOURCE_SPEC_MEM_LIMIT_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name, something is very wrong here.", e);
        }
    }

    @Deprecated
    @NrgPreference(defaultValue = "")
    public String getResourceSpecMemReservation() {
        return getValue(RESOURCE_SPEC_MEM_RESERVATION_PREF_ID);
    }

    @Deprecated
    public void setResourceSpecMemReservation(final String resourceSpecMemReservation) {
        try {
            set(resourceSpecMemReservation, RESOURCE_SPEC_MEM_RESERVATION_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name, something is very wrong here.", e);
        }
    }

    // Inactivity timeout in minutes
    @NrgPreference(defaultValue = "60")
    public long getInactivityTimeout() {
        return getLongValue(INACTIVITY_TIMEOUT_PREF_ID);
    }

    public void setInactivityTimeout(final long inactivityTimeout) {
        try {
            setLongValue(inactivityTimeout, INACTIVITY_TIMEOUT_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'inactivityTimeout': something is very wrong here.", e);
        }
    }

    // Max server lifetime in hours
    @NrgPreference(defaultValue = "48")
    public long getMaxServerLifetime() {
        return getLongValue(MAX_SERVER_LIFETIME_PREF_ID);
    }

    public void setMaxServerLifetime(final long maxServerLifetime) {
        try {
            setLongValue(maxServerLifetime, MAX_SERVER_LIFETIME_PREF_ID);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'maxServerLifetime': something is very wrong here.", e);
        }
    }

    @NrgPreference(defaultValue = "false")
    public boolean getAllUsersCanStartJupyter() {
        return getBooleanValue(ALL_USERS_JUPYTER);
    }

    public void setAllUsersCanStartJupyter(final boolean allUsersCanStartJupyter) {
        try {
            setBooleanValue(allUsersCanStartJupyter, ALL_USERS_JUPYTER);
        } catch (InvalidPreferenceName e) {
            log.error("Invalid preference name 'allUsersCanStartJupyter': something is very wrong here.", e);
        }
    }

}
