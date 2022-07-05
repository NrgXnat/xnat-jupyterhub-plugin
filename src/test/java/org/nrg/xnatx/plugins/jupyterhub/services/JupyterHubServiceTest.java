package org.nrg.xnatx.plugins.jupyterhub.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterHubUserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.JupyterServerAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.dtos.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubServiceConfig.class)
public class JupyterHubServiceTest {

    @Autowired private JupyterHubService jupyterHubService;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;
    @Autowired private JupyterHubClient mockJupyterHubClient;

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UserI user;
    private String username;
    private String servername;
    private Server jupyterServerDefault;
    private Server jupyterServerNamed;
    private User jupyterUser;
    private XnatUserOptions xnatUserOptions;

    @Before
    public void before() throws IOException {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);

        // Mock the Jupyter Server
        servername = "jupyter-server-1";

        // Mock JupyterHubPreferences
        File userWorkspaces = temporaryFolder.newFolder("workspaces", "users");
        when(mockJupyterHubPreferences.getWorkspacePath()).thenReturn(userWorkspaces.getAbsolutePath());

        // Mock Jupyter User, Jupyter Servers, and JupyterHubClient
        jupyterServerDefault = Server.builder().name("").build();
        jupyterServerNamed = Server.builder().name(servername).build();

        Map<String, Server> jupyterServers = new HashMap<>();
        jupyterServers.put(jupyterServerDefault.getName(), jupyterServerDefault);
        jupyterServers.put(jupyterServerNamed.getName(), jupyterServerDefault);

        jupyterUser = User.builder().name(username)
                                    .servers(jupyterServers).build();

        xnatUserOptions = XnatUserOptions.builder().servername(servername)
                                                   .xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME)
                                                   .id("TestProject").build();

        when(mockJupyterHubClient.createUser(username)).thenReturn(jupyterUser);
    }

    @After
    public void after() {
        Mockito.reset(mockJupyterHubClient);
    }

    @Test
    public void testUserWorkspacePath() {
        // Test
        Path userWorkspace = jupyterHubService.getUserWorkspace(user);

        // Verify user workspace was created and that it is in their workspace directory
        assertTrue(Files.exists(userWorkspace));
        assertTrue(userWorkspace.toString().endsWith(mockJupyterHubPreferences.getWorkspacePath() + "/users/" + username));
    }

    @Test
    public void testCreateUser() {
        // Test
        User testUser = jupyterHubService.createUser(user);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).createUser(username);
        assertEquals(username, testUser.getName());
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
    public void testStartServer_NoExceptions() throws Exception {
        // Should convert JupyterHub exceptions to XAPI exceptions
        when(mockJupyterHubClient.startServer(username, jupyterServerNamed.getName(), xnatUserOptions)).thenReturn(jupyterServerDefault);

        // Test
        jupyterHubService.startServer(user, jupyterServerNamed.getName(), xnatUserOptions);

        // Verify the jupyter hub client method was called as expected and no exceptions thrown
        verify(mockJupyterHubClient).startServer(username, jupyterServerNamed.getName(), xnatUserOptions);
    }

    @Test(expected = NotFoundException.class)
    public void testStartServer_UserNotFound() throws Exception {
        // Should convert JupyterHub exceptions to XAPI exceptions
        when(mockJupyterHubClient.startServer(username, jupyterServerNamed.getName(), xnatUserOptions)).thenThrow(new JupyterHubUserNotFoundException(username));

        // Test
        jupyterHubService.startServer(user, jupyterServerNamed.getName(), xnatUserOptions);
    }

    @Test(expected = ResourceAlreadyExistsException.class)
    public void testStartServer_ServerAlreadyExists() throws Exception {
        // Should convert JupyterHub exceptions to XAPI exceptions
        when(mockJupyterHubClient.startServer(username, jupyterServerNamed.getName(), xnatUserOptions)).thenThrow(new JupyterServerAlreadyExistsException(username, servername));

        // Test
        jupyterHubService.startServer(user, jupyterServerNamed.getName(), xnatUserOptions);
    }

    @Test
    public void testStopSever() {
        // Test
        jupyterHubService.stopServer(user);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).stopServer(username);
    }

    @Test
    public void testStopNamedServer() {
        // Test
        jupyterHubService.stopServer(user, servername);

        // Verify the jupyter hub client method was called as expected
        verify(mockJupyterHubClient).stopServer(username, servername);
    }

}