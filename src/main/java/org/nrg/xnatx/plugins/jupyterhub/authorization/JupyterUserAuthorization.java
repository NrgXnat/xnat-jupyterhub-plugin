package org.nrg.xnatx.plugins.jupyterhub.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xapi.authorization.UserXapiAuthorization;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class JupyterUserAuthorization extends UserXapiAuthorization {

    private static final String JUPYTER_ROLE = "Jupyter";

    private final RoleHolder roleHolder;
    private final JupyterHubPreferences jupyterHubPreferences;

    public JupyterUserAuthorization(final RoleHolder roleHolder,
                                    final JupyterHubPreferences jupyterHubPreferences) {
        this.roleHolder = roleHolder;
        this.jupyterHubPreferences = jupyterHubPreferences;
    }

    @Override
    protected boolean checkImpl(AccessLevel accessLevel, JoinPoint joinPoint, UserI user, HttpServletRequest request) {
        boolean userAuth = super.checkImpl(accessLevel, joinPoint, user, request);
        boolean jupyterAuth = checkJupyter(user);

        return userAuth && jupyterAuth;
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

    protected boolean checkJupyter(UserI user) {
        return jupyterHubPreferences.getAllUsersCanStartJupyter() || roleHolder.checkRole(user, JUPYTER_ROLE);
    }

}
