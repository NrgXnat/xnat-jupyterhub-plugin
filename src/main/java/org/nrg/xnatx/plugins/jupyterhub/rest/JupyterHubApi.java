package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xapi.rest.Username;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.om.*;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.models.ContainerMount;
import org.nrg.xnatx.plugins.jupyterhub.models.Container;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("JupyterHub API")
@XapiRestController
@RequestMapping("/jupyterhub")
@Slf4j
public class JupyterHubApi extends AbstractXapiRestController {

    private final SiteConfigPreferences siteConfigPreferences;
    private final AliasTokenService aliasTokenService;
    private final JupyterHubPreferences jupyterHubPreferences;
    private final JupyterHubService jupyterHubService;

    @Autowired
    protected JupyterHubApi(final UserManagementServiceI userManagementService,
                            final RoleHolder roleHolder,
                            final SiteConfigPreferences siteConfigPreferences,
                            final AliasTokenService aliasTokenService,
                            final JupyterHubPreferences jupyterHubPreferences,
                            final JupyterHubService jupyterHubService) {
        super(userManagementService, roleHolder);
        this.jupyterHubPreferences = jupyterHubPreferences;
        this.jupyterHubService = jupyterHubService;
        this.siteConfigPreferences = siteConfigPreferences;
        this.aliasTokenService = aliasTokenService;
    }

    @ApiOperation(value = "Returns the full map of JupyterHub plugin preferences.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub configuration preferences successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access JupyterHub preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/preferences", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Map<String, Object> getJupyterHubPreferences() {
        final UserI user      = getSessionUser();
        final String username = user.getUsername();

        log.debug("User {} requested the JupyterHub preferences", username);

        return jupyterHubPreferences.entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @ApiOperation(value = "Sets a map of JupyterHub plugin preferences.", notes = "Sets the JupyterHub preferences specified in the map.")
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub preferences successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set JupyterHub plugin preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/preferences", consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = AccessLevel.Admin)
    public void setJupyterHubPreferences(@ApiParam(value = "The map of JupyterHub preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) {
        if (!preferences.isEmpty()) {
            for (final String name : preferences.keySet()) {
                try {
                    final Object value = preferences.get(name);
                    if (value instanceof List) {
                        // noinspection rawtypes
                        jupyterHubPreferences.setListValue(name, (List) value);
                    } else if (value instanceof Map) {
                        // noinspection rawtypes
                        jupyterHubPreferences.setMapValue(name, (Map) value);
                    } else if (value.getClass().isArray()) {
                        jupyterHubPreferences.setArrayValue(name, (Object[]) value);
                    } else {
                        jupyterHubPreferences.set(value.toString(), name);
                    }
                    log.info("Set property {} to value: {}", name, value);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error for the preference: {}", name);
                }
            }
        }
    }

    @ApiOperation(value = "Returns the value of the selected JupyterHub plugin preference.", notes = "Complex objects may be returned as encapsulated JSON strings.", response = Object.class)
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub plugin preference successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to access JupyterHub plugin preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"})
    @XapiRequestMapping(value = "/preferences/{preference}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Object getSpecifiedJupyterHubPreference(@ApiParam(value = "The JupyterHub plugin preference to retrieve.", required = true) @PathVariable final String preference) throws NotFoundException {
        if (!jupyterHubPreferences.containsKey(preference)) {
            throw new NotFoundException("No JupyterHub plugin preference named " + preference);
        }
        final Object value = jupyterHubPreferences.get(preference);
        log.debug("User {} requested the value for the JupyterHub plugin preference {}, got value: {}", getSessionUser().getUsername(), preference, value);
        return value;
    }

    @ApiOperation(value = "Sets a single JupyterHub plugin preference.", notes = "Sets the JupyterHub plugin preference specified in the URL to the value set in the body.")
    @ApiResponses({@ApiResponse(code = 200, message = "JupyterHub plugin preference successfully set."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized to set JupyterHub plugin preferences."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/{preference}", consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = AccessLevel.Admin)
    public void setSpecificPixiPreference(@ApiParam(value = "The preference to be set.", required = true) @PathVariable("preference") final String preference,
                                          @ApiParam("The value to be set for the property.") @RequestBody final String value) throws InvalidPreferenceName {
        log.info("User '{}' set the value of the JupyterHub plugin preference {} to: {}", getSessionUser().getUsername(), preference, value);
        jupyterHubPreferences.set(value, preference);
    }

    @ApiOperation(value = "Returns the user's workspace directory", response = Map.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User workspace successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"}) // TODO is this the behavior/role we want?
    @XapiRequestMapping(value = "/users/{username}/workspace", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public Map<String, String> getUserWorkspace(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException {
        final Map<String, String> workspaces = new HashMap<>();

        UserI user = getUserManagementService().getUser(username);
        Path userWorkspacePath = jupyterHubService.getUserWorkspace(user);

        workspaces.put(username, translatePath(userWorkspacePath.toString()));
        return workspaces;
    }

    @ApiOperation(value = "Get a Jupyter Hub user by name.", response = User.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User found."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}", method = GET, restrictTo = AccessLevel.User)
    public User getUser(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws NotFoundException {
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
    @XapiRequestMapping(value = "/users/{username}/server", method = POST, restrictTo = AccessLevel.User)
    public Server getServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException {
        return jupyterHubService.getServer(getUserI(username)).orElse(null);
    }

    @ApiOperation(value = "Starts a Jupyter server for the user", response = String.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully started"),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{xsiType}/{id}", method = POST, restrictTo = AccessLevel.User)
    public String startServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                              @ApiParam(value = "xsiType", required = true) @PathVariable("xsiType") final String xsiType,
                              @ApiParam(value = "id", required = true) @PathVariable("id") final String id) throws NotFoundException, ResourceAlreadyExistsException, UserNotFoundException, UserInitException {
        XnatUserOptions userOptions = XnatUserOptions.builder()
                                                     .xsiType(xsiType)
                                                     .id(id)
                                                     .build();

        Server server = jupyterHubService.startServer(getUserI(username), userOptions);
        // TODO return type? return Server?
        return server.getUrl();
    }

    @ApiOperation(value = "Returns map of paths for the requested item", notes = "Returns a map from item label => item path", response = String.class, responseContainer = "Map")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to access site configuration properties."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @AuthorizedRoles({"JupyterHub", "Administrator"}) // TODO is this the behavior/role we want?
    @XapiRequestMapping(value = "/users/{username}/server/config", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Role)
    public Container getServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                               @ApiParam(value = "xsiType", required = true) @RequestParam("xsiType") final String xsiType,
                               @ApiParam(value = "id", required = true) @RequestParam("id") final String id) throws UserNotFoundException, UserInitException {
        // TODO Support named server
        // TODO New Return Type? Wrap in another DTO
        Map<String, String> paths = new HashMap<>();

        // This method is intended to be called by JupyterHub and not the user
        UserI user = getUserManagementService().getUser(username);

        switch (xsiType) {
            case (XnatProjectdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(jupyterHubService.getProjectPaths(user, Collections.singletonList(id)));
                break;
            }
            case (XnatSubjectdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(jupyterHubService.getSubjectPaths(user, id));
                break;
            }
            case (XnatExperimentdata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(jupyterHubService.getExperimentPath(user, id));
                break;
            }
            case (XnatImagescandata.SCHEMA_ELEMENT_NAME): {
                paths.putAll(jupyterHubService.getImageScanPath(user, Integer.parseInt(id)));
                break;
            }
            case (XdatStoredSearch.SCHEMA_ELEMENT_NAME): {
                Path csv = jupyterHubService.getUserWorkspace(user).resolve("searches").resolve(id + ".csv");
                paths.putAll(jupyterHubService.getStoredSearchPaths(user, id, csv));
                break;
            }
        }

        // Gather mounts
        final Path workspacePath = jupyterHubService.getUserWorkspace(user);
        final ContainerMount workspaceMount = ContainerMount.builder()
                                                            .name("user-workspace")
                                                            .writable(true)
                                                            .xnatHostPath(workspacePath.toString())
                                                            .containerHostPath(translatePath(workspacePath.toString()))
                                                            .jupyterHostPath(Paths.get("/workspace", username).toString())
                                                            .build();

        final List<ContainerMount> xnatDataMounts = paths.entrySet().stream()
                                                                    .map((entry) -> ContainerMount.builder()
                                                                                                  .name(entry.getKey())
                                                                                                  .writable(false)
                                                                                                  .xnatHostPath(entry.getValue())
                                                                                                  .containerHostPath(translatePath(entry.getValue()))
                                                                                                  .jupyterHostPath(Paths.get("/data", entry.getKey()).toString())
                                                                                                  .build())
                                                                    .collect(Collectors.toList());

        final List<ContainerMount> mounts = new ArrayList<>(Collections.emptyList());
        mounts.add(workspaceMount);
        mounts.addAll(xnatDataMounts);

        // Get env variables
        Map<String, String> environment = getDefaultEnvironmentVariables(user, xsiType, id);

        // Build config
        return Container.builder().image("xnat/jupyterhub-single-user:latest")
                                                .environment(environment)
                                                .mounts(mounts).build();
    }

    @ApiOperation(value = "Stops a users Jupyter server")
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully stopped."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Not authorized."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server", method = DELETE, restrictTo = AccessLevel.User)
    public void stopServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username) throws UserNotFoundException, UserInitException {
        UserI user = getUserManagementService().getUser(username);
        jupyterHubService.stopServer(user);
    }

    @ApiOperation(value = "Stops a users named Jupyter server")
    @ApiResponses({@ApiResponse(code = 200, message = "Jupyter server successfully stopped."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "/users/{username}/server/{serverName}", method = DELETE, restrictTo = AccessLevel.User)
    public void stopServer(@ApiParam(value = "username", required = true) @PathVariable("username") @Username final String username,
                           @ApiParam(value = "serverName", required = true) @PathVariable("serverName") final String serverName) throws UserNotFoundException, UserInitException {
        UserI user = getUserManagementService().getUser(username);
        jupyterHubService.stopServer(user, serverName);
    }

    private UserI getUserI(final String username) throws UserNotFoundException, UserInitException {
        return getUserManagementService().getUser(username);
    }

    private String translatePath(String path) {
        String pathTranslationXnatPrefix = jupyterHubPreferences.getPathTranslationXnatPrefix();
        String pathTranslationDockerPrefix = jupyterHubPreferences.getPathTranslationDockerPrefix();

        if (!StringUtils.isEmpty(pathTranslationXnatPrefix) && !StringUtils.isEmpty(pathTranslationDockerPrefix)) {
            return path.replace(pathTranslationXnatPrefix, pathTranslationDockerPrefix);
        } else {
            return path;
        }
    }

    private Map<String, String> getDefaultEnvironmentVariables(UserI user, String xsiType, String id) {
        final AliasToken token = aliasTokenService.issueTokenForUser(user);
        final String processingUrl = (String) siteConfigPreferences.getProperty("processingUrl");
        final String xnatHostUrl = StringUtils.isBlank(processingUrl) ? siteConfigPreferences.getSiteUrl() : processingUrl;

        final Map<String, String> defaultEnvironmentVariables = new HashMap<>();
        defaultEnvironmentVariables.put("XNAT_HOST", xnatHostUrl);
        defaultEnvironmentVariables.put("XNAT_USER", token.getAlias());
        defaultEnvironmentVariables.put("XNAT_PASS", token.getSecret());
        defaultEnvironmentVariables.put("XNAT_XSI_TYPE", xsiType);
        defaultEnvironmentVariables.put("XNAT_ID", id);

        return defaultEnvironmentVariables;
    }

}
