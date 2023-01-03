package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.tracking.entities.EventTrackingData;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnatx.plugins.jupyterhub.authorization.JupyterUserAuthorization;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Hub;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Token;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.Authorizer;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@SuppressWarnings("DefaultAnnotationParam")
@Api("JupyterHub Plugin API")
@XapiRestController
@RequestMapping("/jupyterhub")
@Slf4j
public class JupyterHubApi extends AbstractXapiRestController {

    private final JupyterHubService jupyterHubService;
    private final UserOptionsService jupyterHubUserOptionsService;
    private final EventTrackingDataHibernateService eventTrackingDataHibernateService;

    @Autowired
    public JupyterHubApi(final UserManagementServiceI userManagementService,
                         final RoleHolder roleHolder,
                         final JupyterHubService jupyterHubService,
                         final UserOptionsService jupyterHubUserOptionsService, EventTrackingDataHibernateService eventTrackingDataHibernateService) {
        super(userManagementService, roleHolder);
        this.jupyterHubService = jupyterHubService;
        this.jupyterHubUserOptionsService = jupyterHubUserOptionsService;
        this.eventTrackingDataHibernateService = eventTrackingDataHibernateService;
    }

    @ApiOperation(value = "Get the JupyterHub version.", response = Hub.class)
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub version successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/version", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Hub getVersion() {
        return jupyterHubService.getVersion();
    }

    @ApiOperation(value = "Get detailed information about JupyterHub",
                  notes = "Detailed JupyterHub information, including Python version, JupyterHub's version and " +
                          "executable path, and which Authenticator and Spawner are active.",
                  response = Hub.class)
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub information successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/info", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Hub getInfo() {
        return jupyterHubService.getInfo();
    }

    @ApiOperation(value = "Get a JupyterHub user by name.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.User)
    public User getUser(@ApiParam(value = "username", required = true) @PathVariable @Username final String username) throws NotFoundException, UserNotFoundException, UserInitException {
        return jupyterHubService.getUser(getUserI(username)).orElseThrow(() -> new NotFoundException("No user with name " + username + "exists on JupyterHub."));
    }

    @ApiOperation(value = "Get all the users on JupyterHub.", notes = "All users and their active servers will be returned.", response = User.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "Users found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.Admin)
    public List<User> getUsers() {
        return jupyterHubService.getUsers();
    }

    @ApiOperation(value = "Create a single user on JupyterHub")
    @ApiResponses({@ApiResponse(code = 200, message = "The user has been created"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}", method = POST, restrictTo = AccessLevel.User)
    public void createUser(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException {
        jupyterHubService.createUser(getUserI(username));
    }

    @ApiOperation(value = "Get Jupyter Server details for a user.")
    @ApiResponses({@ApiResponse(code = 200, message = "Server found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.User)
    public Server getServer(@ApiParam(value = "username", required = true) @PathVariable @Username final String username) throws UserNotFoundException, UserInitException {
        return jupyterHubService.getServer(getUserI(username)).orElse(null);
    }

    @ApiOperation(value = "Get Jupyter Server details for a user.", hidden = true)
    @ApiResponses({@ApiResponse(code = 200, message = "Server found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{servername}", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.User)
    public Server getNamedServer(@ApiParam(value = "username", required = true) @PathVariable @Username final String username,
                                 @ApiParam(value = "servername", required = true) @PathVariable final String servername) throws UserNotFoundException, UserInitException {
        return jupyterHubService.getServer(getUserI(username), servername).orElse(null);
    }

    @ApiOperation(value = "Starts a Jupyter server for the user",
                  notes = "Use the Event Tracking API to track progress.")
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully started"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server", method = POST, restrictTo = Authorizer)
    @AuthDelegate(JupyterUserAuthorization.class)
    public void startServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                            @ApiParam(value = "xsiType", required = true) @RequestParam("xsiType") final String xsiType,
                            @ApiParam(value = "itemId", required = true) @RequestParam("itemId") final String itemId,
                            @ApiParam(value = "itemLabel", required = true) @RequestParam("itemLabel") final String itemLabel,
                            @ApiParam(value = "projectId", required = false) @RequestParam(value = "projectId", required = false) final String projectId,
                            @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId,
                            @ApiParam(value = "dockerImage", required = true) @RequestParam(value = "dockerImage") final String dockerImage) throws UserNotFoundException, UserInitException {
        jupyterHubService.startServer(getUserI(username), xsiType, itemId, itemLabel, projectId, eventTrackingId, dockerImage);
    }

    @ApiOperation(value = "Starts a Jupyter server for the user",
                  notes = "Use the Event Tracking API to track progress.",
                  hidden = true)
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully started"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{servername}", method = POST, restrictTo = Authorizer)
    @AuthDelegate(JupyterUserAuthorization.class)
    public void startNamedServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                                 @ApiParam(value = "servername", required = true) @PathVariable("servername") final String servername,
                                 @ApiParam(value = "xsiType", required = true) @RequestParam("xsiType") final String xsiType,
                                 @ApiParam(value = "itemId", required = true) @RequestParam("itemId") final String itemId,
                                 @ApiParam(value = "itemLabel", required = true) @RequestParam("itemLabel") final String itemLabel,
                                 @ApiParam(value = "projectId", required = false) @RequestParam(value = "projectId", required = false) final String projectId,
                                 @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId,
                                 @ApiParam(value = "dockerImage", required = true) @RequestParam(value = "dockerImage") final String dockerImage) throws UserNotFoundException, UserInitException {
        jupyterHubService.startServer(getUserI(username), servername, xsiType, itemId, itemLabel, projectId, eventTrackingId, dockerImage);
    }

    @ApiOperation(value = "Returns the last known user options for the default server", response = XnatUserOptions.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved user options."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"})
    @XapiRequestMapping(value = "/users/{username}/server/user-options", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public XnatUserOptions getUserOptions(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException, NotFoundException {
        UserI user = getUserManagementService().getUser(username);
        return jupyterHubUserOptionsService.retrieveUserOptions(user).orElseThrow(() -> new NotFoundException("Jupyter server configuration not found."));
    }

    @ApiOperation(value = "Returns the last known user options for the named server",
                  response = XnatUserOptions.class,
                  hidden = true)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved user options."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"})
    @XapiRequestMapping(value = "/users/{username}/server/{servername}/user-options", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public XnatUserOptions getUserOptions(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                                          @ApiParam(value = "servername", required = true) @PathVariable("servername") @Username final String servername) throws UserNotFoundException, UserInitException, NotFoundException {
        UserI user = getUserManagementService().getUser(username);
        return jupyterHubUserOptionsService.retrieveUserOptions(user, servername).orElseThrow(() -> new NotFoundException("User options not found."));
    }

    @ApiOperation(value = "Event tracking for the Jupyter notebook server", hidden = true)
    @XapiRequestMapping(value = "/users/{username}/server/events", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Admin)
    public Map<String, String> getEvents(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException, NotFoundException, org.nrg.framework.exceptions.NotFoundException {
        UserI user = getUserManagementService().getUser(username);

        final String eventTrackingId = jupyterHubUserOptionsService.retrieveUserOptions(user, "").orElseThrow(() -> new NotFoundException("Cannot find event tracking ID")).getEventTrackingId();
        EventTrackingData eventTrackingData = eventTrackingDataHibernateService.findByKey(eventTrackingId, user.getID());

        Map<String, String> response = new HashMap<>();
        response.put("lastUpdated", eventTrackingData.getTimestamp().toString());
        response.put("eventTrackingId", eventTrackingId);
        response.put("payload", eventTrackingData.getPayload());
        response.put("succeeded", String.valueOf(eventTrackingData.getSucceeded()));
        response.put("finalMessage", eventTrackingData.getFinalMessage());

        return response;
    }

    @ApiOperation(value = "Stops a users Jupyter server", notes = "Use the Event Tracking API to track progress.")
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully stopped."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server", method = DELETE, restrictTo = AccessLevel.User)
    public void stopServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                           @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId) throws UserNotFoundException, UserInitException {
        UserI user = getUserManagementService().getUser(username);
        jupyterHubService.stopServer(user, eventTrackingId);
    }

    @ApiOperation(value = "Stops a users named Jupyter server",
                  notes = "Use the Event Tracking API to track progress.",
                  hidden = true)
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully stopped."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{serverName}", method = DELETE, restrictTo = AccessLevel.User)
    public void stopServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                           @ApiParam(value = "serverName", required = true) @PathVariable("serverName") final String serverName,
                           @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId) throws UserNotFoundException, UserInitException {
        UserI user = getUserManagementService().getUser(username);
        jupyterHubService.stopServer(user, serverName, eventTrackingId);
    }

    @ApiOperation(value = "Create new API Token", notes = "Creates new access scoped API token for use with JupyterHub")
    @ApiResponses({@ApiResponse(code = 200, message = "Token created."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/tokens", method = POST, restrictTo = AccessLevel.User)
    public Token createToken(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                             @ApiParam(value = "note", required = true) @RequestParam("note") final String note,
                             @ApiParam(value = "expiresIn", required = true) @RequestParam(value = "expiresIn") final Integer expiresIn) throws UserNotFoundException, UserInitException {
        UserI user = getUserManagementService().getUser(username);
        return jupyterHubService.createToken(user, note, expiresIn);
    }

    private UserI getUserI(final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username);
    }
}
