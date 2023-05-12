package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import lombok.NonNull;
import org.nrg.jobtemplates.models.*;
import org.nrg.jobtemplates.services.JobTemplateService;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnatx.plugins.jupyterhub.entities.UserOptionsEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.Mount;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.*;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserWorkspaceService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultUserOptionsService implements UserOptionsService {

    private final JupyterHubPreferences jupyterHubPreferences;
    private final UserWorkspaceService userWorkspaceService;
    private final SearchHelperServiceI searchHelperService;
    private final AliasTokenService aliasTokenService;
    private final SiteConfigPreferences siteConfigPreferences;
    private final UserOptionsEntityService userOptionsEntityService;
    private final PermissionsHelper permissionsHelper;
    private final JobTemplateService jobTemplateService;

    @Autowired
    public DefaultUserOptionsService(final JupyterHubPreferences jupyterHubPreferences,
                                     final UserWorkspaceService userWorkspaceService,
                                     final SearchHelperServiceI searchHelperService,
                                     final AliasTokenService aliasTokenService,
                                     final SiteConfigPreferences siteConfigPreferences,
                                     final UserOptionsEntityService userOptionsEntityService,
                                     final PermissionsHelper permissionsHelper,
                                     final JobTemplateService jobTemplateService) {
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.userWorkspaceService = userWorkspaceService;
        this.searchHelperService = searchHelperService;
        this.aliasTokenService = aliasTokenService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.userOptionsEntityService = userOptionsEntityService;
        this.permissionsHelper = permissionsHelper;
        this.jobTemplateService = jobTemplateService;
    }

    @Override
    public Map<String, String> getProjectPaths(final UserI user, final List<String> projectIds) {
        Map<String, String> projectPaths = new HashMap<>();

        projectIds.forEach(projectId -> {
            XnatProjectdata xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);

            if (xnatProjectdata != null) {
                // Experiments
                final Path projectDirectory = Paths.get(xnatProjectdata.getRootArchivePath() + xnatProjectdata.getCurrentArc());
                if (Files.exists(projectDirectory)) {
                    projectPaths.put("/data/projects/" + projectId + "/experiments", projectDirectory.toString());
                }

                // Project resources
                final Path resourceDirectory = Paths.get(xnatProjectdata.getRootArchivePath() + "/resources");
                if (Files.exists(resourceDirectory)) {
                    projectPaths.put("/data/projects/" + projectId + "/resources", resourceDirectory.toString());
                }

                // Subject resources
                final Path subjectResourceDirectory = Paths.get(xnatProjectdata.getRootArchivePath() + "/subjects");
                if (Files.exists(subjectResourceDirectory)) {
                    projectPaths.put("/data/projects/" + projectId + "/subjects", subjectResourceDirectory.toString());
                }

            }
        });

        return projectPaths;
    }

    @Override
    public Map<String, String> getSubjectPaths(final UserI user, final String subjectId) {
        return getSubjectPaths(user, Collections.singletonList(subjectId));
    }

    @Override
    public Map<String, String> getSubjectPaths(final UserI user, final List<String> subjectIds) {
        Map<String, String> subjectPaths = new HashMap<>();

        subjectIds.forEach(subjectId -> {
            XnatSubjectdata xnatSubjectdata = XnatSubjectdata.getXnatSubjectdatasById(subjectId, user, false);

            if (xnatSubjectdata != null) {
                final XnatProjectdata xnatProjectdata = xnatSubjectdata.getPrimaryProject(false);
                List<XnatSubjectassessordataI> subjectAssessors = xnatSubjectdata.getExperiments_experiment();

                // Subject resources
                final Path subjectResourceDirectory = Paths.get(xnatProjectdata.getRootArchivePath())
                                                           .resolve("subjects")
                                                           .resolve(xnatSubjectdata.getLabel());

                if (Files.exists(subjectResourceDirectory)) {
                    subjectPaths.put("/data/projects/" + xnatProjectdata.getId() + "/subject-resources/" + xnatSubjectdata.getLabel(), subjectResourceDirectory.toString());
                }

                subjectAssessors.forEach(subjectAssessor -> {
                    if (XnatExperimentdata.class.isAssignableFrom(subjectAssessor.getClass())) {
                        try {
                            final String assessorLabel = subjectAssessor.getLabel();
                            final String path = ((XnatExperimentdata) subjectAssessor).getCurrentSessionFolder(true);

                            // Experiments
                            subjectPaths.put("/data/projects/" + xnatProjectdata.getId() + "/experiments/" + assessorLabel, path);
                        } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException | InvalidArchiveStructure e) {
                            // Container service ignores this error.
                            log.error("", e);
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });

        return subjectPaths;
    }

    @Override
    public Map<String, String> getExperimentPath(final UserI user, final String experimentId) {
        return getExperimentPaths(user, Collections.singletonList(experimentId));
    }

    @Override
    public Map<String, String> getExperimentPaths(final UserI user, final List<String> experimentIds) {
        Map<String, String> experimentPaths = new HashMap<>();

        experimentIds.forEach(experimentId -> {
            XnatExperimentdata xnatExperimentdata = XnatExperimentdata.getXnatExperimentdatasById(experimentId, user, false);

            if (xnatExperimentdata != null) {
                try {
                    final String experimentLabel = xnatExperimentdata.getLabel();
                    final String experimentPath = xnatExperimentdata.getCurrentSessionFolder(true);
                    final String projectId = xnatExperimentdata.getPrimaryProject(false).getId();
                    experimentPaths.put("/data/projects/" + projectId + "/experiments/" + experimentLabel, experimentPath);
                } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException | InvalidArchiveStructure e) {
                    // Container service ignores this error.
                    log.error("", e);
                    throw new RuntimeException(e);
                }
            }
        });

        return experimentPaths;
    }

    @Override
    public Map<String, String> getImageScanPath(final UserI user, final Integer imageScanId) {
        return getImageScanPaths(user, Collections.singletonList(imageScanId));
    }

    @Override
    public Map<String, String> getImageScanPaths(final UserI user, final List<Integer> imageScanIds) {
        Map<String, String> imageScanPaths = new HashMap<>();

        imageScanIds.forEach(imageScanId -> {
            XnatImagescandata imageScan = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(imageScanId, user, false);

            if (imageScan != null) {
                final String imageScanLabel = imageScan.getId();
                final String imageScanPath = imageScan.deriveScanDir();
                final String project = imageScan.getProject();
                final String experimentLabel = imageScan.getImageSessionData().getLabel();

                imageScanPaths.put("/data/projects/" + project + "/experiments/" + experimentLabel + "/SCANS/" + imageScanLabel, imageScanPath);
            }
        });

        return imageScanPaths;
    }

    @Override
    public Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId, @Nullable String projectId) {
        Map<String, String> storedSearchPaths = new HashMap<>();
        XdatStoredSearch storedSearch;

        if (storedSearchId.startsWith("@")) { // Site wide or project data bundle @xnat:subjectData
            if (projectId == null) { // Site wide data bundle
                // Mount All projects
                List<String> projectIds = XnatProjectdata.getAllXnatProjectdatas(user, false)
                        .stream()
                        .map(AutoXnatProjectdata::getId)
                        .collect(Collectors.toList());

                return getProjectPaths(user, projectIds);
            } else { // Project data bundle
                XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);

                if (project != null) {
                    // Mount the single project
                    return getProjectPaths(user, Collections.singletonList(projectId));
                } else {
                    throw new RuntimeException("Project " + projectId + " not found.");
                }
            }
        } else { // Stored search
            storedSearch = searchHelperService.getSearchForUser(user, storedSearchId);

            if (storedSearch == null) {
                return storedSearchPaths;
            }

            // For project level stored searches limit the paths to that project
            // For site wide stored searches add all readable projects
            List<String> projectIds = (projectId == null) ?
                    XnatProjectdata.getAllXnatProjectdatas(user, false)
                            .stream()
                            .map(AutoXnatProjectdata::getId)
                            .collect(Collectors.toList()) :
                    Collections.singletonList(projectId);

            return getProjectPaths(user, projectIds);
        }
    }

    @Override
    public Optional<XnatUserOptions> retrieveUserOptions(UserI user) {
        return retrieveUserOptions(user, "");
    }

    @Override
    public Optional<XnatUserOptions> retrieveUserOptions(UserI user, String servername) {
        log.debug("Retrieving user options for user {} server {}",
                  user.getUsername(), servername);

        Optional<UserOptionsEntity> userOptionsEntity = userOptionsEntityService.find(user.getID(), servername);

        if (!userOptionsEntity.isPresent()) {
            return Optional.empty();
        }

        final String projectId = userOptionsEntity.get().getProjectId();
        final String itemId = userOptionsEntity.get().getItemId();
        final String xsiType = userOptionsEntity.get().getXsiType();
        if (!permissionsHelper.canRead(user, projectId, itemId, xsiType)) {
            return Optional.empty();
        }

        return userOptionsEntity.map(UserOptionsEntity::toPojo);
    }

    @Override
    public void storeUserOptions(UserI user, String servername, String xsiType, String id, String projectId,
                                 Long computeSpecConfigId, Long hardwareConfigId, String eventTrackingId) {
        log.debug("Storing user options for user '{}' server '{}' xsiType '{}' id '{}' projectId '{}'",
                  user.getUsername(), servername, xsiType, id, projectId);

        if (!permissionsHelper.canRead(user, projectId, id, xsiType)) {
            return;
        }

        // Resolve the job template before resolving the paths
        JobTemplate jobTemplate = jobTemplateService.resolve(user.getUsername(), projectId, computeSpecConfigId, hardwareConfigId);

        // specific xsi type -> general xsi type
        if (instanceOf(xsiType, XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
            xsiType = XnatExperimentdata.SCHEMA_ELEMENT_NAME;
        } else if (instanceOf(xsiType, XnatImagescandata.SCHEMA_ELEMENT_NAME)) {
            xsiType = XnatImagescandata.SCHEMA_ELEMENT_NAME;
        }

        Map<String, String> paths = new HashMap<>();

        switch (xsiType) {
            case (XnatProjectdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(getProjectPaths(user, Collections.singletonList(id)));
                break;
            }
            case (XnatSubjectdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(getSubjectPaths(user, id));
                break;
            }
            case (XnatExperimentdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(getExperimentPath(user, id));
                break;
            }
            case (XnatImagescandata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(getImageScanPath(user, Integer.parseInt(id)));
                break;
            }
            case (XdatStoredSearch.SCHEMA_ELEMENT_NAME): {
                paths.putAll(getStoredSearchPaths(user, id, projectId));
                break;
            }
        }

        // Gather mounts
        final Path workspacePath = userWorkspaceService.getUserWorkspace(user);

        // Add user workspace mount
        final Mount userNotebookDirectoryMount = Mount.builder()
                .source(translatePath(workspacePath.toString()))
                .target(Paths.get("/workspace", user.getUsername()).toString())
                .type("bind")
                .readOnly(false)
                .build();

        // Add xnat data mounts
        final List<Mount> xnatDataMounts = paths.entrySet().stream()
                .map((entry) -> Mount.builder()
                        .source(translatePath(entry.getValue()))
                        .target(Paths.get(entry.getKey()).toString())
                        .type("bind")
                        .readOnly(true)
                        .build())
                .collect(Collectors.toList());

        // Collect mounts
        final List<Mount> mounts = new ArrayList<>(Collections.emptyList());
        mounts.add(userNotebookDirectoryMount);
        mounts.addAll(xnatDataMounts);

        // Add mounts
        TaskTemplate taskTemplate = toTaskTemplate(jobTemplate);
        taskTemplate.getContainerSpec().getMounts().addAll(mounts);

        // Get and add env variables
        Map<String, String> environmentVariables = getDefaultEnvironmentVariables(user, xsiType, id);
        taskTemplate.getContainerSpec().getEnv().putAll(environmentVariables);

        // Store the user options
        UserOptionsEntity userOptionsEntity = UserOptionsEntity.builder()
                .userId(user.getID())
                .servername(servername)
                .xsiType(xsiType)
                .itemId(id)
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .taskTemplate(taskTemplate)
                .build();

        userOptionsEntityService.createOrUpdate(userOptionsEntity);
    }

    private String translatePath(String path) {
        String pathTranslationXnatPrefix = jupyterHubPreferences.getPathTranslationXnatPrefix();
        String pathTranslationDockerPrefix = jupyterHubPreferences.getPathTranslationDockerPrefix();

        if (!StringUtils.isEmpty(pathTranslationXnatPrefix) && !StringUtils.isEmpty(pathTranslationDockerPrefix)) {
            return path.replace(pathTranslationXnatPrefix, pathTranslationDockerPrefix);
        } else {
            return path;
        }
    }

    protected Map<String, String> getDefaultEnvironmentVariables(UserI user, String xsiType, String id) {
        final AliasToken token = aliasTokenService.issueTokenForUser(user);
        final String processingUrl = (String) siteConfigPreferences.getProperty("processingUrl");
        final String xnatHostUrl = StringUtils.isBlank(processingUrl) ? siteConfigPreferences.getSiteUrl() : processingUrl;

        final Map<String, String> defaultEnvironmentVariables = new HashMap<>();
        defaultEnvironmentVariables.put("XNAT_HOST", xnatHostUrl);
        defaultEnvironmentVariables.put("XNAT_USER", token.getAlias());
        defaultEnvironmentVariables.put("XNAT_PASS", token.getSecret());
        defaultEnvironmentVariables.put("XNAT_DATA", "/data");
        defaultEnvironmentVariables.put("XNAT_XSI_TYPE", xsiType);
        defaultEnvironmentVariables.put("XNAT_ITEM_ID", id);
        defaultEnvironmentVariables.put("JUPYTERHUB_ROOT_DIR", Paths.get("/workspace", user.getUsername()).toString());
        defaultEnvironmentVariables.put("XDG_CONFIG_HOME", Paths.get("/workspace", user.getUsername()).toString());

        return defaultEnvironmentVariables;
    }


    private boolean instanceOf(final String xsiType, final String instanceOfXsiType) {
        try {
            return GenericWrapperElement.GetElement(xsiType).instanceOf(instanceOfXsiType);
        } catch (ElementNotFoundException e) {
            return false;
        } catch (XFTInitException e) {
            log.error("Unable to compare xsi types", e);
            throw new RuntimeException(e);
        }
    }

    protected TaskTemplate toTaskTemplate(@NonNull JobTemplate jobTemplate) {
        // Setup the TaskTemplate
        ContainerSpec containerSpec = ContainerSpec
                .builder()
                .image("")
                .env(new HashMap<>())
                .labels(new HashMap<>())
                .mounts(new ArrayList<>())
                .build();

        Resources resources = Resources.builder()
                .genericResources(new HashMap<>())
                .build();

        Placement placement = new Placement(new ArrayList<>());

        TaskTemplate taskTemplate = TaskTemplate.builder()
                .containerSpec(containerSpec)
                .resources(resources)
                .placement(placement)
                .build();

        // Get the compute spec, hardware, and constraints from the job template
        ComputeSpec computeSpec = jobTemplate.getComputeSpec();
        Hardware hardware = jobTemplate.getHardware();
        List<Constraint> constraints = jobTemplate.getConstraints();

        // Build container spec for the task template
        containerSpec.setImage(computeSpec.getImage());

        // Add environment variables from compute spec and hardware
        Map<String, String> environmentVariables = new HashMap<>();
        Map<String, String> computeSpecEnvVars = computeSpec.getEnvironmentVariables().stream().collect(Collectors.toMap(EnvironmentVariable::getKey, EnvironmentVariable::getValue));
        Map<String, String> hardwareEnvVars = hardware.getEnvironmentVariables().stream().collect(Collectors.toMap(EnvironmentVariable::getKey, EnvironmentVariable::getValue));

        // check for empty otherwise putAll will throw an exception
        if (!computeSpecEnvVars.isEmpty()) {
            environmentVariables.putAll(computeSpecEnvVars);
        }

        // check for empty otherwise putAll will throw an exception
        if (!hardwareEnvVars.isEmpty()) {
            environmentVariables.putAll(hardwareEnvVars);
        }

        containerSpec.setEnv(environmentVariables);

        // Add mounts from compute spec and hardware
        // check for empty otherwise addAll will throw an exception
        List<Mount> mounts = new ArrayList<>();
        if (!computeSpec.getMounts().isEmpty()) {
            mounts.addAll(computeSpec.getMounts().stream().map(mount -> {
                return Mount.builder()
                        .source(mount.getLocalPath())
                        .target(mount.getContainerPath())
                        .type(StringUtils.isEmpty(mount.getVolumeName()) ? "bind" : "volume")
                        .readOnly(mount.isReadOnly())
                        .build();
            }).collect(Collectors.toList()));
        }

        containerSpec.setMounts(mounts);

        // Build resources
        resources.setCpuReservation(hardware.getCpuReservation());
        resources.setCpuLimit(hardware.getCpuLimit());
        resources.setMemReservation(hardware.getMemoryReservation());
        resources.setMemLimit(hardware.getMemoryLimit());

        if (!hardware.getGenericResources().isEmpty()) {
            resources.setGenericResources((hardware.getGenericResources().stream().collect(Collectors.toMap(GenericResource::getName, GenericResource::getValue))));
        }
        // Build placement
        if (!hardware.getConstraints().isEmpty()) {
            placement.getConstraints().addAll(hardware.getConstraints().stream().flatMap(constraint -> constraint.toList().stream()).collect(Collectors.toList()));
        }

        if (constraints != null && !constraints.isEmpty()) {
            placement.getConstraints().addAll(constraints.stream().flatMap(constraint -> constraint.toList().stream()).collect(Collectors.toList()));
        }

        return taskTemplate;
    }
}
