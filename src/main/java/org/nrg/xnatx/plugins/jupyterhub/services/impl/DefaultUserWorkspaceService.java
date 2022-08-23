package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.UserWorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class DefaultUserWorkspaceService implements UserWorkspaceService {

    private final JupyterHubPreferences jupyterHubPreferences;

    @Autowired
    public DefaultUserWorkspaceService(final JupyterHubPreferences jupyterHubPreferences) {
        this.jupyterHubPreferences = jupyterHubPreferences;
    }

    /**
     * A user's 'workspace' is a persistent directory to store their notebooks. Get the user's workspace directory.
     * Will create if it does not exist.
     *
     * @param user The user to create, if needed, and get the workspace directory of.
     *
     * @return Path to user's workspace directory
     */
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

}
