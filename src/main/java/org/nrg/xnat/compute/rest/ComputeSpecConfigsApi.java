package org.nrg.xnat.compute.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.compute.models.ComputeSpecConfig;
import org.nrg.xnat.compute.services.ComputeSpecConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.nrg.xdat.security.helpers.AccessLevel.Admin;
import static org.nrg.xdat.security.helpers.AccessLevel.Read;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api("ComputeSpecConfigs REST API")
@XapiRestController
@RequestMapping(value = "/compute-spec-configs")
public class ComputeSpecConfigsApi extends AbstractXapiRestController {

    private final ComputeSpecConfigService computeSpecConfigService;

    @Autowired
    public ComputeSpecConfigsApi(final UserManagementServiceI userManagementService,
                                 final RoleHolder roleHolder,
                                 final ComputeSpecConfigService computeSpecConfigService) {
        super(userManagementService, roleHolder);
        this.computeSpecConfigService = computeSpecConfigService;
    }

    @ApiOperation(value = "Get all compute spec configs or all compute spec configs for a given type.", response = ComputeSpecConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute spec configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public List<ComputeSpecConfig> getAll(@RequestParam(value = "type", required = false) final ComputeSpecConfig.ConfigType type) {
        if (type != null) {
            return computeSpecConfigService.getByType(type);
        } else {
            return computeSpecConfigService.getAll();
        }
    }

    @ApiOperation(value = "Get a compute spec config.", response = ComputeSpecConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute spec config successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute spec config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Admin)
    public ComputeSpecConfig get(@PathVariable("id") final Long id) throws NotFoundException {
        return computeSpecConfigService.retrieve(id)
                .orElseThrow(() -> new NotFoundException("Compute spec config not found."));
    }

    @ApiOperation(value = "Create a compute spec config.", response = ComputeSpecConfig.class)
    @ApiResponses({
            @ApiResponse(code = 201, message = "Compute spec config successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.POST, restrictTo = Admin)
    public ComputeSpecConfig create(@RequestBody final ComputeSpecConfig computeSpecConfig) {
        return computeSpecConfigService.create(computeSpecConfig);
    }

    @ApiOperation(value = "Update a compute spec config.", response = ComputeSpecConfig.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute spec config successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute spec config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, method = RequestMethod.PUT, restrictTo = Admin)
    public ComputeSpecConfig update(@PathVariable("id") final Long id,
                                    @RequestBody final ComputeSpecConfig computeSpecConfig) throws NotFoundException {
        if (!id.equals(computeSpecConfig.getId())) {
            throw new IllegalArgumentException("The ID in the path must match the ID in the body.");
        }

        return computeSpecConfigService.update(computeSpecConfig);
    }

    @ApiOperation(value = "Delete a compute spec config.")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Compute spec config successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 404, message = "Compute spec config not found."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @XapiRequestMapping(value = "/{id}", method = RequestMethod.DELETE, restrictTo = Admin)
    public void delete(@PathVariable("id") final Long id) throws NotFoundException {
        computeSpecConfigService.delete(id);
    }

    @ApiOperation(value = "Get all available compute spec configs for the given user and project.", response = ComputeSpecConfig.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Compute spec configs successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/available", produces = APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = Read)
    public List<ComputeSpecConfig> getAvailable(@RequestParam(value = "user") final String user,
                                                @RequestParam(value = "project") final String project,
                                                @RequestParam(value = "type", required = false) final ComputeSpecConfig.ConfigType type) {
        return computeSpecConfigService.getAvailable(user, project, type);
    }

}
