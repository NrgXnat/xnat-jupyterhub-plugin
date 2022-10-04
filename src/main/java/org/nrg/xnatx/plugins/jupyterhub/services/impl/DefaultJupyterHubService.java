package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.ResourceAlreadyExistsException;
import org.nrg.xnatx.plugins.jupyterhub.client.exceptions.UserNotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Hub;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEvent;
import org.nrg.xnatx.plugins.jupyterhub.events.JupyterServerEventI;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DefaultJupyterHubService implements JupyterHubService {

    private final JupyterHubClient jupyterHubClient;
    private final NrgEventServiceI eventService;
    private final PermissionsHelper permissionsHelper;
    private final UserOptionsService userOptionsService;
    private final JupyterHubPreferences jupyterHubPreferences;

    @Autowired
    public DefaultJupyterHubService(final JupyterHubClient jupyterHubClient,
                                    final NrgEventServiceI eventService,
                                    final PermissionsHelper permissionsHelper,
                                    final UserOptionsService userOptionsService,
                                    final JupyterHubPreferences jupyterHubPreferences) {
        this.jupyterHubClient = jupyterHubClient;
        this.eventService = eventService;
        this.permissionsHelper = permissionsHelper;
        this.userOptionsService = userOptionsService;
        this.jupyterHubPreferences = jupyterHubPreferences;
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
     * Asynchronously start the default Jupyter notebook server for the user. The provided entity's resources will be
     * mounted to the Jupyter notebook server container. The progress of the server launch is tracked with the event
     * tracking api.
     *
     * @param user            User to start the server for.
     * @param xsiType         Accepts xnat:projectData, xnat:subjectData, xnat:experimentData and its children, and
     *                        xdat:stored_search
     * @param itemId          The provided id's resources will be mounted to the Jupyter notebook server container
     * @param itemLabel       The label of the provided item (e.g. the subject label or experiment label).
     * @param projectId       Can be null for xdat:stored_search
     * @param eventTrackingId Use with the event tracking api to track progress.
     * @param dockerImage     The docker image to use for the single-user notebook server container
     */
    @Override
    public void startServer(final UserI user, final String xsiType, final String itemId,
                            final String itemLabel, @Nullable final String projectId, final String eventTrackingId,
                            final String dockerImage) {
        startServer(user, "", xsiType, itemId, itemLabel, projectId, eventTrackingId, dockerImage);
    }

    /**
     * Asynchronously starts a named Jupyter notebook server for the user. The provided entity's resources will be
     * mounted to the Jupyter notebook server container. The progress of the server launch is tracked with the event
     * tracking api.
     *
     * @param user              User to start the server for.
     * @param servername        The name of the server that will be started
     * @param xsiType           Accepts xnat:projectData, xnat:subjectData, xnat:experimentData and its children, and
     *                          xdat:stored_search
     * @param itemId            The provided id's resources will be mounted to the Jupyter notebook server container
     * @param itemLabel         The label of the provided item (e.g. the subject label or experiment label).
     * @param projectId         Can be null for xdat:stored_search
     * @param eventTrackingId   Use with the event tracking api to track progress.
     * @param dockerImage       The docker image to use for the single-user notebook server container
     */
    @Override
    public void startServer(final UserI user, String servername, final String xsiType, final String itemId,
                            final String itemLabel, @Nullable  final String projectId, final String eventTrackingId,
                            final String dockerImage) {
        eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                              JupyterServerEventI.Operation.Start, 0,
                                                              "Starting Jupyter notebook server for user " + user.getUsername()));

        if (!permissionsHelper.canRead(user, projectId, itemId, xsiType)) {
            eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                JupyterServerEventI.Operation.Start,
                                                                "Failed to launch Jupyter notebook server. Permission denied."));
            return;
        }

        CompletableFuture.runAsync(() -> {
            // Check if JupyterHub is online
            try {
                jupyterHubClient.getVersion();
            } catch (Exception e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Unable to connect to JupyterHub"));
                return;
            }

            try {
                // We don't want to update the user options entity if there is a running server
                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 0,
                                                                      "Checking for existing Jupyter notebook servers."));

                if (jupyterHubClient.getServer(user.getUsername(), servername).isPresent()) {
                    eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId,
                                                                        user.getID(), xsiType, itemId,
                                                                        JupyterServerEventI.Operation.Start,
                                                                        "Failed to launch Jupyter notebook server. " +
                                                                                "There is already one running."));
                    return;
                }

                eventService.triggerEvent(JupyterServerEvent.progress(eventTrackingId, user.getID(), xsiType, itemId,
                                                                      JupyterServerEventI.Operation.Start, 20,
                                                                      "Building notebook server container configuration."));

                userOptionsService.storeUserOptions(user, servername, xsiType, itemId, projectId, dockerImage);

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
                                                                    "Failed to launch Jupyter notebook server. Timeout reached."));
            } catch (UserNotFoundException e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to launch Jupyter notebook server. User not found."));
            } catch (ResourceAlreadyExistsException e) {
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start,
                                                                    "Failed to launch Jupyter notebook server. Resource already exists."));
            } catch (InterruptedException e) {
                String msg = "Failed to launch Jupyter notebook server. Thread interrupted..";
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start, msg));
                log.error(msg, e);
            } catch (Exception e) {
                String msg = "Failed to launch Jupyter notebook server. See system logs for detailed error.";
                eventService.triggerEvent(JupyterServerEvent.failed(eventTrackingId, user.getID(), xsiType, itemId,
                                                                    JupyterServerEventI.Operation.Start, msg));
                log.error(msg, e);
            }

        });
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

    private Integer inMilliSec(Integer seconds) {
        return seconds * 1000;
    }

}
