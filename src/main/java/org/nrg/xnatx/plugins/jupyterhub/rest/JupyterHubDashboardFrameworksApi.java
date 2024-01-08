package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Authenticated;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Api("JupyterHub Dashboard Frameworks API")
@XapiRestController
@RequestMapping("/jupyterhub/dashboards/frameworks")
@Slf4j
public class JupyterHubDashboardFrameworksApi extends AbstractXapiRestController {

    private final DashboardFrameworkService dashboardFrameworkService;

    @Autowired
    public JupyterHubDashboardFrameworksApi(final UserManagementServiceI userManagementService,
                                            final RoleHolder roleHolder,
                                            final DashboardFrameworkService dashboardFrameworkService) {
        super(userManagementService, roleHolder);
        this.dashboardFrameworkService = dashboardFrameworkService;
    }

    @ApiOperation(value = "Get all dashboard frameworks.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the list of dashboard frameworks"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public Iterable<DashboardFramework> getAll() {
        return dashboardFrameworkService.getAll();
    }

    @ApiOperation(value = "Get a dashboard framework by name.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved the dashboard framework"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard framework not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{name}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = Authenticated)
    public DashboardFramework get(@PathVariable("name") String name) throws NotFoundException {
        return dashboardFrameworkService.get(name)
                                        .orElseThrow(() -> new NotFoundException("DashboardFramework", name));
    }

    @ApiOperation(value = "Create a dashboard framework.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully created the dashboard framework"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = Admin)
    public DashboardFramework create(@RequestBody DashboardFramework dashboardFramework) {
        return dashboardFrameworkService.create(dashboardFramework);
    }

    @ApiOperation(value = "Update a dashboard framework.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully updated the dashboard framework"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard framework not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{name}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = Admin)
    public DashboardFramework update(@RequestBody DashboardFramework dashboardFramework) throws NotFoundException {
        return dashboardFrameworkService.update(dashboardFramework);
    }

    @ApiOperation(value = "Delete a dashboard framework.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully deleted the dashboard framework"),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Dashboard framework not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{name}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = Admin)
    public void delete(@PathVariable("name") String name) throws NotFoundException {
        dashboardFrameworkService.delete(name);
    }

}
