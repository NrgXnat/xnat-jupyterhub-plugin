package org.nrg.xnatx.plugins.jupyterhub.utils;

import org.nrg.xft.security.UserI;

public interface PermissionsHelper {
    boolean canRead(UserI user, String projectId, String entityId, String xsiType);
}
