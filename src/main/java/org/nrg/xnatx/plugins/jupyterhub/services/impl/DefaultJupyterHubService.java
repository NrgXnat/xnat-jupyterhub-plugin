package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xnat.compute.services.JobTemplateService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Hub;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Token;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEvent;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEventI;
import org.nrg.xnatx.plugins.jupyterhub.models.ServerStartRequest;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.utils.JupyterHubServiceAccountHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("BusyWait")
@Service
@Slf4j
public class DefaultJupyterHubService implements JupyterHubService {

    private final JupyterHubClient jupyterHubClient;
    private final NrgEventServiceI eventService;
    private final PermissionsHelper permissionsHelper;
    private final UserOptionsService userOptionsService;
    private final JupyterHubPreferences jupyterHubPreferences;
    private final UserManagementServiceI userManagementService;
    private final JobTemplateService jobTemplateService;
    private final JupyterHubServiceAccountHelper jupyterHubServiceAccountHelper;

    @Autowired
    public DefaultJupyterHubService(final JupyterHubClient jupyterHubClient,
                                    final NrgEventServiceI eventService,
                                    final PermissionsHelper permissionsHelper,
                                    final UserOptionsService userOptionsService,
                                    final JupyterHubPreferences jupyterHubPreferences,
                                    final UserManagementServiceI userManagementService,
                                    final JobTemplateService jobTemplateService,
                                    final JupyterHubServiceAccountHelper jupyterHubServiceAccountHelper) {
        this.jupyterHubClient = jupyterHubClient;
        this.eventService = eventService;
        this.permissionsHelper = permissionsHelper;
        this.userOptionsService = userOptionsService;
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.userManagementService = userManagementService;
        this.jobTemplateService = jobTemplateService;
        this.jupyterHubServiceAccountHelper = jupyterHubServiceAccountHelper;
    }

    /**
     * Get JupyterHub version
     * @return Hub with version field populated
     */
    @Override
    public Hub getVersion() {
        return jupyterHubClient.getVersion();
    }

    /**
     * Get full JupyterHub information
     * @return Hub with all fields populated
     */
    @Override
    public Hub getInfo() {
        return jupyterHubClient.getInfo();
    }

    /**
     * Creates the JupyterHub user account for the provided XNAT user.
     *
     * @param user The XNAT user
     *
     * @return The created JupyterHub user
     */
    @Override
    public User createUser(final UserI user) {
        return jupyterHubClient.createUser(user.getUsername());
    }

    /**
     * Gets the JupyterHub user for the provided XNAT user. Optional is empty if user does not exist.
     *
     * @param user The XNAT user
     *
     * @return The JupyterHub user if it exists.
     */
    @Override
    public Optional<User> getUser(final UserI user) {
        return jupyterHubClient.getUser(user.getUsername());
    }

    /**
     * Gets all the users and their active servers from JupyterHub.
     * @return List of all users on JupyterHub
     */
    @Override
    public List<User> getUsers() {
        return jupyterHubClient.getUsers();
    }

    /**
     * Gets the default unnamed Jupyter notebook server for the provided XNAT user. Optional is empty if the user or
     * server does not exist.
     * @param user User to get the server for.
     * @return The JupyterHub server if running, or an empty optional if not running.
     */
    @Override
    public Optional<Server> getServer(final UserI user) {
        return jupyterHubClient.getServer(user.getUsername());
    }

    /**
     * Gets a named Jupyter notebook server for the provided XNAT user. Optional is empty if the user or server does not
     * exist.
     *
     * @param user User to get the server for.
     *
     * @return The JupyterHub server if running, or an empty optional if not running.
     */
    @Override
    public Optional<Server> getServer(final UserI user, final String servername) {
        return jupyterHubClient.getServer(user.getUsername(), servername);
    }

    /**
     * Asynchronously starts a Jupyter notebook server based on the provided request. Use the event tracking api to keep
     * track of progress.
     * @param user          The user requesting the server.
     * @param startRequest  The request to start a Jupyter notebook server.
     */
    @Override
    public void startServer(final UserI user, final ServerStartRequest startRequest) {
        validateServerStartRequest(user, startRequest);

        final String servername = startRequest.getServername();
        final String xsiType = startRequest.getXsiType();
        final String itemId = startRequest.getItemId();
        final String itemLabel = startRequest.getItemLabel();
        final String projectId = startRequest.getProjectId();
        final String eventTrackingId = startRequest.getEventTrackingId();
        final Long computeEnvironmentConfigId = startRequest.getComputeEnvironmentConfigId();
        final Long hardwareConfigId = startRequest.getHardwareConfigId();

        eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                              JupyterServerEventI.Operation.Start, 0,
                                                              "Starting Jupyter notebook server for user " + user.getUsername()));

        if (!permissionsHelper.canRead(user, projectId, itemId, xsiType)) {
            eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                JupyterServerEventI.Operation.Start,
                                                                "Failed to launch Jupyter notebook server. Permission denied to read " + xsiType + " " + itemId + " in project " + projectId));
            return;
        }

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Project, projectId);
        executionScope.put(Scope.User, user.getUsername());
        if (!jobTemplateService.isAvailable(computeEnvironmentConfigId, hardwareConfigId, executionScope)) {
            eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                JupyterServerEventI.Operation.Start,
                                                                "Failed to launch Jupyter notebook server. The compute environment or hardware configuration is not available to the user."));
            return;
        }

        CompletableFuture.runAsync(() -> {
            // getVersion() does not require authentication, check if JupyterHub is online
            try {
                jupyterHubClient.getVersion();
            } catch (Exception e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to connect to JupyterHub. Please ensure the following:\n" +
                                                                    "(1) JupyterHub is running \n" +
                                                                    "(2) Verify the correct API URL is set in the plugin settings."
                ));
                return;
            }

            // getInfo() does require authentication, check if XNAT can connect and authenticate with JupyterHub
            try {
                jupyterHubClient.getInfo();
            } catch (Exception e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                        JupyterServerEventI.Operation.Start,
                            "Failed to connect to JupyterHub. Please check the following: \n" +
                                    "(1) Ensure that JupyterHub is running. \n" +
                                    "(2) Verify the correct API URL is set in the plugin settings. \n" +
                                    "(3) Confirm XNATs token for authenticating with JupyterHub is correctly set in " +
                                    "both the plugin settings and JupyterHub configuration."
                ));
                return;
            }

            try {
                // We don't want to update the user options entity if there is a running server
                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 0,
                                                                      "Checking for existing Jupyter notebook servers."));

                boolean hasRunningServer = jupyterHubClient.getUser(user.getUsername())
                                                           .orElseGet(() -> createUser(user))
                                                           .getServers()
                                                           .containsKey(servername);
                if (hasRunningServer) {
                    eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId,
                                                                        user.getID(), xsiType, itemId,
                                                                        JupyterServerEventI.Operation.Start,
                                                                        "Failed to launch Jupyter notebook server. " +
                                                                                "There is already one running. " +
                                                                                "Please stop the running server before starting a new one."));
                    return;
                }

                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 20,
                                                                      "Building notebook server container configuration."));

                userOptionsService.storeUserOptions(user, servername, xsiType, itemId, projectId, computeEnvironmentConfigId, hardwareConfigId, eventTrackingId);

                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 30,
                                                                      "Saved container configuration. Sending start request to JupyterHub."));

                // Send empty user options. User's should not be able to directly send bind mounts.
                // JupyterHub will request the user options.
                jupyterHubClient.startServer(user.getUsername(), servername,
                                             XnatUserOptions.builder()
                                                     .userId(user.getID())
                                                     .xsiType(xsiType)
                                                     .itemId(itemId)
                                                     .itemLabel(itemLabel)
                                                     .projectId(projectId)
                                                     .eventTrackingId(eventTrackingId)
                                                     .build());

                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 40,
                                                                      "JupyterHub is spawning notebook server container."));

                // TODO consume progress api
                int time = 0;
                while (time < inMilliSec(jupyterHubPreferences.getStartTimeout())) {
                    // Give JupyterHub a chance to spawn the server before polling
                    Thread.sleep(inMilliSec(jupyterHubPreferences.getStartPollingInterval()));
                    Optional<Server> server = jupyterHubClient.getServer(user.getUsername(), servername);

                    if (server.isPresent()) {
                        if (server.get().getReady()) {
                            log.info("Jupyter server started for user {}", user.getUsername());
                            eventService.triggerEvent(JupyterServerEvent.completed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                                   JupyterServerEventI.Operation.Start,
                                                                                   "Jupyter notebook server is available at: " + server.get().getUrl()));
                            return;
                        }
                    } else {
                        eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                              JupyterServerEventI.Operation.Start, 45,
                                                                              "Waiting for JupyterHub to spawn server."));
                    }

                    time += inMilliSec(jupyterHubPreferences.getStartPollingInterval());
                }

                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to launch Jupyter notebook server. " +
                                                                            "Timeout exceeded while waiting for JupyterHub to spawn server. " +
                                                                            "Check the XNAT and JupyterHub system logs for error messages."));
            } catch (UserNotFoundException e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to launch Jupyter notebook server. User not found."));
            } catch (ResourceAlreadyExistsException e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to launch Jupyter notebook server. A server with the same name is already running."));
            } catch (InterruptedException e) {
                String msg = "Failed to launch Jupyter notebook server. Thread interrupted. Check the XNAT and JupyterHub system logs for error messages.";
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start, msg));
                log.error(msg, e);
            } catch (Exception e) {
                String msg = "Failed to launch Jupyter notebook server. ";

                if (!jupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled()) {
                    msg += "Make sure the JupyterHub service account user is enabled and provide the credentials to JupyterHub (refer to the documentation for more details). ";
                }

                msg += "Check the XNAT and JupyterHub system logs for error messages.";

                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start, msg));
                log.error(msg, e);
            }

        });
    }

    /**
     * Validates the provided request. Throws an exception if the request is invalid.
     * @param user The user making the request.
     * @param request The request to validate.
     */
    public void validateServerStartRequest(UserI user, ServerStartRequest request) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (request == null) {
            throw new IllegalArgumentException("ServerStartRequest cannot be null");
        }

        List<String> errorMessages = new ArrayList<>();

        if (!StringUtils.equals(user.getUsername(), request.getUsername())) {
            errorMessages.add("Usernames do not match");
        }

        if (StringUtils.isBlank(request.getUsername())) {
            errorMessages.add("Username cannot be blank");
        }

        if (StringUtils.isBlank(request.getXsiType())) {
            errorMessages.add("XSI type cannot be blank");
        }

        if (StringUtils.isBlank(request.getItemId())) {
            errorMessages.add("Item ID cannot be blank");
        }

        if (StringUtils.isBlank(request.getItemLabel())) {
            errorMessages.add("Item label cannot be blank");
        }

        if (StringUtils.isBlank(request.getProjectId())) {
            errorMessages.add("Project ID cannot be blank");
        }

        if (StringUtils.isBlank(request.getEventTrackingId())) {
            errorMessages.add("Event tracking ID cannot be blank");
        }

        if (request.getComputeEnvironmentConfigId() == null) {
            errorMessages.add("Compute environment config ID cannot be null");
        }

        if (request.getHardwareConfigId() == null) {
            errorMessages.add("hardware config id cannot be null");
        }

        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errorMessages));
        }
    }


    /**
     * Asynchronously stops the default unnamed Jupyter notebook server for the provided user. Use the event tracking
     * api to keep track of progress.
     *
     * @param user              The user of the default server to stop.
     * @param eventTrackingId   Use this with the Event Tracking Data Api to keep track of progress.
     */
    @Override
    public void stopServer(final UserI user, String eventTrackingId) {
        stopServer(user, "", eventTrackingId);
    }

    /**
     * Asynchronously stops a named Jupyter notebook server for the provided user. Use the event tracking api to keep
     * track of progress.
     *
     * @param user              The user of the named server to stop.
     * @param servername        Name of the server to stop
     * @param eventTrackingId   Use this with the Event Tracking Data Api to keep track of progress.
     */
    @Override
    public void stopServer(final UserI user, final String servername, String eventTrackingId) {
        eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(),
                                                              JupyterServerEventI.Operation.Stop, 0,
                                                              "Stopping Jupyter Notebook Server."));

        CompletableFuture.runAsync(() -> {
            try {
                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(),
                                                                      JupyterServerEventI.Operation.Stop, 50,
                                                                      "Sending stop request to JupyterHub."));

                jupyterHubClient.stopServer(user.getUsername(), servername);

                int time = 0;
                while (time < inMilliSec(jupyterHubPreferences.getStopTimeout())) {
                    // Give JupyterHub a chance to shut down the server before polling
                    Thread.sleep(inMilliSec(jupyterHubPreferences.getStopPollingInterval()));

                    Optional<Server> server = jupyterHubClient.getServer(user.getUsername(), servername);

                    if (!server.isPresent()) {
                        log.info("Jupyter server stopped for user {}", user.getUsername());
                        eventService.triggerEvent(JupyterServerEvent.completed(eventTrackingId, user.getID(),
                                                                               JupyterServerEventI.Operation.Stop,
                                                                               "Jupyter Server Stopped."));
                        return;
                    }

                    time += inMilliSec(jupyterHubPreferences.getStopPollingInterval());
                }

                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(),
                                                                    JupyterServerEventI.Operation.Stop,
                                                                    "Failed to stop Jupyter Server."));
            } catch (RuntimeException | InterruptedException e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(),
                                                                    JupyterServerEventI.Operation.Stop,
                                                                    "Failed to stop Jupyter Server."));
                log.error("Failed to stop jupyter server", e);
            }
        });
    }

    /**
     * Create a new token for the provided user. Token is given access:severs!user=username scope on JupyterHub.
     *
     * @param user User to create token for
     * @param note The note to identify the new token with. This note will help you keep track of what your tokens
     *             are for.
     * @param expiresIn Lifetime of the token in seconds
     * @return Token with the token field populated. This is the only chance to save the token!!!
     *
     */
    @Override
    public Token createToken(UserI user, String note, Integer expiresIn) {
        Token token = Token.builder()
                .note(note)
                .expires_in(expiresIn)
                .scopes(Collections.singletonList("access:servers!user=" + user.getUsername()))
                .build();

        return jupyterHubClient.createToken(user.getUsername(), token);
    }

    /**
     * Stop servers that have been inactive for some period of time
     */
    @Override
    public void cullInactiveServers() {
        try {
            if (jupyterHubPreferences.getInactivityTimeout() > 0) {
                log.debug("Culling idle Jupyter notebook servers");

                final List<User> users = getUsers();
                final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

                users.forEach(user -> {
                    Map<String, Server> servers = user.getServers();
                    servers.forEach((servername, server) -> {
                        final ZonedDateTime lastActivity = server.getLast_activity();
                        long inactiveTime = ChronoUnit.MINUTES.between(lastActivity, now);

                        if (inactiveTime > jupyterHubPreferences.getInactivityTimeout()) {
                            try {
                                UserI userI = userManagementService.getUser(user.getName());
                                log.info("Removing Jupyter server {} for user {} due to inactivity.", servername, user.getName());
                                stopServer(userI, servername, now + "_cullIdleServers");
                            } catch (UserInitException | org.nrg.xdat.security.user.exceptions.UserNotFoundException e) {
                                log.error("Unable to delete long running Jupyter server for user " + user.getName(), e);
                            }
                        }
                    });
                });
            } else {
                log.debug("Not culling idle Jupyter notebook servers");
            }
        } catch (Exception e) {
            log.error("Failed to cull idle Jupyter notebook servers");
        }
    }

    /**
     * Stop servers that have been running for some period of time
     */
    @Override
    public void cullLongRunningServers() {
        try {
            if (jupyterHubPreferences.getMaxServerLifetime() > 0) {
                log.debug("Culling long running Jupyter notebook servers");

                final List<User> users = getUsers();
                final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

                users.forEach(user -> {
                    Map<String, Server> servers = user.getServers();
                    servers.forEach((servername, server) -> {
                        final ZonedDateTime started = server.getStarted();
                        long runningTime = ChronoUnit.HOURS.between(started, now);

                        if (runningTime >= jupyterHubPreferences.getMaxServerLifetime()) {
                            try {
                                UserI userI = userManagementService.getUser(user.getName());
                                log.info("Removing Jupyter server {} for user {} due to long running time.", servername, user.getName());
                                stopServer(userI, servername, now + "_cullLongRunningServers");
                            } catch (UserInitException | org.nrg.xdat.security.user.exceptions.UserNotFoundException e) {
                                log.error("Unable to delete long running Jupyter server for user " + user.getName(), e);
                            }
                        }
                    });
                });
            } else {
                log.debug("Not culling long running Jupyter notebook servers");
            }
        } catch (Exception e) {
            log.error("Failed to cull long running Jupyter notebook servers");
        }
    }

    private Integer inMilliSec(Integer seconds) {
        return seconds * 1000;
    }

}
