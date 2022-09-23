package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("JupyterHub Plugin API")
@XapiRestController
@RequestMapping("/jupyterhub")
@Slf4j
public class JupyterHubApi extends AbstractXapiRestController {

    private final JupyterHubService jupyterHubService;
    private final UserOptionsService jupyterHubUserOptionsService;

    @Autowired
    public JupyterHubApi(final UserManagementServiceI userManagementService,
                         final RoleHolder roleHolder,
                         final JupyterHubService jupyterHubService,
                         final UserOptionsService jupyterHubUserOptionsService) {
        super(userManagementService, roleHolder);
        this.jupyterHubService = jupyterHubService;
        this.jupyterHubUserOptionsService = jupyterHubUserOptionsService;
    }

    @ApiOperation(value = "Get a Jupyter Hub user by name.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}", method = GET, produces = APPLICATION_JSON_VALUE, restrictTo = AccessLevel.User)
    public User getUser(@ApiParam(value = "username", required = true) @PathVariable @Username final String username) throws NotFoundException {
        return jupyterHubService.getUser(getSessionUser()).orElseThrow(() -> new NotFoundException("No user with name " + username + "exists on JupyterHub."));
    }

    @ApiOperation(value = "Create a single user on Jupyter Hub")
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

    @ApiOperation(value = "Get Jupyter Server details for a user.")
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
    @XapiRequestMapping(value = "/users/{username}/server", method = POST, restrictTo = AccessLevel.User)
    public void startServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                            @ApiParam(value = "xsiType", required = true) @RequestParam("xsiType") final String xsiType,
                            @ApiParam(value = "itemId", required = true) @RequestParam("itemId") final String itemId,
                            @ApiParam(value = "itemLabel", required = true) @RequestParam("itemLabel") final String itemLabel,
                            @ApiParam(value = "projectId", required = true) @RequestParam("projectId") final String projectId,
                            @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId,
                            @ApiParam(value = "dockerImage", required = true) @RequestParam(value = "dockerImage") final String dockerImage) throws UserNotFoundException, UserInitException {
        jupyterHubService.startServer(getUserI(username), xsiType, itemId, itemLabel, projectId, eventTrackingId, dockerImage);
    }

    @ApiOperation(value = "Starts a Jupyter server for the user",
                  notes = "Use the Event Tracking API to track progress.")
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully started"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{servername}", method = POST, restrictTo = AccessLevel.User)
    public void startNamedServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                                 @ApiParam(value = "servername", required = true) @PathVariable("servername") final String servername,
                                 @ApiParam(value = "xsiType", required = true) @RequestParam("xsiType") final String xsiType,
                                 @ApiParam(value = "itemId", required = true) @RequestParam("itemId") final String itemId,
                                 @ApiParam(value = "itemLabel", required = true) @RequestParam("itemLabel") final String itemLabel,
                                 @ApiParam(value = "projectId", required = true) @RequestParam("projectId") final String projectId,
                                 @ApiParam(value = "eventTrackingId", required = true) @RequestParam(value = "eventTrackingId") final String eventTrackingId,
                                 @ApiParam(value = "dockerImage", required = true) @RequestParam(value = "dockerImage") final String dockerImage) throws UserNotFoundException, UserInitException {
        jupyterHubService.startServer(getUserI(username), servername, xsiType, itemId, itemLabel, projectId, eventTrackingId, dockerImage);
    }

    // TODO start named server

    @ApiOperation(value = "Returns the last known user options for the default server", response = XnatUserOptions.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved user options."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"}) // TODO is this the behavior/role we want?
    @XapiRequestMapping(value = "/users/{username}/server/user-options", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public XnatUserOptions getUserOptions(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException, NotFoundException {
        UserI user = getUserManagementService().getUser(username);
        return jupyterHubUserOptionsService.retrieveUserOptions(user).orElseThrow(() -> new NotFoundException("Jupyter server configuration not found."));
    }

    @ApiOperation(value = "Returns the last known user options for the named server", response = XnatUserOptions.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully retrieved user options."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"}) // TODO is this the behavior/role we want?
    @XapiRequestMapping(value = "/users/{username}/server/{servername}/user-options", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public XnatUserOptions getUserOptions(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                                          @ApiParam(value = "servername", required = true) @PathVariable("servername") @Username final String servername) throws UserNotFoundException, UserInitException, NotFoundException {
        UserI user = getUserManagementService().getUser(username);
        return jupyterHubUserOptionsService.retrieveUserOptions(user, servername).orElseThrow(() -> new NotFoundException("User options not found."));
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

    @ApiOperation(value = "Stops a users named Jupyter server", notes = "Use the Event Tracking API to track progress.")
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

    private UserI getUserI(final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username);
    }
}
