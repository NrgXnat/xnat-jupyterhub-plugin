package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnatx.plugins.jupyterhub.entities.UserOptionsEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.BindMount;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserWorkspaceService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
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


    @Autowired
    public DefaultUserOptionsService(final JupyterHubPreferences jupyterHubPreferences,
                                     final UserWorkspaceService userWorkspaceService,
                                     final SearchHelperServiceI searchHelperService,
                                     final AliasTokenService aliasTokenService,
                                     final SiteConfigPreferences siteConfigPreferences,
                                     final UserOptionsEntityService userOptionsEntityService,
                                     final PermissionsHelper permissionsHelper) {
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.userWorkspaceService = userWorkspaceService;
        this.searchHelperService = searchHelperService;
        this.aliasTokenService = aliasTokenService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.userOptionsEntityService = userOptionsEntityService;
        this.permissionsHelper = permissionsHelper;
    }

    @Override
    public Map<String, String> getProjectPaths(final UserI user, final List<String> projectIds) {
        Map<String, String> projectPaths = new HashMap<>();

        projectIds.forEach(projectId -> {
            XnatProjectdata xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);

            if (xnatProjectdata != null) {
                // Experiments
                projectPaths.put("/data/projects/" + projectId + "/experiments", xnatProjectdata.getRootArchivePath() + xnatProjectdata.getCurrentArc());

                // Project resources
                final Path resourceDirectory = Paths.get(xnatProjectdata.getRootArchivePath() + "/resources");
                if (Files.exists(resourceDirectory)) {
                    projectPaths.put("/data/projects/" + projectId + "/project-resources", resourceDirectory.toString());
                }

                // Subject resources
                final Path subjectResourceDirectory = Paths.get(xnatProjectdata.getRootArchivePath() + "/subjects");
                if (Files.exists(subjectResourceDirectory)) {
                    projectPaths.put("/data/projects/" + projectId + "/subject-resources", subjectResourceDirectory.toString());
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
    public Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId) {
        return getStoredSearchPaths(user, storedSearchId, null);
    }

    @Override
    public Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId, Path csv) {
        Map<String, String> storedSearchPaths = new HashMap<>();

        XdatStoredSearch storedSearch;

        // TODO Set Search Description

        if (storedSearchId.contains("@")) { // Site wide or project data bundle
            if (storedSearchId.startsWith("@")) { // Site wide data bundle -> @xnat:subjectData
                String elementName = storedSearchId.substring(1);
                DisplaySearch displaySearch = new DisplaySearch();
                displaySearch.setUser(user);
                displaySearch.setDisplay("listing");

                try {
                    displaySearch.setRootElement(elementName);
                } catch (XFTInitException | ElementNotFoundException e) {
                    log.error("Unable to set root element", e);
                    throw new RuntimeException(e);
                }

                storedSearch = displaySearch.convertToStoredSearch(storedSearchId);
                storedSearch.setId(storedSearchId);

                String description = storedSearchId.replace("@", "_").replace(":", "_");

                storedSearch.setDescription(description);
                storedSearch.setBriefDescription(description);
            } else { // Project data bundle -> ProjectA@xnat:subjectData
                String[] splitId = storedSearchId.split("@");
                String projectId = splitId[0];
                String elementName = splitId[1];

                XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);

                if (project != null) {
                    storedSearch = project.getDefaultSearch(elementName);

                    String description = storedSearchId.replace("@", "_").replace(":", "_");

                    storedSearch.setDescription(description);
                    storedSearch.setBriefDescription(description);
                } else {
                    throw new RuntimeException("Project " + projectId + " not found.");
                }
            }
        } else { // Stored search
            storedSearch = searchHelperService.getSearchForUser(user, storedSearchId);
        }

        if (storedSearch == null) {
            return storedSearchPaths;
        }

        try {
            // First get the keys/ids associated with each row of the search.
            // These will be the ids of the root element
            MaterializedViewI mv = MaterializedView.getViewBySearchID(storedSearch.getId(), user, MaterializedView.DEFAULT_MATERIALIZED_VIEW_SERVICE_CODE);

            // Add paths for each key
            final String rootElement = storedSearch.getRootElementName();
            if (rootElement.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                final ArrayList<String> subjectIds = mv.getColumnValues("key").convertColumnToArrayList("values");
                storedSearchPaths.putAll(this.getSubjectPaths(user, subjectIds));
            } else if (instanceOf(rootElement, XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
                final ArrayList<String> experimentIds = mv.getColumnValues("key").convertColumnToArrayList("values");
                storedSearchPaths.putAll(this.getExperimentPaths(user, experimentIds));
            } else if (instanceOf(rootElement, XnatImagescandata.SCHEMA_ELEMENT_NAME)) {
                final ArrayList<Integer> imageScanIds = mv.getColumnValues("key").convertColumnToArrayList("values");
                storedSearchPaths.putAll(this.getImageScanPaths(user, imageScanIds));
            } // ELSE -> nothing. Only support searches on subjects, image sessions and image scans // TODO Throw Error??

            if (csv != null) {
                // Execute the search
                DisplaySearch ds = storedSearch.getDisplaySearch(user);
                final XFTTable data = (XFTTable) ds.execute(new CSVPresenter(), user.getLogin());

                // Write search output to csv
                if (!Files.exists(csv)) {
                    Files.createDirectories(csv.getParent());
                    Files.createFile(csv);
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(csv.toFile(), false));
                data.toCSV(writer);
            }
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

        return storedSearchPaths;
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
    public void storeUserOptions(UserI user, String servername, String xsiType, String id, String projectId, String dockerImage) {
        log.debug("Storing user options for user '{}' server '{}' xsiType '{}' id '{}' projectId '{}'",
                  user.getUsername(), servername, xsiType, id, projectId);

        if (!permissionsHelper.canRead(user, projectId, id, xsiType)) {
            return;
        }

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
                Path csv = userWorkspaceService.getUserWorkspace(user).resolve("searches").resolve(id + ".csv");
                paths.putAll(getStoredSearchPaths(user, id, csv));
                break;
            }
        }

        // Gather mounts
        final Path workspacePath = userWorkspaceService.getUserWorkspace(user);
        final BindMount workspaceMount = BindMount.builder()
                .name("user-workspace")
                .writable(true)
                .xnatHostPath(workspacePath.toString())
                .containerHostPath(translatePath(workspacePath.toString()))
                .jupyterHostPath(Paths.get("/workspace", user.getUsername()).toString())
                .build();

        final List<BindMount> xnatDataMounts = paths.entrySet().stream()
                .map((entry) -> BindMount.builder()
                        .name(entry.getKey())
                        .writable(false)
                        .xnatHostPath(entry.getValue())
                        .containerHostPath(translatePath(entry.getValue()))
                        .jupyterHostPath(Paths.get(entry.getKey()).toString())
                        .build())
                .collect(Collectors.toList());

        final List<BindMount> mounts = new ArrayList<>(Collections.emptyList());
        mounts.add(workspaceMount);
        mounts.addAll(xnatDataMounts);

        // Get env variables
        Map<String, String> environmentVariables = getDefaultEnvironmentVariables(user, xsiType, id);

        // Store the user options
        UserOptionsEntity userOptionsEntity = UserOptionsEntity.builder()
                .userId(user.getID())
                .servername(servername)
                .xsiType(xsiType)
                .itemId(id)
                .projectId(projectId)
                .dockerImage(dockerImage)
                .environmentVariables(environmentVariables)
                .bindMountsJson(UserOptionsEntity.bindMountPojo(mounts))
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

}
