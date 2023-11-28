package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.authorization.JupyterUserAuthorization;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("JupyterHub Dashboard API")
@XapiRestController
@RequestMapping("/jupyterhub/dashboards")
@Slf4j
public class JupyterHubDashboardApi extends AbstractXapiRestController {

    private final DashboardService dashboardService;

    @Autowired
    protected JupyterHubDashboardApi(final UserManagementServiceI userManagementService,
                                     final RoleHolder roleHolder,
                                     final DashboardService dashboardService) {
        super(userManagementService, roleHolder);
        this.dashboardService = dashboardService;
    }

    @ApiOperation(value = "Get all dashboards.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the list of dashboards"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Iterable<Dashboard> getDashboards() {
        return dashboardService.getAll();
    }

    @ApiOperation(value = "Create a dashboard.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created or updated the dashboard"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @AuthDelegate(JupyterUserAuthorization.class)
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = AccessLevel.Authorizer)
    public Dashboard createDashboard(@RequestBody Dashboard dashboard) {
        return dashboardService.create(dashboard);
    }

    @ApiOperation(value = "Get a dashboard.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the dashboard."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Dashboard getDashboard(@ApiParam(value = "id", required = true) @PathVariable("id") Long id) throws NotFoundException {
        return dashboardService.get(id)
                               .orElseThrow(() -> new NotFoundException("Dashboard", id));
    }

    @ApiOperation(value = "Update a dashboard.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated the dashboard."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @AuthDelegate(JupyterUserAuthorization.class)
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Authorizer)
    public Dashboard updateDashboard(@ApiParam(value = "id") @PathVariable("id") Long id,
                                     @ApiParam(value = "dashboard") @RequestBody Dashboard dashboard) throws NotFoundException {
        if (!id.equals(dashboard.getId())) {
            throw new IllegalArgumentException("Dashboard ID does not match");
        }

        return dashboardService.update(dashboard);
    }

    @ApiOperation(value = "Delete a dashboard.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Successfully deleted the dashboard."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @AuthDelegate(JupyterUserAuthorization.class)
    @XapiRequestMapping(value = "/{id}", method = DELETE, restrictTo = AccessLevel.Authorizer)
    public void deleteDashboard(@ApiParam(value = "id", required = true) @PathVariable("id") Long id) throws NotFoundException {
        Dashboard dashboard = dashboardService.get(id)
                                              .orElseThrow(() -> new NotFoundException("Dashboard", id));

//        if (!dashboard.getOwner().equals(getSessionUser().getUsername())) {
//            throw new IllegalArgumentException("Dashboard does not belong to user");
//        }

        dashboardService.delete(id);
    }

}
