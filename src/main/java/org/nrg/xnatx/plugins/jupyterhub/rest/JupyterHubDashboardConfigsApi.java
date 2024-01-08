package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.Project;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api("JupyterHub Dashboard Configs API")
@XapiRestController
@RequestMapping("/jupyterhub/dashboards/configs")
@Slf4j
public class JupyterHubDashboardConfigsApi extends AbstractXapiRestController {

    private final DashboardConfigService dashboardConfigService;

    @Autowired
    public JupyterHubDashboardConfigsApi(final UserManagementServiceI userManagementService,
                                     final RoleHolder roleHolder,
                                     final DashboardConfigService dashboardConfigService) {
        super(userManagementService, roleHolder);
        this.dashboardConfigService = dashboardConfigService;
    }

    @ApiOperation(value = "Get all dashboard configs.", response = DashboardConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the list of dashboard configs"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Authenticated)
    public Iterable<DashboardConfig> getDashboardConfigs() {
        return dashboardConfigService.getAll();
    }

    @ApiOperation(value = "Get a dashboard config.", response = DashboardConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the dashboard config"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Authenticated)
    public DashboardConfig getDashboardConfig(@PathVariable("id") final Long id) throws NotFoundException {
        return dashboardConfigService.retrieve(id).orElseThrow(() -> new NotFoundException("Dashboard config not found."));
    }

    @ApiOperation(value = "Create a dashboard config.", response = DashboardConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created or updated the dashboard config"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = AccessLevel.Admin)
    public DashboardConfig createDashboardConfig(@RequestBody final DashboardConfig dashboardConfig) {
        return dashboardConfigService.create(dashboardConfig);
    }

    @ApiOperation(value = "Update a dashboard config.", response = DashboardConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated the dashboard config"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "Dashboard config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = AccessLevel.Admin)
    public DashboardConfig updateDashboardConfig(@PathVariable("id") final Long id,
                                                 @RequestBody final DashboardConfig dashboardConfig) throws NotFoundException {
        if (!dashboardConfig.getId().equals(id)) {
            throw new IllegalArgumentException("Dashboard config id does not match id in request path.");
        }

        return dashboardConfigService.update(dashboardConfig);
    }

    @ApiOperation(value = "Delete a dashboard config.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Dashboard config successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 404, message = "Dashboard config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = RequestMethod.DELETE, restrictTo = AccessLevel.Admin)
    public void deleteDashboardConfig(@PathVariable("id") final Long id) throws NotFoundException {
        dashboardConfigService.delete(id);
    }

    @ApiOperation(value = "Get all available dashboard configs.", response = DashboardConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the list of available dashboard configs"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/available", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Authenticated)
    public Iterable<DashboardConfig> getAvailableDashboardConfigs(@RequestParam final Map<String, String> scopes) {
        Map<Scope, String> executionScope = scopes.entrySet().stream()
                                                  .filter(entry -> Scope.getCodes().contains(entry.getKey()))
                                                  .collect(Collectors.toMap(entry -> Scope.getScope(entry.getKey()), Map.Entry::getValue));

        return dashboardConfigService.getAvailable(executionScope);
    }

    @ApiOperation(value = "Enable a dashboard config at the site level.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully enabled the dashboard config at the site level."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}/scope/site", method = RequestMethod.POST, restrictTo = AccessLevel.Admin)
    public void enableDashboardConfigAtSite(@PathVariable("id") final Long id) throws NotFoundException {
        dashboardConfigService.enableForSite(id);
    }

    @ApiOperation(value = "Disable a dashboard config at the site level.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully disabled the dashboard config at the site level."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}/scope/site", method = RequestMethod.DELETE, restrictTo = AccessLevel.Admin)
    public void disableDashboardConfigAtSite(@PathVariable("id") final Long id) throws NotFoundException {
        dashboardConfigService.disableForSite(id);
    }

    @ApiOperation(value = "Enable a dashboard config at the project level.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully enabled the dashboard config at the project level."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}/scope/project/{projectId}", method = RequestMethod.POST, restrictTo = AccessLevel.Edit)
    public void enableDashboardConfigAtProject(@PathVariable("id") final Long id,
                                               @PathVariable("projectId") @Project final String projectId) throws NotFoundException {
        dashboardConfigService.enableForProject(id, projectId);
    }

    @ApiOperation(value = "Disable a dashboard config at the project level.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully disabled the dashboard config at the project level."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}/scope/project/{projectId}", method = RequestMethod.DELETE, restrictTo = AccessLevel.Edit)
    public void disableDashboardConfigAtProject(@PathVariable("id") final Long id,
                                                @PathVariable("projectId") @Project final String projectId) throws NotFoundException {
        dashboardConfigService.disableForProject(id, projectId);
    }

}
