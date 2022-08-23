package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xft.security.UserI;

import java.nio.file.Path;

public interface UserWorkspaceService {
    Path getUserWorkspace(UserI user);
}
