package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Api("JupyterHub Plugin Preferences API")
@XapiRestController
@RequestMapping("/jupyterhub/preferences")
@Slf4j
public class JupyterHubPreferencesApi extends AbstractXapiRestController {

    private final JupyterHubPreferences jupyterHubPreferences;

    @Autowired
    public JupyterHubPreferencesApi(final UserManagementServiceI userManagementService,
                                    final RoleHolder roleHolder,
                                    final JupyterHubPreferences jupyterHubPreferences) {
        super(userManagementService, roleHolder);
        this.jupyterHubPreferences = jupyterHubPreferences;
    }

    @ApiOperation(value = "Returns the full map of JupyterHub plugin preferences.",
                  notes = "Complex objects may be returned as encapsulated JSON strings.",
                  response = String.class, responseContainer = "Map")
    @ApiResponses({
            @ApiResponse(code = 200, message = "JupyterHub plugin preferences successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to access JupyterHub preferences."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Map<String, Object> getPreferences() {
        final UserI user      = getSessionUser();
        final String username = user.getUsername();

        log.debug("User {} requested the JupyterHub preferences", username);

        return jupyterHubPreferences.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @ApiOperation(value = "Sets a map of JupyterHub plugin preferences.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "JupyterHub plugin preferences successfully set."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to set JupyterHub plugin preferences."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(consumes = {APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = AccessLevel.Admin)
    public void setPreferences(@ApiParam(value = "The map of JupyterHub preferences to be set.", required = true) @RequestBody final Map<String, Object> preferences) {
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
                    log.debug("Set property {} to value: {}", name, value);
                } catch (InvalidPreferenceName invalidPreferenceName) {
                    log.error("Got an invalid preference name error for the preference: {}", name);
                }
            }
        }
    }

    @ApiOperation(value = "Returns the value of the selected JupyterHub plugin preference.",
                  notes = "Returns singleton map from preference name to preference object.",
                  response = Map.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "JupyterHub plugin preference successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to access JupyterHub plugin preferences."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{preference}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Map<String, Object> getSpecifiedPreference(@ApiParam(value = "The JupyterHub plugin preference to retrieve.", required = true) @PathVariable final String preference) throws NotFoundException {
        if (!jupyterHubPreferences.containsKey(preference)) {
            throw new NotFoundException("No JupyterHub plugin preference named " + preference);
        }
        final Object value = jupyterHubPreferences.get(preference);
        log.debug("User {} requested the value for the JupyterHub plugin preference {}, got value: {}", getSessionUser().getUsername(), preference, value);
        return Collections.singletonMap(preference, value);
    }

    @ApiOperation(value = "Sets a single JupyterHub plugin preference.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "JupyterHub plugin preference successfully set."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized to set JupyterHub plugin preferences."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{preference}", consumes = {TEXT_PLAIN_VALUE, APPLICATION_JSON_VALUE}, method = POST, restrictTo = AccessLevel.Admin)
    public void setSpecificPreference(@ApiParam(value = "The preference to be set.", required = true) @PathVariable final String preference,
                                      @ApiParam(value = "The value to be set for the property.", required = true) @RequestBody final String value) throws InvalidPreferenceName {
        log.debug("User '{}' set the value of the JupyterHub plugin preference {} to: {}", getSessionUser().getUsername(), preference, value);
        jupyterHubPreferences.set(value, preference);
    }

}
