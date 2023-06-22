package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.entities.ComputeSpecConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeSpecConfigEntityService;
import org.nrg.xnat.compute.services.ComputeSpecConfigService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultComputeSpecConfigService implements ComputeSpecConfigService {

    private final ComputeSpecConfigEntityService computeSpecConfigEntityService;
    private final HardwareConfigEntityService hardwareConfigEntityService;

    @Autowired
    public DefaultComputeSpecConfigService(final ComputeSpecConfigEntityService computeSpecConfigEntityService,
                                           final HardwareConfigEntityService hardwareConfigEntityService) {
        this.computeSpecConfigEntityService = computeSpecConfigEntityService;
        this.hardwareConfigEntityService = hardwareConfigEntityService;
    }

    /**
     * Checks if a ComputeSpecConfig with the given ID exists.
     * @param id The ID of the ComputeSpecConfig to check for.
     * @return True if a ComputeSpecConfig with the given ID exists, false otherwise.
     */
    @Override
    public boolean exists(Long id) {
        return computeSpecConfigEntityService.exists("id", id);
    }

    /**
     * Gets a ComputeSpecConfig by its ID.
     * @param id The ID of the ComputeSpecConfig to retrieve.
     * @return The ComputeSpecConfig with the given ID, if it exists or else an empty Optional.
     */
    @Override
    public Optional<ComputeSpecConfig> retrieve(Long id) {
        ComputeSpecConfigEntity entity = computeSpecConfigEntityService.retrieve(id);
        return Optional.ofNullable(entity)
                .map(ComputeSpecConfigEntity::toPojo);
    }

    /**
     * Get all ComputeSpecConfigs.
     * @return A list of all ComputeSpecConfigs.
     */
    @Override
    public List<ComputeSpecConfig> getAll() {
        return computeSpecConfigEntityService
                .getAll()
                .stream()
                .map(ComputeSpecConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new ComputeSpecConfig.
     * @param type The type of the ComputeSpecConfig to create.
     * @return The newly created ComputeSpecConfig.
     */
    @Override
    public List<ComputeSpecConfig> getByType(ComputeSpecConfig.ConfigType type) {
        return computeSpecConfigEntityService
                .findByType(type)
                .stream()
                .map(ComputeSpecConfigEntity::toPojo)
                .collect(Collectors.toList());
    }

    /**
     * Get all ComputeSpecConfigs that are available to the given user and project regardless of type.
     * @param user The user to check for.
     * @param project The project to check for.
     * @return A list of all ComputeSpecConfigs that are available to the given user and project.
     */
    @Override
    public List<ComputeSpecConfig> getAvailable(String user, String project) {
        return getAvailable(user, project, null);
    }

    /**
     * Get all ComputeSpecConfigs of the given type that are available to the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param type The type of ComputeSpecConfig to check for.
     * @return A list of all ComputeSpecConfigs of the given type that are available to the given user and project.
     */
    @Override
    public List<ComputeSpecConfig> getAvailable(String user, String project, ComputeSpecConfig.ConfigType type) {
        List<ComputeSpecConfig> all;

        if (type == null) {
            all = getAll();
        } else {
            all = getByType(type);
        }

        final List<ComputeSpecConfig> available = all.stream().filter(computeSpecConfig -> {
            final ComputeSpecScope siteScope = computeSpecConfig.getScopes().get(Scope.Site);
            final ComputeSpecScope projectScope = computeSpecConfig.getScopes().get(Scope.Project);
            final ComputeSpecScope userScope = computeSpecConfig.getScopes().get(Scope.User);

            return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
        }).collect(Collectors.toList());

        available.forEach(computeSpecConfig -> {
            final Set<HardwareConfig> hardwareConfigs = computeSpecConfig.getHardwareOptions().getHardwareConfigs();
            final Set<HardwareConfig> availableHardware = hardwareConfigs.stream()
                    .filter(hardwareConfig -> {
                        final HardwareScope siteScope = hardwareConfig.getScopes().get(Scope.Site);
                        final HardwareScope projectScope = hardwareConfig.getScopes().get(Scope.Project);
                        final HardwareScope userScope = hardwareConfig.getScopes().get(Scope.User);

                        return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
                    }).collect(Collectors.toSet());

            computeSpecConfig.getHardwareOptions().setHardwareConfigs(availableHardware);
        });

        return available;
    }

    /**
     * Checks if the given ComputeSpecConfig is available to the given user and project.
     * @param user The user to check for.
     * @param project The project to check for.
     * @param id The ID of the ComputeSpecConfig to check for.
     * @return True if the ComputeSpecConfig with the given ID is available to the given user and project, false otherwise.
     */
    @Override
    public boolean isAvailable(String user, String project, Long id) {
        final Optional<ComputeSpecConfig> computeSpecConfig = retrieve(id);

        if (!computeSpecConfig.isPresent()) {
            return false;
        }

        final ComputeSpecScope siteScope = computeSpecConfig.get().getScopes().get(Scope.Site);
        final ComputeSpecScope projectScope = computeSpecConfig.get().getScopes().get(Scope.Project);
        final ComputeSpecScope userScope = computeSpecConfig.get().getScopes().get(Scope.User);

        return isScopeEnabledForSiteUserAndProject(user, project, siteScope, userScope, projectScope);
    }

    /**
     * Creates a new ComputeSpecConfig.
     * @param computeSpecConfig The ComputeSpecConfig to create.
     * @return The newly created ComputeSpecConfig, with its ID set.
     */
    @Override
    public ComputeSpecConfig create(ComputeSpecConfig computeSpecConfig) {
        // Validate the ComputeSpecConfig
        validate(computeSpecConfig);

        // Create the new compute spec config entity
        ComputeSpecConfigEntity newComputeSpecConfigEntity = computeSpecConfigEntityService.create(ComputeSpecConfigEntity.fromPojo(computeSpecConfig));

        // Update the pojo with the new ID, needed for association with hardware configs
        computeSpecConfig.setId(newComputeSpecConfigEntity.getId());

        setHardwareConfigsForComputeSpecConfig(computeSpecConfig);

        return computeSpecConfigEntityService.retrieve(newComputeSpecConfigEntity.getId()).toPojo();
    }

    /**
     * Updates an existing ComputeSpecConfig.
     * @param computeSpecConfig The ComputeSpecConfig to update.
     * @return The updated ComputeSpecConfig.
     * @throws NotFoundException If the ComputeSpecConfig to update does not exist.
     */
    @Override
    public ComputeSpecConfig update(ComputeSpecConfig computeSpecConfig) throws NotFoundException {
        // Validate the ComputeSpecConfig
        if (computeSpecConfig.getId() == null) {
            throw new IllegalArgumentException("ComputeSpecConfig ID cannot be null when updating");
        }

        validate(computeSpecConfig);

        ComputeSpecConfigEntity template = computeSpecConfigEntityService.get(computeSpecConfig.getId());
        template.update(computeSpecConfig);
        computeSpecConfigEntityService.update(template);

        // Update the pojo with the new ID, needed for association with hardware configs
        computeSpecConfig.setId(template.getId());

        setHardwareConfigsForComputeSpecConfig(computeSpecConfig);

        return computeSpecConfigEntityService.get(computeSpecConfig.getId()).toPojo();
    }

    /**
     * Deletes an existing ComputeSpecConfig.
     * @param id The ID of the ComputeSpecConfig to delete.
     * @throws NotFoundException If the ComputeSpecConfig to delete does not exist.
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (!exists(id)) {
            throw new NotFoundException("No compute spec config found with ID " + id);
        }

        // Remove all hardware configs from the compute spec config
        // Get the ids of all hardware configs associated with the compute spec config before deleting to avoid
        // a ConcurrentModificationException
        Set<Long> hardwareConfigIds = computeSpecConfigEntityService.retrieve(id)
                .getHardwareOptions()
                .getHardwareConfigs()
                .stream()
                .map(HardwareConfigEntity::getId)
                .collect(Collectors.toSet());

        hardwareConfigIds.forEach(hardwareConfigId -> {
            computeSpecConfigEntityService.removeHardwareConfigEntity(id, hardwareConfigId);
        });

        computeSpecConfigEntityService.delete(id);
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
    protected boolean isScopeEnabledForSiteUserAndProject(String user, String project, ComputeSpecScope siteScope, ComputeSpecScope userScope, ComputeSpecScope projectScope) {
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
     * Connect the hardware config entities to the compute spec config entity.
     * @param computeSpecConfig The compute spec config to connect the hardware configs to.
     */
    protected void setHardwareConfigsForComputeSpecConfig(ComputeSpecConfig computeSpecConfig) {
        // Probably a more efficient way to do this, but it works for now
        // Remove all hardware configs from the compute spec config
        // Get the ids of all hardware configs associated with the compute spec config before deleting to avoid
        // a ConcurrentModificationException
        Set<Long> hardwareConfigIds = computeSpecConfigEntityService.retrieve(computeSpecConfig.getId())
                .getHardwareOptions()
                .getHardwareConfigs()
                .stream()
                .map(HardwareConfigEntity::getId)
                .collect(Collectors.toSet());

        hardwareConfigIds.forEach(hardwareConfigId -> {
            computeSpecConfigEntityService.removeHardwareConfigEntity(computeSpecConfig.getId(), hardwareConfigId);
        });

        // Add the hardware configs to the compute spec config
        if (computeSpecConfig.getHardwareOptions().isAllowAllHardware()) {
            // Add all hardware configs to the compute spec config
            hardwareConfigEntityService.getAll().forEach(hardwareConfigEntity -> {
                computeSpecConfigEntityService.addHardwareConfigEntity(computeSpecConfig.getId(), hardwareConfigEntity.getId());
            });
        } else {
            // Add the specified hardware configs to the compute spec config
            computeSpecConfig.getHardwareOptions().getHardwareConfigs().forEach(hardwareConfig -> {
                if (hardwareConfig.getId() == null) {
                    // cant add a hardware config that doesn't exist
                    return;
                }

                HardwareConfigEntity hardwareConfigEntity = hardwareConfigEntityService.retrieve(hardwareConfig.getId());

                if (hardwareConfigEntity == null) {
                    // cant add a hardware config that doesn't exist
                    return;
                }

                computeSpecConfigEntityService.addHardwareConfigEntity(computeSpecConfig.getId(), hardwareConfigEntity.getId());
            });
        }
    }

    /**
     * Validates the given ComputeSpecConfig. Throws an IllegalArgumentException if the ComputeSpecConfig is invalid.
     * @param config The ComputeSpecConfig to validate.
     */
    protected void validate(ComputeSpecConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ComputeSpecConfig cannot be null");
        }

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(config.getConfigTypes()));
        errors.addAll(validate(config.getComputeSpec()));
        errors.addAll(validate(config.getScopes()));
        errors.addAll(validate(config.getHardwareOptions()));

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("ComputeSpecConfig is invalid: " + errors);
        }
    }

    private List<String> validate(final Set<ComputeSpecConfig.ConfigType> configTypes) {
        List<String> errors = new ArrayList<>();

        if (configTypes == null || configTypes.isEmpty()) {
            errors.add("ComputeSpecConfig must have at least one config type");
        }

        return errors;
    }

    private List<String> validate(final ComputeSpec computeSpec) {
        List<String> errors = new ArrayList<>();

        if (computeSpec == null) {
            errors.add("ComputeSpec cannot be null");
            return errors;
        }

        if (StringUtils.isBlank(computeSpec.getName())) {
            errors.add("ComputeSpec name cannot be blank");
        }

        if (StringUtils.isBlank(computeSpec.getImage())) {
            errors.add("ComputeSpec image cannot be blank");
        }

        return errors;
    }

    private List<String> validate(final Map<Scope, ComputeSpecScope> scopes) {
        List<String> errors = new ArrayList<>();

        if (scopes == null || scopes.isEmpty()) {
            errors.add("ComputeSpecConfig must have at least one scope");
            return errors;
        }

        if (!scopes.containsKey(Scope.Site)) {
            errors.add("ComputeSpecConfig must have a site scope");
        }

        if (!scopes.containsKey(Scope.User)) {
            errors.add("ComputeSpecConfig must have a user scope");
        }

        if (!scopes.containsKey(Scope.Project)) {
            errors.add("ComputeSpecConfig must have a project scope");
        }

        return errors;
    }

    private List<String> validate(final ComputeSpecHardwareOptions hardwareOptions) {
        List<String> errors = new ArrayList<>();

        if (hardwareOptions == null) {
            errors.add("ComputeSpecHardwareOptions cannot be null");
            return errors;
        }

        if (hardwareOptions.getHardwareConfigs() == null) {
            errors.add("ComputeSpecHardwareOptions hardware configs cannot be null");
        }

        return errors;
    }

}
