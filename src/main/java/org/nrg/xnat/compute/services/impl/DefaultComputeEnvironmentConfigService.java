package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultComputeEnvironmentConfigService implements ComputeEnvironmentConfigService {

    private final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService;
    private final HardwareConfigEntityService hardwareConfigEntityService;

    @Autowired
    public DefaultComputeEnvironmentConfigService(final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                  final HardwareConfigEntityService hardwareConfigEntityService) {
        this.computeEnvironmentConfigEntityService = computeEnvironmentConfigEntityService;
        this.hardwareConfigEntityService = hardwareConfigEntityService;
    }

    /**
     * Checks if a ComputeEnvironmentConfig with the given ID exists.
     * @param id The ID of the ComputeEnvironmentConfig to check for.
     * @return True if a ComputeEnvironmentConfig with the given ID exists, false otherwise.
     */
    @Override
    public boolean exists(Long id) {
        return computeEnvironmentConfigEntityService.exists("id", id);
    }

    /**
     * Gets a ComputeEnvironmentConfig by its ID.
     * @param id The ID of the ComputeEnvironmentConfig to retrieve.
     * @return The ComputeEnvironmentConfig with the given ID, if it exists or else an empty Optional.
     */
    @Override
    public Optional<ComputeEnvironmentConfig> retrieve(Long id) {
        ComputeEnvironmentConfigEntity entity = computeEnvironmentConfigEntityService.retrieve(id);
        return Optional.ofNullable(entity)
                .map(ComputeEnvironmentConfigEntity::toPojo);
    }

    /**
     * Get all ComputeEnvironmentConfigs.
     * @return A list of all ComputeEnvironmentConfigs.
     */
    @Override
    public List<ComputeEnvironmentConfig> getAll() {
        return computeEnvironmentConfigEntityService
                .getAll()
                .stream()
                .map(ComputeEnvironmentConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new ComputeEnvironmentConfig.
     * @param type The type of the ComputeEnvironmentConfig to create.
     * @return The newly created ComputeEnvironmentConfig.
     */
    @Override
    public List<ComputeEnvironmentConfig> getByType(ComputeEnvironmentConfig.ConfigType type) {
        return computeEnvironmentConfigEntityService
                .findByType(type)
                .stream()
                .map(ComputeEnvironmentConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Get all ComputeEnvironmentConfigs that are available to the given user and project regardless of type.
     * @param user The user to check for.
     * @param project The project to check for.
     * @return A list of all ComputeEnvironmentConfigs that are available to the given user and project.
     */
    @Override
    public List<ComputeEnvironmentConfig> getAvailable(String user, String project) {
        return getAvailable(user, project, null);
    }

    /**
     * Get all ComputeEnvironmentConfigs of the given type that are available to the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param type The type of ComputeEnvironmentConfig to check for.
     * @return A list of all ComputeEnvironmentConfigs of the given type that are available to the given user and project.
     */
    @Override
    public List<ComputeEnvironmentConfig> getAvailable(String user, String project, ComputeEnvironmentConfig.ConfigType type) {
        List<ComputeEnvironmentConfig> all;

        if (type == null) {
            all = getAll();
        } else {
            all = getByType(type);
        }

        final List<ComputeEnvironmentConfig> available = all.stream().filter(computeEnvironmentConfig -> {
            final ComputeEnvironmentScope siteScope = computeEnvironmentConfig.getScopes().get(Scope.Site);
            final ComputeEnvironmentScope projectScope = computeEnvironmentConfig.getScopes().get(Scope.Project);
            final ComputeEnvironmentScope userScope = computeEnvironmentConfig.getScopes().get(Scope.User);

            return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
        }).collect(Collectors.toList());

        available.forEach(computeEnvironmentConfig -> {
            final Set<HardwareConfig> hardwareConfigs = computeEnvironmentConfig.getHardwareOptions().getHardwareConfigs();
            final Set<HardwareConfig> availableHardware = hardwareConfigs.stream()
                    .filter(hardwareConfig -> {
                        final HardwareScope siteScope = hardwareConfig.getScopes().get(Scope.Site);
                        final HardwareScope projectScope = hardwareConfig.getScopes().get(Scope.Project);
                        final HardwareScope userScope = hardwareConfig.getScopes().get(Scope.User);

                        return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
                    }).collect(Collectors.toSet());

            computeEnvironmentConfig.getHardwareOptions().setHardwareConfigs(availableHardware);
        });

        return available;
    }

    /**
     * Checks if the given ComputeEnvironmentConfig is available to the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param id The ID of the ComputeEnvironmentConfig to check for.
     * @return True if the ComputeEnvironmentConfig with the given ID is available to the given user and project, false otherwise.
     */
    @Override
    public boolean isAvailable(String user, String project, Long id) {
        final Optional<ComputeEnvironmentConfig> computeEnvironmentConfig = retrieve(id);

        if (!computeEnvironmentConfig.isPresent()) {
            return false;
        }

        final ComputeEnvironmentScope siteScope = computeEnvironmentConfig.get().getScopes().get(Scope.Site);
        final ComputeEnvironmentScope projectScope = computeEnvironmentConfig.get().getScopes().get(Scope.Project);
        final ComputeEnvironmentScope userScope = computeEnvironmentConfig.get().getScopes().get(Scope.User);

        return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
    }

    /**
     * Creates a new ComputeEnvironmentConfig.
     * @param computeEnvironmentConfig The ComputeEnvironmentConfig to create.
     * @return The newly created ComputeEnvironmentConfig, with its ID set.
     */
    @Override
    public ComputeEnvironmentConfig create(ComputeEnvironmentConfig computeEnvironmentConfig) {
        // Validate the ComputeEnvironmentConfig
        validate(computeEnvironmentConfig);

        // Create the new compute environment config entity
        ComputeEnvironmentConfigEntity newComputeEnvironmentConfigEntity = computeEnvironmentConfigEntityService.create(ComputeEnvironmentConfigEntity.fromPojo(computeEnvironmentConfig));

        // Update the pojo with the new ID, needed for association with hardware configs
        computeEnvironmentConfig.setId(newComputeEnvironmentConfigEntity.getId());

        setHardwareConfigsForComputeEnvironmentConfig(computeEnvironmentConfig);

        return computeEnvironmentConfigEntityService.retrieve(newComputeEnvironmentConfigEntity.getId()).toPojo();
    }

    /**
     * Updates an existing ComputeEnvironmentConfig.
     * @param computeEnvironmentConfig The ComputeEnvironmentConfig to update.
     * @return The updated ComputeEnvironmentConfig.
     * @throws NotFoundException If the ComputeEnvironmentConfig to update does not exist.
     */
    @Override
    public ComputeEnvironmentConfig update(ComputeEnvironmentConfig computeEnvironmentConfig) throws NotFoundException {
        // Validate the ComputeEnvironmentConfig
        if (computeEnvironmentConfig.getId() == null) {
            throw new IllegalArgumentException("ComputeEnvironmentConfig ID cannot be null when updating");
        }

        validate(computeEnvironmentConfig);

        ComputeEnvironmentConfigEntity template = computeEnvironmentConfigEntityService.get(computeEnvironmentConfig.getId());
        template.update(computeEnvironmentConfig);
        computeEnvironmentConfigEntityService.update(template);

        // Update the pojo with the new ID, needed for association with hardware configs
        computeEnvironmentConfig.setId(template.getId());

        setHardwareConfigsForComputeEnvironmentConfig(computeEnvironmentConfig);

        return computeEnvironmentConfigEntityService.get(computeEnvironmentConfig.getId()).toPojo();
    }

    /**
     * Deletes an existing ComputeEnvironmentConfig.
     * @param id The ID of the ComputeEnvironmentConfig to delete.
     * @throws NotFoundException If the ComputeEnvironmentConfig to delete does not exist.
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (!exists(id)) {
            throw new NotFoundException("No compute environment config found with ID " + id);
        }

        // Remove all hardware configs from the compute environment config
        // Get the ids of all hardware configs associated with the compute environment config before deleting to avoid
        // a ConcurrentModificationException
        Set<Long> hardwareConfigIds = computeEnvironmentConfigEntityService.retrieve(id)
                .getHardwareOptions()
                .getHardwareConfigs()
                .stream()
                .map(HardwareConfigEntity::getId)
                .collect(Collectors.toSet());

        hardwareConfigIds.forEach(hardwareConfigId -> {
            computeEnvironmentConfigEntityService.removeHardwareConfigEntity(id, hardwareConfigId);
        });

        computeEnvironmentConfigEntityService.delete(id);
    }

    /**
     * Checks if the scope is enabled for the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param siteScope The site scope to check.
     * @param userScope The user scope to check.
     * @param projectScope The project scope to check.
     * @return True if the scopes are enabled for the given user and project, false otherwise.
     */
    protected boolean isScopeEnabledForSiteUserAndProject(String user, String project, ComputeEnvironmentScope siteScope, ComputeEnvironmentScope userScope, ComputeEnvironmentScope projectScope) {
        return siteScope != null && siteScope.isEnabled() && // Site scope must be enabled
                userScope != null && (userScope.isEnabled() || userScope.getIds().contains(user)) && // User scope must be enabled for all users or the user must be in the list
                projectScope != null && (projectScope.isEnabled() || projectScope.getIds().contains(project)); // Project scope must be enabled for all projects or the project must be in the list
    }

    /**
     * Checks if the scope is enabled for the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param siteScope The site scope to check.
     * @param userScope The user scope to check.
     * @param projectScope The project scope to check.
     * @return True if the scopes are enabled for the given user and project, false otherwise.
     */
    protected boolean isScopeEnabledForSiteUserAndProject(String user, String project, HardwareScope siteScope, HardwareScope userScope, HardwareScope projectScope) {
        return siteScope != null && siteScope.isEnabled() && // Site scope must be enabled
                userScope != null && (userScope.isEnabled() || userScope.getIds().contains(user)) && // User scope must be enabled for all users or the user must be in the list
                projectScope != null && (projectScope.isEnabled() || projectScope.getIds().contains(project)); // Project scope must be enabled for all projects or the project must be in the list
    }

    /**
     * Connect the hardware config entities to the compute environment config entity.
     * @param computeEnvironmentConfig The compute environment config to connect the hardware configs to.
     */
    protected void setHardwareConfigsForComputeEnvironmentConfig(ComputeEnvironmentConfig computeEnvironmentConfig) {
        // Probably a more efficient way to do this, but it works for now
        // Remove all hardware configs from the compute environment config
        // Get the ids of all hardware configs associated with the compute environment config before deleting to avoid
        // a ConcurrentModificationException
        Set<Long> hardwareConfigIds = computeEnvironmentConfigEntityService.retrieve(computeEnvironmentConfig.getId())
                .getHardwareOptions()
                .getHardwareConfigs()
                .stream()
                .map(HardwareConfigEntity::getId)
                .collect(Collectors.toSet());

        hardwareConfigIds.forEach(hardwareConfigId -> {
            computeEnvironmentConfigEntityService.removeHardwareConfigEntity(computeEnvironmentConfig.getId(), hardwareConfigId);
        });

        // Add the hardware configs to the compute environment config
        if (computeEnvironmentConfig.getHardwareOptions().isAllowAllHardware()) {
            // Add all hardware configs to the compute environment config
            hardwareConfigEntityService.getAll().forEach(hardwareConfigEntity -> {
                computeEnvironmentConfigEntityService.addHardwareConfigEntity(computeEnvironmentConfig.getId(), hardwareConfigEntity.getId());
            });
        } else {
            // Add the specified hardware configs to the compute environment config
            computeEnvironmentConfig.getHardwareOptions().getHardwareConfigs().forEach(hardwareConfig -> {
                if (hardwareConfig.getId() == null) {
                    // cant add a hardware config that doesn't exist
                    return;
                }

                HardwareConfigEntity hardwareConfigEntity = hardwareConfigEntityService.retrieve(hardwareConfig.getId());

                if (hardwareConfigEntity == null) {
                    // cant add a hardware config that doesn't exist
                    return;
                }

                computeEnvironmentConfigEntityService.addHardwareConfigEntity(computeEnvironmentConfig.getId(), hardwareConfigEntity.getId());
            });
        }
    }

    /**
     * Validates the given ComputeEnvironmentConfig. Throws an IllegalArgumentException if the ComputeEnvironmentConfig is invalid.
     * @param config The ComputeEnvironmentConfig to validate.
     */
    protected void validate(ComputeEnvironmentConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ComputeEnvironmentConfig cannot be null");
        }

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(config.getConfigTypes()));
        errors.addAll(validate(config.getComputeEnvironment()));
        errors.addAll(validate(config.getScopes()));
        errors.addAll(validate(config.getHardwareOptions()));

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("ComputeEnvironmentConfig is invalid: " + errors);
        }
    }

    private List<String> validate(final Set<ComputeEnvironmentConfig.ConfigType> configTypes) {
        List<String> errors = new ArrayList<>();

        if (configTypes == null || configTypes.isEmpty()) {
            errors.add("ComputeEnvironmentConfig must have at least one config type");
        }

        return errors;
    }

    private List<String> validate(final ComputeEnvironment computeEnvironment) {
        List<String> errors = new ArrayList<>();

        if (computeEnvironment == null) {
            errors.add("ComputeEnvironment cannot be null");
            return errors;
        }

        if (StringUtils.isBlank(computeEnvironment.getName())) {
            errors.add("ComputeEnvironment name cannot be blank");
        }

        if (StringUtils.isBlank(computeEnvironment.getImage())) {
            errors.add("ComputeEnvironment image cannot be blank");
        }

        return errors;
    }

    private List<String> validate(final Map<Scope, ComputeEnvironmentScope> scopes) {
        List<String> errors = new ArrayList<>();

        if (scopes == null || scopes.isEmpty()) {
            errors.add("ComputeEnvironmentConfig must have at least one scope");
            return errors;
        }

        if (!scopes.containsKey(Scope.Site)) {
            errors.add("ComputeEnvironmentConfig must have a site scope");
        }

        if (!scopes.containsKey(Scope.User)) {
            errors.add("ComputeEnvironmentConfig must have a user scope");
        }

        if (!scopes.containsKey(Scope.Project)) {
            errors.add("ComputeEnvironmentConfig must have a project scope");
        }

        return errors;
    }

    private List<String> validate(final ComputeEnvironmentHardwareOptions hardwareOptions) {
        List<String> errors = new ArrayList<>();

        if (hardwareOptions == null) {
            errors.add("ComputeEnvironmentHardwareOptions cannot be null");
            return errors;
        }

        if (hardwareOptions.getHardwareConfigs() == null) {
            errors.add("ComputeEnvironmentHardwareOptions hardware configs cannot be null");
        }

        return errors;
    }

}
