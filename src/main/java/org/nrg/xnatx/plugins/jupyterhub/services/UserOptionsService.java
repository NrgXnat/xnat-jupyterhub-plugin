package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserOptionsService {

    Map<String, String> getProjectPaths(UserI user, List<String> projectIds) throws BaseXnatExperimentdata.UnknownPrimaryProjectException, DBPoolException, SQLException, InvalidArchiveStructure, IOException;
    Map<String, String> getProjectPaths(UserI user, List<String> projectIds, String eventTrackingId) throws BaseXnatExperimentdata.UnknownPrimaryProjectException, DBPoolException, SQLException, InvalidArchiveStructure, IOException;
    Map<String, String> getSubjectPaths(UserI user, String subjectId);
    Map<String, String> getSubjectPaths(UserI user, List<String> subjectIds);
    Map<String, String> getExperimentPath(UserI user, String experimentId);
    Map<String, String> getExperimentPaths(UserI user, List<String> experimentIds);
    Map<String, String> getImageScanPath(UserI user, Integer imageScanId);
    Map<String, String> getImageScanPaths(UserI user, List<Integer> imageScanIds);
    Map<String, String> getStoredSearchPaths(UserI user, String storedSearchId, String projectId) throws BaseXnatExperimentdata.UnknownPrimaryProjectException, DBPoolException, SQLException, InvalidArchiveStructure, IOException;

    Optional<XnatUserOptions> retrieveUserOptions(UserI user);
    Optional<XnatUserOptions> retrieveUserOptions(UserI user, String servername);
    void storeUserOptions(UserI user, String servername, String xsiType, String id, String projectId, Long computeEnvironmentConfigId, Long hardwareConfigId, String eventTrackingId) throws BaseXnatExperimentdata.UnknownPrimaryProjectException, DBPoolException, SQLException, InvalidArchiveStructure, IOException;

}
