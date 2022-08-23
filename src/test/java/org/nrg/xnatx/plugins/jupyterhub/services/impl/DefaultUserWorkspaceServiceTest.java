package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultUserWorkspaceServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultUserWorkspaceServiceConfig.class)
public class DefaultUserWorkspaceServiceTest {

    @Autowired private DefaultUserWorkspaceService userWorkspaceService;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UserI user;
    private String username;

    @Before
    public void before() throws IOException {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);

        // Set up the workspace directory
        File userWorkspaces = temporaryFolder.newFolder("workspaces", "users");
        when(mockJupyterHubPreferences.getWorkspacePath()).thenReturn(userWorkspaces.getAbsolutePath());
    }

    @Test
    public void testUserWorkspacePath() {
        // Test
        Path userWorkspace = userWorkspaceService.getUserWorkspace(user);

        // Verify user workspace was created and that it is in their workspace directory
        assertTrue(Files.exists(userWorkspace));
        assertTrue(userWorkspace.toString().endsWith(mockJupyterHubPreferences.getWorkspacePath() + "/users/" + username));
    }

}