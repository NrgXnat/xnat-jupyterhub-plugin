package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultJupyterHubServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEventI;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultJupyterHubServiceConfig.class)
public class DefaultJupyterHubServiceTest {

    @Autowired private DefaultJupyterHubService jupyterHubService;
    @Autowired private JupyterHubClient mockJupyterHubClient;
    @Autowired private PermissionsHelper mockPermissionsHelper;
    @Autowired private NrgEventServiceI mockEventService;
    @Autowired private UserOptionsEntityService mockUserOptionsEntityService;
    @Autowired private UserOptionsService mockUserOptionsService;

    @Captor ArgumentCaptor<JupyterServerEventI> jupyterServerEventCaptor;

    private UserI user;
    private String username;
    private final String servername = "";
    private final String eventTrackingId = "eventTrackingId";
    private final String projectId = "TestProject";

    @Before
    public void before() {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);

        // Capture Jupyter events
        jupyterServerEventCaptor = ArgumentCaptor.forClass(JupyterServerEventI.class);
    }

    @After
    public void after() {
        Mockito.reset(mockJupyterHubClient);
        Mockito.reset(mockEventService);
        Mockito.reset(mockPermissionsHelper);
        Mockito.reset(mockUserOptionsEntityService);
    }

    @Test
    public void testCreateUser() {
        // Test
        jupyterHubService.createUser(user);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).createUser(username);
    }

    @Test
    public void testGetUser() {
        // Test
        jupyterHubService.getUser(user);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getUser(username);
    }

    @Test
    public void testGetServer() {
        // Test
        jupyterHubService.getServer(user);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getServer(username);
    }

    @Test
    public void testGetNamedServer() {
        // Test
        jupyterHubService.getServer(user, servername);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getServer(username, servername);
    }

    @Test
    public void testStartServer_cantReadProject() throws Exception {
        // Can't read project
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(false);

        // Test
        jupyterHubService.startServer(user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, eventTrackingId);

        // Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());

        // Verify user options are not saved
        verify(mockUserOptionsEntityService, never()).createOrUpdate(any());

        // Verify no attempts to start a server
        verify(mockJupyterHubClient, never()).startServer(any(), any(), any());
    }

    @Test
    public void testStartServer_cantReadSubject() throws Exception {
        // Can't read subject
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(false);

        // Test
        String subjectId = "XNAT_S00001";
        jupyterHubService.startServer(user, XnatSubjectdata.SCHEMA_ELEMENT_NAME, subjectId, projectId, eventTrackingId);

        //Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());

        // Verify user options are not saved
        verify(mockUserOptionsEntityService, never()).createOrUpdate(any());

        // Verify no attempts to start a server
        verify(mockJupyterHubClient, never()).startServer(any(), any(), any());
    }

    @Test
    public void testStartServer_cantReadExperiment() throws Exception {
        // Can't read experiment
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(false);

        // Test
        String experimentId = "XNAT_E00001";
        jupyterHubService.startServer(user, XnatExperimentdata.SCHEMA_ELEMENT_NAME, experimentId, projectId, eventTrackingId);

        //Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());

        // Verify user options are not saved
        verify(mockUserOptionsEntityService, never()).createOrUpdate(any());

        // Verify no attempts to start a server
        verify(mockJupyterHubClient, never()).startServer(any(), any(), any());
    }

    @Test
    public void testStartServer_cantReadScan() throws Exception {
        // Can't read experiment
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(false);

        // Test
        jupyterHubService.startServer(user, XnatImagescandata.SCHEMA_ELEMENT_NAME, "456", projectId, eventTrackingId);

        //Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());

        // Verify user options are not saved
        verify(mockUserOptionsEntityService, never()).createOrUpdate(any());

        // Verify no attempts to start a server
        verify(mockJupyterHubClient, never()).startServer(any(), any(), any());
    }

    @Test(timeout = 3500)
    public void testStartServer_serverAlreadyRunning() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Setup existing server
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.of(Server.builder().build()));

        // Test
        jupyterHubService.startServer(user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, eventTrackingId);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());

        // Verify user options are not saved
        verify(mockUserOptionsEntityService, never()).createOrUpdate(any());

        // Verify no attempts to start a server
        verify(mockJupyterHubClient, never()).startServer(any(), any(), any());
    }

    @Test(timeout = 8000)
    public void testStartServer_Timeout() throws UserNotFoundException, ResourceAlreadyExistsException, InterruptedException {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // To successfully start a server there should be no running servers at first.
        // A subsequent call should contain a server, but let's presume it is never ready
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.empty());

        // Test
        jupyterHubService.startServer(user, XnatProjectdata.SCHEMA_ELEMENT_NAME, projectId, projectId, eventTrackingId);
        Thread.sleep(6000); // Async call, need to wait. Is there a better way to test this?

        // Verify user options are stored
        verify(mockUserOptionsService, times(1)).storeUserOptions(eq(user), eq(""), eq(XnatProjectdata.SCHEMA_ELEMENT_NAME), eq(projectId), eq(projectId));

        // Verify JupyterHub start server request sent
        verify(mockJupyterHubClient, times(1)).startServer(eq(username), eq(""), any(UserOptions.class));

        // Verify attempts to get server from JupyterHub
        verify(mockJupyterHubClient, atLeast(2)).getServer(eq(username), eq(""));

        // Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());
    }

    @Test(timeout = 8000)
    public void testStartServer_Success() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // To successfully start a server there should be no running servers at first.
        // A subsequent call should eventually return a server in the ready state
        when(mockJupyterHubClient.getServer(anyString(), anyString()))
                .thenReturn(Optional.empty(), Optional.of(Server.builder().ready(true).build()));

        // Test
        jupyterHubService.startServer(user, XnatProjectdata.SCHEMA_ELEMENT_NAME, "TestProject", "TestProject", eventTrackingId);
        Thread.sleep(6000); // Async call, need to wait. Is there a better way to test this?

        // Verify user options are stored
        verify(mockUserOptionsService, times(1)).storeUserOptions(user, "", XnatProjectdata.SCHEMA_ELEMENT_NAME, "TestProject", "TestProject");

        // Verify JupyterHub start server request sent
        verify(mockJupyterHubClient, times(1)).startServer(eq(username), eq(""), any(UserOptions.class));

        // Should be at two calls to JupyterHub, first to see no server is running, second to see it does after starting
        verify(mockJupyterHubClient, times(2)).getServer(username, "");

        // Verify start completed event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Completed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());
    }

    @Test(timeout = 3000)
    public void testStopSever_Failure() throws Exception {
        // Returning a server should lead to a failure to stop event
        when(mockJupyterHubClient.getServer(anyString(), anyString()))
                .thenReturn(Optional.of(Server.builder().build()));

        // Test
        jupyterHubService.stopServer(user, eventTrackingId);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify one attempt to stop the sever
        verify(mockJupyterHubClient, times(1)).stopServer(username, servername);

        // Verify attempts to see if server stopped
        verify(mockJupyterHubClient, times(2)).getServer(username, servername);

        // Verify failure to stop event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Stop, capturedEvent.getOperation());
    }

    @Test(timeout = 3000)
    public void testStopSever_Success() throws Exception {
        // Returning an empty should lead to a failure to start event
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.empty());

        // Test
        jupyterHubService.stopServer(user, eventTrackingId);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify one attempt to stop the sever
        verify(mockJupyterHubClient, times(1)).stopServer(username, servername);

        // Verify one attempt to see if server stopped
        verify(mockJupyterHubClient, times(1)).getServer(username, servername);

        // Verify stop completed event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Completed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Stop, capturedEvent.getOperation());
    }

}