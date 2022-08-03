package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterHubUserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterServerAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
public class DefaultJupyterHubService implements JupyterHubService {

    private final JupyterHubPreferences jupyterHubPreferences;
    private final JupyterHubClient jupyterHubClient;
    private final SearchHelperServiceI searchHelperService;

    @Autowired
    public DefaultJupyterHubService(final JupyterHubPreferences jupyterHubPreferences,
                                    final JupyterHubClient jupyterHubClient,
                                    final SearchHelperServiceI searchHelperService) {
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.jupyterHubClient = jupyterHubClient;
        this.searchHelperService = searchHelperService;
    }

    @Override
    public Path getUserWorkspace(final UserI user) {
        final Path userWorkspacePath = Paths.get(jupyterHubPreferences.getWorkspacePath(), "users", user.getUsername());

        if (!Files.exists(userWorkspacePath)) {
            try {
                Files.createDirectories(userWorkspacePath);
            } catch (IOException e) {
                log.error("Unable to create Jupyter notebook workspace for user " + user.getUsername(), e);
                throw new RuntimeException(e);
            }
        }

        return userWorkspacePath;
    }

    @Override
    public User createUser(final UserI user) {
        return jupyterHubClient.createUser(user.getUsername());
    }

    @Override
    public Optional<User> getUser(final UserI user) {
        return jupyterHubClient.getUser(user.getUsername());
    }

    @Override
    public Optional<Server> getServer(final UserI user) {
        return jupyterHubClient.getServer(user.getUsername());
    }

    @Override
    public Optional<Server> getServer(final UserI user, final String servername) {
        return jupyterHubClient.getServer(user.getUsername(), servername);
    }

    @Override
    public Server startServer(final UserI user, final XnatUserOptions xnatUserOptions) throws NotFoundException, ResourceAlreadyExistsException {
        return startServer(user, "", xnatUserOptions);
    }

    @Override
    public Server startServer(final UserI user, String servername, final XnatUserOptions xnatUserOptions) throws NotFoundException, ResourceAlreadyExistsException {
        try {
            return jupyterHubClient.startServer(user.getUsername(), servername, xnatUserOptions);
        } catch (JupyterHubUserNotFoundException e) {
            throw new NotFoundException("Jupyter user " + user.getUsername() + " not found.");
        } catch (JupyterServerAlreadyExistsException e) {
            throw new ResourceAlreadyExistsException("jupyter server", servername);
        }
    }

    @Override
    public void stopServer(final UserI user) {
        jupyterHubClient.stopServer(user.getUsername());
    }

    @Override
    public void stopServer(final UserI user, final String servername) {
        jupyterHubClient.stopServer(user.getUsername(), servername);
    }

    @Override
    public Map<String, String> getProjectPaths(final UserI user, final List<String> projectIds) {
        Map<String, String> projectPaths = new HashMap<>();

        projectIds.forEach(projectId -> {
            XnatProjectdata xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(projectId, user, false);

            if (xnatProjectdata != null) {
                projectPaths.put(projectId, xnatProjectdata.getRootArchivePath() + xnatProjectdata.getCurrentArc());
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
                List<XnatSubjectassessordataI> subjectAssessors = xnatSubjectdata.getExperiments_experiment();

                subjectAssessors.forEach(subjectAssessor -> {
                    if (XnatExperimentdata.class.isAssignableFrom(subjectAssessor.getClass())) {
                        try {
                            final String assessorLabel = subjectAssessor.getLabel();
                            final String assessorId = subjectAssessor.getId();
                            final String path = ((XnatExperimentdata) subjectAssessor).getCurrentSessionFolder(true);

                            if (subjectIds.size() == 1) {
                                // For single subjects, use the assessor label as the key
                                subjectPaths.put(assessorLabel, path);
                            } else {
                                // For multiple subjects, use the assessor id as the key. If this method is called from
                                // a search there could be subjects in different projects with the same experiment
                                // labels.
                                subjectPaths.put(assessorId, path);
                            }
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

                    if (experimentIds.size() == 1) {
                        // For single experiments, use the label as the key
                        experimentPaths.put(experimentLabel, experimentPath);
                    } else {
                        // For multiple experiments, use the id as the key. If this method is called from
                        // a search there could be experiments in different projects with the same experiment
                        // labels.
                        experimentPaths.put(experimentId, experimentPath);
                    }
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

                if (imageScanPaths.size() == 1) {
                    // For single image scans, use the label as the key
                    imageScanPaths.put(imageScanLabel, imageScanPath);
                } else {
                    // For multiple scans, use the id as the key. If this method is called from a search there could be
                    // scans in different projects with the same labels
                    imageScanPaths.put(imageScanId.toString(), imageScanPath);
                }
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

        XdatStoredSearch storedSearch = null;

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
                    throw new RuntimeException(e);
                }

                storedSearch = displaySearch.convertToStoredSearch(storedSearchId);
                storedSearch.setId(storedSearchId);

                String description = storedSearchId.replace("@", "_")
                                       .replace(":", "_");

                storedSearch.setDescription(description);
                storedSearch.setBriefDescription(description);
            } else { // Project data bundle -> ProjectA@xnat:subjectData
                String[] splitId = storedSearchId.split("@");
                String projectId = splitId[0];
                String elementName = splitId[1];

                XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId, user, false);

                if (project != null) {
                    storedSearch = project.getDefaultSearch(elementName);

                    String description = storedSearchId.replace("@", "_")
                                           .replace(":", "_");

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
            // TODO
            log.error("", e);
            throw new RuntimeException(e);
        }

        return storedSearchPaths;
    }

    private boolean instanceOf(final String xsiType, final String instanceOfXsiType) {
        try {
            return GenericWrapperElement.GetElement(xsiType).instanceOf(instanceOfXsiType);
        } catch (ElementNotFoundException  e) {
            return false;
        } catch (XFTInitException e) {
            throw new RuntimeException(e);
        }
    }

}
