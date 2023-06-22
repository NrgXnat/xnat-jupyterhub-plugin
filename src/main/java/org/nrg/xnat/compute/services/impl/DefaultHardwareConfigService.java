package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.entities.ComputeSpecConfigEntity;
import org.nrg.xnat.compute.entities.ComputeSpecHardwareOptionsEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.Hardware;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnat.compute.models.HardwareScope;
import org.nrg.xnat.compute.services.ComputeSpecConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultHardwareConfigService implements HardwareConfigService {

    private final HardwareConfigEntityService hardwareConfigEntityService;
    private final ComputeSpecConfigEntityService computeSpecConfigEntityService;

    @Autowired
    public DefaultHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                        final ComputeSpecConfigEntityService computeSpecConfigEntityService) {
        this.hardwareConfigEntityService = hardwareConfigEntityService;
        this.computeSpecConfigEntityService = computeSpecConfigEntityService;
    }

    /**
     * Checks if a hardware config with the given id exists.
     * @param id The id of the hardware config to check for
     * @return True if a hardware config with the given id exists, false otherwise
     */
    @Override
    public boolean exists(Long id) {
        return hardwareConfigEntityService.exists("id", id);
    }

    /**
     * Returns the hardware config with the given id.
     * @param id The id of the hardware config to retrieve
     * @return An optional containing the hardware config with the given id, or empty if no such hardware config exists
     */
    @Override
    public Optional<HardwareConfig> retrieve(Long id) {
        HardwareConfigEntity entity = hardwareConfigEntityService.retrieve(id);
        return Optional.ofNullable(entity).map(HardwareConfigEntity::toPojo);
    }

    /**
     * Returns all hardware configs
     * @return List of all hardware configs
     */
    @Override
    public List<HardwareConfig> retrieveAll() {
        return hardwareConfigEntityService
                .getAll()
                .stream()
                .map(HardwareConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new hardware config.
     * @param hardwareConfig The hardware config to create
     * @return The newly created hardware config with an id
     */
    @Override
    public HardwareConfig create(HardwareConfig hardwareConfig) {
        // Validate the hardware config
        validate(hardwareConfig);

        // Create the new hardware config entity
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig));

        // Add the hardware config to all compute spec configs that allow all hardware
        computeSpecConfigEntityService.getAll().stream()
                .map(ComputeSpecConfigEntity::getHardwareOptions)
                .filter(ComputeSpecHardwareOptionsEntity::isAllowAllHardware)
                .forEach(hardwareOptions -> {
                    computeSpecConfigEntityService.addHardwareConfigEntity(hardwareOptions.getId(), hardwareConfigEntity.getId());
                });

        return hardwareConfigEntityService.retrieve(hardwareConfigEntity.getId()).toPojo();
    }

    /**
     * Updates an existing hardware config.
     * @param hardwareConfig The hardware config to update
     * @return The updated hardware config
     * @throws NotFoundException If no hardware config exists with the given id
     */
    @Override
    public HardwareConfig update(HardwareConfig hardwareConfig) throws NotFoundException {
        // Validate the hardware config
        if (hardwareConfig.getId() == null) {
            throw new IllegalArgumentException("Hardware config id cannot be null");
        }

        validate(hardwareConfig);

        // Update the hardware config
        HardwareConfigEntity template = hardwareConfigEntityService.get(hardwareConfig.getId());
        template.update(hardwareConfig);
        hardwareConfigEntityService.update(template);
        return hardwareConfigEntityService.get(hardwareConfig.getId()).toPojo();
    }

    /**
     * Deletes the hardware config with the given id.
     * @param id The id of the hardware config to delete
     * @throws NotFoundException If no hardware config exists with the given id
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (!exists(id)) {
            throw new NotFoundException("No hardware config found with id " + id);
        }

        // Remove the hardware config from all compute spec configs
        // Probably a more efficient way to do this, but this is the easiest way to do it
        computeSpecConfigEntityService.getAll().stream()
                .map(ComputeSpecConfigEntity::getHardwareOptions)
                .forEach(hardwareOptions -> {
                    computeSpecConfigEntityService.removeHardwareConfigEntity(hardwareOptions.getId(), id);
                });

        hardwareConfigEntityService.delete(id);
    }

    /**
     * Checks if the hardware config with the given id is available for the given user and project.
     * @param user The user to check for
     * @param project The project to check for
     * @param id The id of the hardware config to check for
     * @return True if the hardware config with the given id is available for the given user and project, false otherwise.
     */
    @Override
    public boolean isAvailable(String user, String project, Long id) {
        final Optional<HardwareConfig> hardwareConfig = retrieve(id);

        if (!hardwareConfig.isPresent()) {
            log.error("No hardware config found with id " + id);
            return false;
        }

        final HardwareScope siteScope = hardwareConfig.get().getScopes().get(Scope.Site);
        final HardwareScope projectScope = hardwareConfig.get().getScopes().get(Scope.Project);
        final HardwareScope userScope = hardwareConfig.get().getScopes().get(Scope.User);

        return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
    }

    protected boolean isScopeEnabledForSiteUserAndProject(String user, String project, HardwareScope siteScope, HardwareScope userScope, HardwareScope projectScope) {
        return siteScope != null && siteScope.isEnabled() && // Site scope must be enabled
                userScope != null && (userScope.isEnabled() || userScope.getIds().contains(user)) && // User scope must be enabled for all users or the user must be in the list
                projectScope != null && (projectScope.isEnabled() || projectScope.getIds().contains(project)); // Project scope must be enabled for all projects or the project must be in the list
    }

    /**
     * Validates the given hardware config. Throws an IllegalArgumentException if the hardware config is invalid.
     * @param hardwareConfig The hardware config to validate
     */
    protected void validate(HardwareConfig hardwareConfig) {
        if (hardwareConfig == null) {
            throw new IllegalArgumentException("Hardware config cannot be null");
        }

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(hardwareConfig.getHardware()));
        errors.addAll(validate(hardwareConfig.getScopes()));

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Hardware config is invalid: " + String.join(", ", errors));
        }
    }

    private List<String> validate(final Hardware hardware) {
        List<String> errors = new ArrayList<>();

        if (hardware == null) {
            errors.add("Hardware cannot be null");
            return errors;
        }

        if (StringUtils.isBlank(hardware.getName())) {
            errors.add("Hardware name cannot be blank");
        }

        return errors;
    }

    private List<String> validate(final Map<Scope, HardwareScope> scopes) {
        List<String> errors = new ArrayList<>();

        if (scopes == null || scopes.isEmpty()) {
            errors.add("Hardware scopes cannot be null or empty");
            return errors;
        }

        if (!scopes.containsKey(Scope.Site)) {
            errors.add("Hardware scopes must contain a site scope");
        }

        if (!scopes.containsKey(Scope.User)) {
            errors.add("Hardware scopes must contain a user scope");
        }

        if (!scopes.containsKey(Scope.Project)) {
            errors.add("Hardware scopes must contain a project scope");
        }

        return errors;
    }

}
