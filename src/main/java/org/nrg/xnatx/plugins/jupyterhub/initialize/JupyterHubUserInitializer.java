package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xapi.rest.users.UsersApi;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.utils.SystemHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Initializing task which creates a user account for JupyterHub allowing it to communicate with XNAT
 */
@Component
@Slf4j
public class JupyterHubUserInitializer extends AbstractInitializingTask {

    private final UserManagementServiceI userManagementService;
    private final RoleHolder roleHolder;
    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final SystemHelper systemHelper;
    private final DatabaseHelper databaseHelper;

    @Autowired
    public JupyterHubUserInitializer(final UserManagementServiceI userManagementService,
                                     final RoleHolder roleHolder,
                                     final XFTManagerHelper xftManagerHelper,
                                     final XnatAppInfo appInfo,
                                     final SystemHelper systemHelper,
                                     final DatabaseHelper databaseHelper) {

        this.userManagementService = userManagementService;
        this.roleHolder = roleHolder;
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.systemHelper = systemHelper;
        this.databaseHelper = databaseHelper;
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
        log.info("Creating `jupyterhub` user account.");

        try {
            if (!xftManagerHelper.isInitialized() || !databaseHelper.tableExists("xdat_user")) {
                log.info("XFT and database not initialized, deferring execution.");
                throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(
                    InitializingTaskException.Level.Error,
                    "An error occurred trying to access the database to check for the table 'xdat_user'.",
                    e);
        }

        if (!appInfo.isInitialized()) {
            log.info("XNAT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        final String username = systemHelper.getOrDefault("JH_XNAT_USERNAME", "jupyterhub");
        final String password = systemHelper.getOrDefault("JH_XNAT_PASSWORD", "jupyterhub");
        final boolean enabled = systemHelper.getOrDefault("JH_XNAT_SERVICE_USER_ENABLED", "false").equals("true");

        if (userManagementService.exists(username)) {
            log.info("`jupyterhub` user account {} already exists, skipping creation.", username);
            return;
        }

        final UserI jupyterhubUser = userManagementService.createUser();
        jupyterhubUser.setLogin(username);
        jupyterhubUser.setEmail("jupyterhub@jupyterhub.jupyterhub");
        jupyterhubUser.setFirstname("jupyterhub");
        jupyterhubUser.setLastname("jupyterhub");
        jupyterhubUser.setPassword(password);
        jupyterhubUser.setEnabled(enabled);
        jupyterhubUser.setVerified(true);

        String errorMessage = "Unable to create the `jupyterhub` user account. " +
                "This account is used by JupyterHub to communicate with XNAT. " +
                "You will need to create this account manually in the UI. " +
                "The default username is 'jupyterhub', the password is 'jupyterhub' (but you should change this), " +
                "and the account must have the JupyterHub service account role. " +
                "This accounts credentials must be added to the JupyterHub configuration/environment variables as well. " +
                "Please see the documentation for more information.";

        try {
            userManagementService.save(jupyterhubUser, Users.getAdminUser(),
                                       false, new EventDetails(EventUtils.CATEGORY.DATA,
                                                               EventUtils.TYPE.PROCESS,
                                                               UsersApi.Event.Added,
                                                               "Requested by the JupyterHub Plugin",
                                                               "Created new user " + jupyterhubUser.getUsername()));
        } catch (Exception e) {
            log.error(errorMessage, e);
            throw new InitializingTaskException(InitializingTaskException.Level.Error, errorMessage, e);
        }

        try {
            boolean added = roleHolder.addRole(Users.getAdminUser(), jupyterhubUser, "JupyterHub");

            if (!added) {
                log.error(errorMessage);
                throw new InitializingTaskException(InitializingTaskException.Level.Error, errorMessage);
            }
        } catch (Exception e) {
            log.error(errorMessage, e);
            throw new InitializingTaskException(InitializingTaskException.Level.Error, errorMessage, e);
        }

        log.info("Successfully created `jupyterhub` user account: username={}, password=HIDDEN, enabled={}",
                 username, enabled);
    }

    @Override
    public boolean isMaxedOut() {
        // The execution count is incremented regardless of whether XNAT has been initialized or not, padding the count
        // to account for this. 12 * 15 seconds = 3 minutes seems reasonable.
        int maxExecutions = 12;
        return super.executions() >= maxExecutions;
    }

}
