package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultPermissionsHelperConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultPermissionsHelperConfig.class)
public class DefaultPermissionsHelperTest {

    @Autowired private DefaultPermissionsHelper permissionsHelper;
    @Autowired private SearchHelperServiceI mockSearchHelperService;
    @Autowired private PermissionsServiceI mockPermissionsService;

    private final UserI user = mock(UserI.class);
    private final String projectId = "TestProject";
    private final String subjectId = "XNAT_S00001";
    private final String experimentId = "XNAT_E00001";
    private final String siteStoredSearchId = "xs12345653";
    private final String projectStoredSearchId = "xs123452211";
    private final String siteDataSearchId = "@xnat:subjectData";
    private final String projectDataSearchId = "@xnat:subjectData";

    @After
    public void after() {
        Mockito.reset(mockSearchHelperService);
        Mockito.reset(mockPermissionsService);
    }

    @Test
    public void testCanReadSiteStoredSearch() {
        // Setup
        XdatStoredSearch ss = new XdatStoredSearch(user);
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(ss);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, null, siteStoredSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testCantReadSiteStoredSearch() {
        // Setup
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(null);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, null, siteStoredSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCanReadProjectStoredSearch() throws Exception {
        // Setup stored search
        XdatStoredSearch ss = new XdatStoredSearch(user);
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(ss);

        // Setup project permission
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(true);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectStoredSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testCantReadProjectStoredSearch() throws Exception {
        // Setup stored search
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(null);

        // Setup project permission
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(false);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectDataSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCanReadSiteDataSearch() {
        // Test
        final boolean canRead = permissionsHelper.canRead(user, null, siteDataSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testCanReadProjectDataSearch() throws Exception {
        // Setup stored search
        XdatStoredSearch ss = new XdatStoredSearch(user);
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(ss);

        // Setup project permission
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(true);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectDataSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testCantReadProjectDataSearch() throws Exception {
        // Setup stored search
        XdatStoredSearch ss = new XdatStoredSearch(user);
        when(mockSearchHelperService.getSearchForUser(any(UserI.class), anyString())).thenReturn(ss);

        // Setup project permission
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(false);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectDataSearchId, XdatStoredSearch.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCantReadProject() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(false);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectId, XnatProjectdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCanReadProject() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenReturn(true);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, projectId, XnatProjectdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testProjectPermissionServiceExceptions() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq("xnat:projectData/ID"), any(Object.class))).thenThrow(Exception.class);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, experimentId, XnatExperimentdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCantReadSubject() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(subjectId))).thenReturn(false);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, subjectId, XnatSubjectdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCanReadSubject() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(subjectId))).thenReturn(true);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, subjectId, XnatSubjectdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testSubjectPermissionServiceExceptions() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(subjectId))).thenThrow(Exception.class);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, experimentId, XnatExperimentdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCantReadExperiment() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(experimentId))).thenReturn(false);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, experimentId, XnatExperimentdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

    @Test
    public void testCanReadExperiment() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(experimentId))).thenReturn(true);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, experimentId, XnatExperimentdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertTrue(canRead);
    }

    @Test
    public void testExperimentPermissionServiceExceptions() throws Exception {
        // Setup
        when(mockPermissionsService.canRead(any(UserI.class), eq(projectId), eq(experimentId))).thenThrow(Exception.class);

        // Test
        final boolean canRead = permissionsHelper.canRead(user, projectId, experimentId, XnatExperimentdata.SCHEMA_ELEMENT_NAME);

        // Assert
        assertFalse(canRead);
    }

}