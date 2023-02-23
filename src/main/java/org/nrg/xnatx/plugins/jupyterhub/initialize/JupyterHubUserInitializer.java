package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.rest.users.UsersApi;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initializing task which creates a user account for JupyterHub allowing it to communicate with XNAT
 */
@Component
@Slf4j
public class JupyterHubUserInitializer extends AbstractInitializingTask {

    private final UserManagementServiceI userManagementService;
    private final RoleHolder roleHolder;
    private final XFTManagerHelper xftManagerHelper;

    @Autowired
    public JupyterHubUserInitializer(final UserManagementServiceI userManagementService,
                                     final RoleHolder roleHolder,
                                     final XFTManagerHelper xftManagerHelper) {
        this.userManagementService = userManagementService;
        this.roleHolder = roleHolder;
        this.xftManagerHelper = xftManagerHelper;
    }

    @Override
    public String getTaskName() {
        return "JupyterHubUserInitialization";
    }

    /**
     * Creates a user account for JupyterHub allowing it to communicate with XNAT
     *
     * @throws InitializingTaskException if the jupyterhub user account or role cannot be created.
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing JupyterHub user.");

        if (!xftManagerHelper.isInitialized()) {
            log.debug("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (userManagementService.exists("jupyterhub")) {
            log.debug("JupyterHub user already exists.");
            return;
        }

        final UserI jupyterhubUser = userManagementService.createUser();
        jupyterhubUser.setLogin("jupyterhub");
        jupyterhubUser.setEmail("jupyterhub@jupyterhub.jupyterhub");
        jupyterhubUser.setFirstname("jupyterhub");
        jupyterhubUser.setLastname("jupyterhub");
        jupyterhubUser.setPassword("jupyterhub");
        jupyterhubUser.setEnabled(false);
        jupyterhubUser.setVerified(true);

        try {
            userManagementService.save(jupyterhubUser, Users.getAdminUser(),
                                       false, new EventDetails(EventUtils.CATEGORY.DATA,
                                                               EventUtils.TYPE.PROCESS,
                                                               UsersApi.Event.Added,
                                                               "Requested by the JupyterHub Plugin",
                                                               "Created new user " + jupyterhubUser.getUsername()));
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error,
                                                "Error occurred creating user " + jupyterhubUser.getLogin(),
                                                e);
        }

        try {
            boolean added = roleHolder.addRole(Users.getAdminUser(), jupyterhubUser, "JupyterHub");

            if (!added) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error,
                                                    "Error occurred adding the JupyterHub role to the jupyterhub user account.");
            }
        } catch (Exception e) {
            // The user can be deleted, but it is disabled.
            throw new InitializingTaskException(InitializingTaskException.Level.Error,
                                                "Error occurred adding the JupyterHub role to the jupyterhub user account.",
                                                e);
        }

        log.info("Created jupyterhub user.");
    }
}
