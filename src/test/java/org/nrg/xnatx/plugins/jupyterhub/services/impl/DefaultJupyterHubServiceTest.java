package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xnat.compute.services.JobTemplateService;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Token;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultJupyterHubServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEventI;
import org.nrg.xnatx.plugins.jupyterhub.models.ServerStartRequest;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;
    @Autowired private UserManagementServiceI mockUserManagementServiceI;
    @Autowired private JobTemplateService mockJobTemplateService;

    @Captor ArgumentCaptor<JupyterServerEventI> jupyterServerEventCaptor;
    @Captor ArgumentCaptor<Token> tokenArgumentCaptor;

    private UserI user;
    private String username;
    private final String servername = "";
    private final String eventTrackingId = "eventTrackingId";
    private final String projectId = "TestProject";
    private final Long computeEnvironmentConfigId = 3L;
    private final Long hardwareConfigId = 2L;
    private User userWithServers;
    private User userNoServers;

    private ServerStartRequest startProjectRequest;
    private ServerStartRequest startSubjectRequest;
    private ServerStartRequest startExperimentRequest;
    private ServerStartRequest startScanRequest;

    @Before
    public void before() throws org.nrg.xdat.security.user.exceptions.UserNotFoundException, UserInitException {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);
        when(mockUserManagementServiceI.getUser(eq(username))).thenReturn(user);

        Server server = Server.builder()
                .name(servername)
                .build();

        userWithServers = User.builder()
                .name(username)
                .servers(Collections.singletonMap("", server))
                .build();

        userNoServers = User.builder()
                .name(username)
                .servers(Collections.emptyMap())
                .build();

        // Setup start project request
        startProjectRequest = ServerStartRequest.builder()
                .username(username)
                .servername(servername)
                .xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME)
                .itemId(projectId)
                .itemLabel(projectId)
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .computeEnvironmentConfigId(computeEnvironmentConfigId)
                .hardwareConfigId(hardwareConfigId)
                .build();

        // Setup start subject request
        startSubjectRequest = ServerStartRequest.builder()
                .username(username)
                .servername(servername)
                .xsiType(XnatSubjectdata.SCHEMA_ELEMENT_NAME)
                .itemId("XNAT_S00001")
                .itemLabel("Subject1")
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .computeEnvironmentConfigId(computeEnvironmentConfigId)
                .hardwareConfigId(hardwareConfigId)
                .build();

        // Setup start experiment request
        startExperimentRequest = ServerStartRequest.builder()
                .username(username)
                .servername(servername)
                .xsiType(XnatExperimentdata.SCHEMA_ELEMENT_NAME)
                .itemId("XNAT_E00001")
                .itemLabel("Experiment1")
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .computeEnvironmentConfigId(computeEnvironmentConfigId)
                .hardwareConfigId(hardwareConfigId)
                .build();

        // Setup start scan request
        startScanRequest = ServerStartRequest.builder()
                .username(username)
                .servername(servername)
                .xsiType(XnatImagescandata.SCHEMA_ELEMENT_NAME)
                .itemId("XNAT_S00001")
                .itemLabel("Scan1")
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .computeEnvironmentConfigId(computeEnvironmentConfigId)
                .hardwareConfigId(hardwareConfigId)
                .build();

        // Capture Jupyter events
        jupyterServerEventCaptor = ArgumentCaptor.forClass(JupyterServerEventI.class);
        tokenArgumentCaptor = ArgumentCaptor.forClass(Token.class);

        // Polling rate and timeout
        when(mockJupyterHubPreferences.getStartTimeout()).thenReturn(2);
        when(mockJupyterHubPreferences.getStartPollingInterval()).thenReturn(1);
        when(mockJupyterHubPreferences.getStopTimeout()).thenReturn(2);
        when(mockJupyterHubPreferences.getStopPollingInterval()).thenReturn(1);
    }

    @After
    public void after() {
        Mockito.reset(mockJupyterHubClient);
        Mockito.reset(mockEventService);
        Mockito.reset(mockPermissionsHelper);
        Mockito.reset(mockUserOptionsEntityService);
        Mockito.reset(mockUserManagementServiceI);
    }

    @Test
    public void testGetVersion() {
        // Test
        jupyterHubService.getVersion();

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getVersion();
    }

    @Test
    public void testGetInfo() {
        // Test
        jupyterHubService.getInfo();

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getInfo();
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
    public void testGetUsers() {
        // Test
        jupyterHubService.getUsers();

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).getUsers();
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
        jupyterHubService.startServer(user, startProjectRequest);

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
        jupyterHubService.startServer(user, startSubjectRequest);

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
        jupyterHubService.startServer(user, startExperimentRequest);

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
        jupyterHubService.startServer(user, startScanRequest);

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

    @Test(timeout = 2000)
    public void testStartServer_HubOffline() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Connection to JupyterHub failed
        when(mockJupyterHubClient.getVersion()).thenThrow(RuntimeException.class);

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(1000); // Async call, need to wait. Is there a better way to test this?

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

    @Test(timeout = 2000)
    public void testStartServer_HubOnlineButXNATCantConnect() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Can't getInfo from JupyterHub, but connection to JupyterHub succeeded
        when(mockJupyterHubClient.getVersion()).thenReturn(null);
        when(mockJupyterHubClient.getInfo()).thenThrow(RuntimeException.class);

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(1000); // Async call, need to wait. Is there a better way to test this?

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

    @Test(timeout = 2000)
    public void testStartServer_serverAlreadyRunning() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Setup existing server
        when(mockJupyterHubClient.getUser(anyString())).thenReturn(Optional.of(userWithServers));

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(1000); // Async call, need to wait. Is there a better way to test this?

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

    @Test(timeout = 2000)
    public void testStartServer_jobTemplateUnavailable() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Job template unavailable
        when(mockJobTemplateService.isAvailable(any(), any(), any())).thenReturn(false);

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(1000); // Async call, need to wait. Is there a better way to test this?

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

    @Test(timeout = 4000)
    public void testStartServer_Timeout() throws UserNotFoundException, ResourceAlreadyExistsException, InterruptedException {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Job template is available
        when(mockJobTemplateService.isAvailable(any(), any(), any())).thenReturn(true);

        // To successfully start a server there should be no running servers at first.
        // A subsequent call should contain a server, but let's presume it is never ready
        when(mockJupyterHubClient.getUser(anyString())).thenReturn(Optional.of(userNoServers));
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.empty());

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(3000); // Async call, need to wait. Is there a better way to test this?

        // Verify user options are stored
        verify(mockUserOptionsService, times(1)).storeUserOptions(eq(user), eq(""), eq(XnatProjectdata.SCHEMA_ELEMENT_NAME),
                                                                  eq(projectId), eq(projectId), eq(computeEnvironmentConfigId),
                                                                  eq(hardwareConfigId), eq(eventTrackingId));

        // Verify JupyterHub start server request sent
        verify(mockJupyterHubClient, times(1)).startServer(eq(username), eq(""), any(UserOptions.class));

        // Verify 2 attempts to get server from JupyterHub with polling rate and timeout
        verify(mockJupyterHubClient, times(2)).getServer(eq(username), eq(""));

        // Verify failure to start event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Failed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());
    }

    @Test(timeout = 3000)
    public void testStartServer_Success() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Job template is available
        when(mockJobTemplateService.isAvailable(any(), any(), any())).thenReturn(true);

        // To successfully start a server there should be no running servers at first.
        // A subsequent call should eventually return a server in the ready state
        when(mockJupyterHubClient.getUser(anyString())).thenReturn(Optional.of(userNoServers));
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.of(Server.builder().ready(true).build()));

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify user options are stored
        verify(mockUserOptionsService, times(1)).storeUserOptions(eq(user), eq(""), eq(XnatProjectdata.SCHEMA_ELEMENT_NAME),
                                                                  eq(projectId), eq(projectId), eq(computeEnvironmentConfigId),
                                                                  eq(hardwareConfigId), eq(eventTrackingId));

        // Verify JupyterHub start server request sent
        verify(mockJupyterHubClient, times(1)).startServer(eq(username), eq(""), any(UserOptions.class));

        // Verify start completed event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Completed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Start, capturedEvent.getOperation());
    }

    @Test(timeout = 3000)
    public void testStartServer_CreateUser_Success() throws Exception {
        // Grant permissions
        when(mockPermissionsHelper.canRead(any(), anyString(), anyString(), anyString())).thenReturn(true);

        // Job template is available
        when(mockJobTemplateService.isAvailable(any(), any(), any())).thenReturn(true);

        // To successfully start a server there should be no running servers at first.
        // A subsequent call should eventually return a server in the ready state
        // Start with a user who does not yet exist on JupyterHub
        when(mockJupyterHubClient.getUser(anyString())).thenReturn(Optional.empty());
        when(mockJupyterHubClient.createUser(anyString())).thenReturn(userNoServers);
        when(mockJupyterHubClient.getServer(anyString(), anyString())).thenReturn(Optional.of(Server.builder().ready(true).build()));

        // Test
        jupyterHubService.startServer(user, startProjectRequest);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify create user attempt
        verify(mockJupyterHubClient, times(1)).createUser(anyString());

        // Verify user options are stored
        verify(mockUserOptionsService, times(1)).storeUserOptions(eq(user), eq(""), eq(XnatProjectdata.SCHEMA_ELEMENT_NAME),
                                                                  eq(projectId), eq(projectId), eq(computeEnvironmentConfigId),
                                                                  eq(hardwareConfigId), eq(eventTrackingId));

        // Verify JupyterHub start server request sent
        verify(mockJupyterHubClient, times(1)).startServer(eq(username), eq(""), any(UserOptions.class));

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
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(Server.builder().build()));

        // Test
        jupyterHubService.stopServer(user, eventTrackingId);
        Thread.sleep(2500); // Async call, need to wait. Is there a better way to test this?

        // Verify one attempt to stop the sever
        verify(mockJupyterHubClient, atLeastOnce()).stopServer(username, servername);

        // Verify attempts to see if server stopped
        verify(mockJupyterHubClient, atLeast(2)).getServer(username, servername);

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
        Thread.sleep(2000); // Async call, need to wait. Is there a better way to test this?

        // Verify one attempt to stop the sever
        verify(mockJupyterHubClient, times(1)).stopServer(username, servername);

        // Verify at least one attempt to see if server stopped
        verify(mockJupyterHubClient, atLeastOnce()).getServer(username, servername);

        // Verify stop completed event occurred
        verify(mockEventService, atLeastOnce()).triggerEvent(jupyterServerEventCaptor.capture());
        JupyterServerEventI capturedEvent = jupyterServerEventCaptor.getValue();
        assertEquals(JupyterServerEventI.Status.Completed, capturedEvent.getStatus());
        assertEquals(JupyterServerEventI.Operation.Stop, capturedEvent.getOperation());
    }

    @Test
    public void testCreateToken() {
        // Setup
        final String note = "token note";
        final int expiresIn = 60;
        final String scope = "access:servers!user=" + username;

        // Test
        jupyterHubService.createToken(user, note, expiresIn);

        // Verify
        verify(mockJupyterHubClient, times(1)).createToken(eq(username), tokenArgumentCaptor.capture());
        Token tokenArgumentCaptorValue = tokenArgumentCaptor.getValue();
        assertEquals(note, tokenArgumentCaptorValue.getNote());
        assertEquals(expiresIn, tokenArgumentCaptorValue.getExpires_in());
        assertEquals(Collections.singletonList(scope), tokenArgumentCaptorValue.getScopes());
    }

    @Test(timeout = 3000)
    public void testCullIdleServers_Cull() throws Exception {
        // Setup
        when(mockJupyterHubPreferences.getInactivityTimeout()).thenReturn(90L);

        Server server_active = Server.builder()
                .name("server_active")
                .last_activity(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        Server server_inactive = Server.builder()
                .name("server_inactive")
                .last_activity(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(2L))
                .build();

        Map<String, Server> servers = new HashMap<>();
        servers.put("server_active", server_active);
        servers.put("server_inactive", server_inactive);

        User user = User.builder()
                .name(username)
                .servers(servers)
                .build();

        when(mockJupyterHubClient.getUsers()).thenReturn(Collections.singletonList(user));

        // Test
        jupyterHubService.cullInactiveServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify active server not stopped
        verify(mockJupyterHubClient, never()).stopServer(eq(username), eq(server_active.getName()));

        // Verify inactive server stopped
        verify(mockJupyterHubClient, times(1)).stopServer(eq(username), eq(server_inactive.getName()));
    }

    @Test(timeout = 3000)
    public void testCullIdleServers_NoCull() throws InterruptedException {
        // Setup -> timeout set to zero
        when(mockJupyterHubPreferences.getInactivityTimeout()).thenReturn(0L);

        // Test
        jupyterHubService.cullInactiveServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify no servers stopped
        verify(mockJupyterHubClient, never()).stopServer(any(), any());
    }

    @Test(timeout = 3000)
    public void testCullIdleServers_Exception() throws InterruptedException {
        // Setup
        when(mockJupyterHubPreferences.getInactivityTimeout()).thenReturn(90L);
        when(mockJupyterHubClient.getUsers()).thenThrow(new RuntimeException("Unable to connect to JupyterHub"));

        // Test
        jupyterHubService.cullInactiveServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify no servers stopped
        verify(mockJupyterHubClient, never()).stopServer(any(), any());
    }

    @Test(timeout = 3000)
    public void testCullLongRunningServers_Cull() throws InterruptedException {
        // Setup
        when(mockJupyterHubPreferences.getMaxServerLifetime()).thenReturn(48L);

        Server server_active = Server.builder()
                .name("server_active")
                .started(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        Server server_long_running = Server.builder()
                .name("server_long_running")
                .started(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(48L).minusMinutes(1L))
                .build();

        Map<String, Server> servers = new HashMap<>();
        servers.put(server_active.getName(), server_active);
        servers.put(server_long_running.getName(), server_long_running);

        User user = User.builder()
                .name(username)
                .servers(servers)
                .build();

        when(mockJupyterHubClient.getUsers()).thenReturn(Collections.singletonList(user));

        // Test
        jupyterHubService.cullLongRunningServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify active server not stopped
        verify(mockJupyterHubClient, never()).stopServer(eq(username), eq(server_active.getName()));

        // Verify inactive server stopped
        verify(mockJupyterHubClient, times(1)).stopServer(eq(username), eq(server_long_running.getName()));
    }

    @Test(timeout = 3000)
    public void testCullLongRunningServers_NoCull_Disabled() throws InterruptedException {
        // Setup -> timeout set to zero
        when(mockJupyterHubPreferences.getMaxServerLifetime()).thenReturn(0L);

        // Test
        jupyterHubService.cullLongRunningServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify servers not stopped
        verify(mockJupyterHubClient, never()).stopServer(any(), any());
    }

    @Test(timeout = 3000)
    public void testCullLongRunningServers_NoCull() throws InterruptedException {
        // Setup
        when(mockJupyterHubPreferences.getMaxServerLifetime()).thenReturn(48L);

        Server server_active = Server.builder()
                .name("server_active")
                .started(ZonedDateTime.now(ZoneId.of("UTC")))
                .build();

        // Server started less than 48 hours ago
        Server server_long_running = Server.builder()
                .name("server_long_running")
                .started(ZonedDateTime.now(ZoneId.of("UTC")).minusHours(47L).minusMinutes(59L))
                .build();

        Map<String, Server> servers = new HashMap<>();
        servers.put(server_active.getName(), server_active);
        servers.put(server_long_running.getName(), server_long_running);

        User user = User.builder()
                .name(username)
                .servers(servers)
                .build();

        when(mockJupyterHubClient.getUsers()).thenReturn(Collections.singletonList(user));

        // Test
        jupyterHubService.cullLongRunningServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify active server not stopped
        verify(mockJupyterHubClient, never()).stopServer(eq(username), eq(server_active.getName()));

        // Verify inactive server not stopped. Timeout is set to zero
        verify(mockJupyterHubClient, never()).stopServer(eq(username), eq(server_long_running.getName()));
    }

    @Test(timeout = 3000)
    public void testCullLongRunningServers_Exception() throws InterruptedException {
        // Setup
        when(mockJupyterHubPreferences.getMaxServerLifetime()).thenReturn(48L);
        when(mockJupyterHubClient.getUsers()).thenThrow(new RuntimeException("Unable to connect to JupyterHub"));

        // Test
        jupyterHubService.cullLongRunningServers();
        Thread.sleep(2000); // Stop server is async call, need to wait.

        // Verify no server stopped
        verify(mockJupyterHubClient, never()).stopServer(any(), any());

    }
}