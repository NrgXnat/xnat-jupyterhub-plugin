package org.nrg.xnatx.plugins.jupyterhub.rest;

import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@Api("JupyterHub Profiles API")
@XapiRestController
@RequestMapping("/jupyterhub/profiles")
@Slf4j
public class JupyterHubProfilesApi extends AbstractXapiRestController {

    private final ProfileService profileService;

    @Autowired
    public JupyterHubProfilesApi(final UserManagementServiceI userManagementService,
                                 final RoleHolder roleHolder,
                                 final ProfileService profileService) {
        super(userManagementService, roleHolder);
        this.profileService = profileService;
    }

    @ApiOperation(value = "Get a profiles.", response = Profile.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public Profile get(@ApiParam(value = "id", required = true) @PathVariable final Long id) throws NotFoundException {
        return profileService.get(id).orElseThrow(() -> new NotFoundException("No profile with id " + id + " exists."));
    }

    @ApiOperation(value = "Get all profiles.", response = Profile.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profiles successfully retrieved."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = GET, restrictTo = AccessLevel.Authenticated)
    public List<Profile> getAll() {
        return profileService.getAll()
                .stream()
                .sorted(Comparator.comparing(Profile::getName)).collect(toList());
    }

    @ApiOperation(value = "Create a profile.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Profile successfully created."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @XapiRequestMapping(value = "", produces = APPLICATION_JSON_VALUE, method = POST, restrictTo = AccessLevel.Admin)
    public Long create(@ApiParam(value = "profile", required = true) @RequestBody final Profile profile) throws DataFormatException {
        return profileService.create(profile);
    }

    @ApiOperation(value = "Update a profile.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully updated."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = PUT, restrictTo = AccessLevel.Admin)
    public void update(@ApiParam(value = "id", required = true) @PathVariable final Long id,
                       @ApiParam(value = "profile", required = true) @RequestBody final Profile profile) throws NotFoundException, DataFormatException {
        if (!id.equals(profile.getId())) {
            throw new DataFormatException("The profile ID in the path must match the ID in the body.");
        } else if (!profileService.exists(id)) {
            throw new NotFoundException("No profile with id " + id + " exists.");
        }

        profileService.update(profile);
    }

    @ApiOperation(value = "Delete a profile.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile successfully deleted."),
            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
            @ApiResponse(code = 403, message = "Not authorized."),
            @ApiResponse(code = 500, message = "Unexpected error")
    })
    @XapiRequestMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, method = DELETE, restrictTo = AccessLevel.Admin)
    public void delete(@ApiParam(value = "id", required = true) @PathVariable final Long id) throws NotFoundException {
        if (!profileService.exists(id)) {
            throw new NotFoundException("No profile with id " + id + " exists.");
        }

        profileService.delete(id);
    }

}
