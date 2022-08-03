package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface JupyterHubService {

    Path getUserWorkspace(UserI user);
    User createUser(UserI user);
    Optional<User> getUser(UserI user);
    Optional<Server> getServer(UserI user);
    Optional<Server> getServer(UserI user, String servername);
    Server startServer(UserI user, XnatUserOptions xnatUserOptions) throws NotFoundException, ResourceAlreadyExistsException;
    Server startServer(UserI user, String servername, XnatUserOptions xnatUserOptions) throws NotFoundException, ResourceAlreadyExistsException;
    void stopServer(UserI user);
    void stopServer(UserI user, String servername);

    Map<String, String> getProjectPaths(UserI user, List<String> projectIds);
    Map<String, String> getSubjectPaths(UserI user, String subjectId);
    Map<String, String> getSubjectPaths(UserI user, List<String> subjectIds);
    Map<String, String> getExperimentPath(UserI user, String experimentId);
    Map<String, String> getExperimentPaths(UserI user, List<String> experimentIds);
    Map<String, String> getImageScanPath(UserI user, Integer imageScanId);
    Map<String, String> getImageScanPaths(UserI user, List<Integer> imageScanIds);

    Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId);
    Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId, Path csv);
}
