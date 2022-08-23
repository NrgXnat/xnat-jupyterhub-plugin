package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultPermissionsHelper implements PermissionsHelper {

    private final SearchHelperServiceI searchHelperService;
    private final PermissionsServiceI permissionsService;

    @Autowired
    public DefaultPermissionsHelper(final SearchHelperServiceI searchHelperService,
                                    final PermissionsServiceI permissionsService) {
        this.searchHelperService = searchHelperService;
        this.permissionsService = permissionsService;
    }

    /**
     * Can the user read a stored search, a project, or the subject or experiment within the context of the project.
     *
     * @param user        The user for which to retrieve permissions.
     * @param projectId   The ID of the project. Can be null for stored searches.
     * @param entityId    Represents the ID of the stored search, subject, or experiment object.
     * @param xsiType     xdat:stored_search, xnat:projectData, xnat:subjectData, or an xnat:experimentData
     *
     * @return True if the user can read the stored search, the project, or the subject or experiment within the context
     *         of the project.
     */
    @Override
    public boolean canRead(UserI user, String projectId, String entityId, String xsiType) {
        if (xsiType.equals(XdatStoredSearch.SCHEMA_ELEMENT_NAME)) {
            return canReadStoredSearch(user, entityId);
        } else {
            return canReadProjSubjExp(user, projectId, entityId, xsiType);
        }
    }

    /**
     * Can the user read the provided stored search.
     *
     * @param user              The user for which to retrieve permissions.
     * @param storedSearchId    The ID of the stored search
     *
     * @return True if the user can read the stored search.
     */
    private boolean canReadStoredSearch(final UserI user, final String storedSearchId) {
        return searchHelperService.getSearchForUser(user, storedSearchId) != null;
    }

    /**
     * Can the user read the project, or the subject or experiment with the specified ID in the context of the project.
     *
     * @param user          The user for which to retrieve permissions.
     * @param projectId     The ID of the project.
     * @param entityId      Represents the ID of the subject or experiment object.
     * @param xsiType       xnat:projectData, xnat:subjectData, or an xnat:experimentData
     *
     * @return True if user can read the project, or subject/experiment in the context of the project
     */
    private boolean canReadProjSubjExp(final UserI user, final String projectId, final String entityId, final String xsiType) {
        boolean can = false;
        if (xsiType != null) {
            if (xsiType.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                try {
                    can = permissionsService.canRead(user, "xnat:projectData/ID", (Object) projectId);
                } catch (Exception e) {
                    log.error("Could not check read permissions for user \"{}\" project \"{}\" xsiType \"{}\"",
                              user.getUsername(), projectId, xsiType, e);
                }
            } else {
                try {
                    can = permissionsService.canRead(user, projectId, entityId);
                } catch (Exception e) {
                    log.error("Could not check read permissions for user \"{}\" project \"{}\" xsiType \"{}\" entity id \"{}\"",
                              user.getUsername(), projectId, xsiType, entityId, e);
                }
            }
        }
        log.debug("User \"{}\" can{} read type \"{}\" in project \"{}\"",
                  user.getUsername(), can ? "" : "not", xsiType, projectId);
        return can;
    }

}
