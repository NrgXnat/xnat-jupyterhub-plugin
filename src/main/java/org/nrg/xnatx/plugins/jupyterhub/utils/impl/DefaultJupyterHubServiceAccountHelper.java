package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.utils.JupyterHubServiceAccountHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class DefaultJupyterHubServiceAccountHelper implements JupyterHubServiceAccountHelper {

    private final UserManagementServiceI userManagementService;
    private final RoleServiceI roleService;

    @Autowired
    public DefaultJupyterHubServiceAccountHelper(final UserManagementServiceI userManagementService,
                                                 final RoleServiceI roleService) {
        this.userManagementService = userManagementService;
        this.roleService = roleService;
    }

    /**
     * Checks if the JupyterHub service account is enabled. The JupyterHub service account is considered enabled if
     * any of the users with the JupyterHub role are enabled. Without this account, JupyterHub will not be able to
     * communicate with XNAT. An administrator is responsible for supplying the credentials for the service account to
     * JupyterHub as environment variables.
     *
     * @return true if a JupyterHub service account is enabled, false otherwise
     */
    @Override
    public boolean isJupyterHubServiceAccountEnabled() {
        Collection<String> jupyterHubServiceAccountRoleHolders = roleService.getUsers("JupyterHub");

        if (jupyterHubServiceAccountRoleHolders == null || jupyterHubServiceAccountRoleHolders.isEmpty()) {
            return false;
        }

        // If any of the users with the JupyterHub role are enabled, then the JupyterHub service account is
        // considered enabled. The credentials for the JupyterHub service account still need to be added to the
        // JupyterHub configuration environment variables by an administrator.
        return jupyterHubServiceAccountRoleHolders.stream().map(username -> {
            try {
                UserI user = userManagementService.getUser(username);
                return user.isEnabled();
            } catch (Exception e) {
                return false;
            }
        }).reduce(false, (a, b) -> a || b);
    }

}
