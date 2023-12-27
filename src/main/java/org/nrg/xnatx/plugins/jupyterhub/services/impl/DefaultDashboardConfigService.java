package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.AccessScope;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardConfigEntity;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardScope;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultDashboardConfigService implements DashboardConfigService {

    private final DashboardConfigEntityService dashboardConfigEntityService;
    private final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService;
    private final HardwareConfigEntityService hardwareConfigEntityService;
    private final DashboardFrameworkEntityService dashboardFrameworkEntityService;

    @Autowired
    public DefaultDashboardConfigService(final DashboardConfigEntityService dashboardConfigEntityService,
                                         final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                         final HardwareConfigEntityService hardwareConfigEntityService,
                                         final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        super();
        this.dashboardConfigEntityService = dashboardConfigEntityService;
        this.computeEnvironmentConfigEntityService = computeEnvironmentConfigEntityService;
        this.hardwareConfigEntityService = hardwareConfigEntityService;
        this.dashboardFrameworkEntityService = dashboardFrameworkEntityService;
    }

    /**
     * Checks if a dashboard config exists with the given id
     * @param id The dashboard config id to check for
     * @return True if a dashboard config exists with the given id, false otherwise
     */
    @Override
    public boolean exists(Long id) {
        return dashboardConfigEntityService.exists("id", id);
    }

    /**
     * Retrieves a dashboard config with the given id
     * @param id The id of the dashboard config to retrieve
     * @return The dashboard config with the given id, if found
     */
    @Override
    public Optional<DashboardConfig> retrieve(Long id) {
        DashboardConfigEntity entity = dashboardConfigEntityService.retrieve(id);
        return Optional.ofNullable(entity).map(DashboardConfigEntity::toPojo);
    }

    /**
     * Retrieves all dashboard configs
     * @return A list of all dashboard configs
     */
    @Override
    public List<DashboardConfig> getAll() {
        return dashboardConfigEntityService.getAll().stream()
                                           .map(DashboardConfigEntity::toPojo)
                                           .collect(Collectors.toList());
    }

    /**
     * Creates a new dashboard config
     * @param dashboardConfig The dashboard config to create
     * @return The created dashboard config
     */
    @Override
    public DashboardConfig create(DashboardConfig dashboardConfig) {
        validate(dashboardConfig);

        // Create the dashboard config
        DashboardConfigEntity dashboardConfigEntity = dashboardConfigEntityService.create(DashboardConfigEntity.fromPojo(dashboardConfig));

        // Then add the compute environment config and hardware config
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity = computeEnvironmentConfigEntityService.retrieve(dashboardConfig.getComputeEnvironmentConfig().getId());
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigEntityService.retrieve(dashboardConfig.getHardwareConfig().getId());

        // Then add the dashboard framework if it's not custom
        if (StringUtils.isNotBlank(dashboardConfig.getDashboard().getFramework()) &&
            !dashboardConfig.getDashboard().getFramework().equalsIgnoreCase("custom")) {
            DashboardFrameworkEntity dashboardFrameworkEntity = dashboardFrameworkEntityService.findFrameworkByName(dashboardConfig.getDashboard().getFramework())
                                                                                               .orElseThrow(() -> new IllegalArgumentException("Dashboard framework does not exist"));
            dashboardConfigEntity.getDashboard().setDashboardFramework(dashboardFrameworkEntity);
        } else {
            dashboardConfigEntity.getDashboard().setDashboardFramework(null);
        }

        if (computeEnvironmentConfigEntity == null) {
            throw new IllegalArgumentException("Compute environment config does not exist");
        }

        if (hardwareConfigEntity == null) {
            throw new IllegalArgumentException("Hardware config does not exist");
        }

        dashboardConfigEntity.setComputeEnvironmentConfig(computeEnvironmentConfigEntity);
        dashboardConfigEntity.setHardwareConfig(hardwareConfigEntity);

        dashboardConfigEntityService.update(dashboardConfigEntity);

        // Then return the dashboard config
        return dashboardConfigEntityService.retrieve(dashboardConfigEntity.getId()).toPojo();
    }

    /**
     * Updates a dashboard config
     * @param dashboardConfig The dashboard config to update
     * @return The updated dashboard config
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public DashboardConfig update(DashboardConfig dashboardConfig) throws NotFoundException {
        // Validate
        if (dashboardConfig == null) {
            // Can't check ID if null
            throw new IllegalArgumentException("Dashboard config cannot be null");
        } else if (dashboardConfig.getId() == null) {
            // ID is required for update, but not create
            throw new IllegalArgumentException("Dashboard config id cannot be null");
        } else if (!exists(dashboardConfig.getId())) {
            // Can't update a dashboard config that doesn't exist
            throw new NotFoundException("Dashboard config not found");
        }

        validate(dashboardConfig);

        // Update the dashboard config
        DashboardConfigEntity entity = dashboardConfigEntityService.retrieve(dashboardConfig.getId());
        entity.update(dashboardConfig);

        // Update the compute environment config
        ComputeEnvironmentConfigEntity computeEnvironmentConfigEntity = computeEnvironmentConfigEntityService.retrieve(dashboardConfig.getComputeEnvironmentConfig().getId());
        entity.setComputeEnvironmentConfig(computeEnvironmentConfigEntity);

        // Update the hardware config
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigEntityService.retrieve(dashboardConfig.getHardwareConfig().getId());
        entity.setHardwareConfig(hardwareConfigEntity);

        // Then add the dashboard framework if it's not custom
        if (StringUtils.isNotBlank(dashboardConfig.getDashboard().getFramework()) &&
            !dashboardConfig.getDashboard().getFramework().equalsIgnoreCase("custom")) {
            DashboardFrameworkEntity dashboardFrameworkEntity = dashboardFrameworkEntityService.findFrameworkByName(dashboardConfig.getDashboard().getFramework())
                                                                                               .orElseThrow(() -> new IllegalArgumentException("Dashboard framework does not exist"));
            entity.getDashboard().setDashboardFramework(dashboardFrameworkEntity);
        } else {
            entity.getDashboard().setDashboardFramework(null);
        }

        // Save the dashboard config
        dashboardConfigEntityService.update(entity);

        // Return the updated dashboard config
        return dashboardConfigEntityService.retrieve(entity.getId()).toPojo();
    }

    /**
     * Deletes a dashboard config
     * @param id The id of the dashboard config to delete
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (!exists(id)) {
            throw new NotFoundException("DashboardConfigEntity", id);
        }

        dashboardConfigEntityService.delete(id);
    }

    /**
     * Checks if a dashboard config is available
     * @param id The id of the dashboard config to check
     * @param executionScope The execution scope to check
     * @return True if available, false otherwise
     */
    @Override
    public boolean isAvailable(Long id, Map<Scope, String> executionScope) {
        final Optional<DashboardConfig> dashboardConfig = retrieve(id);

        if (!dashboardConfig.isPresent()) {
            return false;
        }

        Map<Scope, AccessScope> requiredScopes = dashboardConfig.get().getScopes()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return AccessScope.isEnabledFor(requiredScopes, executionScope);
    }

    /**
     * Gets all available dashboard configs for the given execution scope
     * @param executionScope The execution scope to check
     * @return A list of all available dashboard configs
     */
    @Override
    public List<DashboardConfig> getAvailable(Map<Scope, String> executionScope) {
        return getAll().stream()
                       .filter(dashboardConfig -> isAvailable(dashboardConfig.getId(), executionScope))
                       .collect(Collectors.toList());
    }

    /**
     * Checks if a dashboard config is valid
     * @param config The dashboard config to check
     * @return True if valid, false otherwise
     */
    @Override
    public boolean isValid(DashboardConfig config) {
        try {
            validate(config);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Enables a dashboard config for the site
     * @param id The id of the dashboard config to enable
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public void enableForSite(Long id) throws NotFoundException {
        // Get the dashboard config
        final Optional<DashboardConfig> dashboardConfig = retrieve(id);

        // Make sure it exists
        if (!dashboardConfig.isPresent()) {
            throw new IllegalArgumentException("Dashboard config does not exist");
        }

        // Enable the site scope
        final DashboardConfig config = dashboardConfig.get();
        config.getScopes().get(Scope.Site).setEnabled(true);

        update(config);
    }

    /**
     * Disables a dashboard config for the site
     * @param id The id of the dashboard config to disable
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public void disableForSite(Long id) throws NotFoundException {
        // Get the dashboard config
        final Optional<DashboardConfig> dashboardConfig = retrieve(id);

        // Make sure it exists
        if (!dashboardConfig.isPresent()) {
            throw new IllegalArgumentException("Dashboard config does not exist");
        }

        // Disable the site scope
        final DashboardConfig config = dashboardConfig.get();
        config.getScopes().get(Scope.Site).setEnabled(false);

        update(config);
    }

    /**
     * Enables a dashboard config for the specified project
     * @param id The id of the dashboard config to enable
     * @param projectId The id of the project to enable the dashboard config for
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public void enableForProject(Long id, String projectId) throws NotFoundException {
        // Get the dashboard config
        final Optional<DashboardConfig> dashboardConfig = retrieve(id);

        // Make sure it exists
        if (!dashboardConfig.isPresent()) {
            throw new IllegalArgumentException("Dashboard config does not exist");
        }

        // Enable for the project
        final DashboardConfig config = dashboardConfig.get();
        config.getScopes().get(Scope.Project).getIds().add(projectId);

        update(config);
    }

    /**
     * Disables a dashboard config for the specified project
     * @param id The id of the dashboard config to disable
     * @param projectId The id of the project to disable the dashboard config for
     * @throws NotFoundException If the dashboard config does not exist
     */
    @Override
    public void disableForProject(Long id, String projectId) throws NotFoundException {
        // Get the dashboard config
        final Optional<DashboardConfig> dashboardConfig = retrieve(id);

        // Make sure it exists
        if (!dashboardConfig.isPresent()) {
            throw new IllegalArgumentException("Dashboard config does not exist");
        }

        // Disable for the project
        final DashboardConfig config = dashboardConfig.get();
        config.getScopes().get(Scope.Project).getIds().remove(projectId);

        update(config);
    }

    /**
     * Validates a dashboard config
     * @param config The dashboard config to validate
     * @throws IllegalArgumentException If invalid. The exception message will contain the validation errors.
     */
    protected void validate(final DashboardConfig config) throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException("Dashboard config cannot be null");
        }

        List<String> errors = new ArrayList<>();

        errors.addAll(validate(config.getDashboard()));
        errors.addAll(validate(config.getScopes()));

        if (config.getComputeEnvironmentConfig() == null) {
            errors.add("Compute environment config cannot be null");
        }

        if (config.getHardwareConfig() == null) {
            errors.add("Hardware config cannot be null");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Dashboard config is invalid: " + String.join(", ", errors));
        }
    }

    /**
     * Validates a dashboard
     * @param dashboard The dashboard to validate
     * @return A list of errors, if any
     */
    protected List<String> validate(final Dashboard dashboard) {
        List<String> errors = new ArrayList<>();

        if (dashboard == null) {
            errors.add("Dashboard cannot be null");
            return errors;
        }

        if (StringUtils.isBlank(dashboard.getName())) {
            errors.add("Dashboard name cannot be blank");
        }

        return errors;
    }

    /**
     * Validates dashboard scopes
     * @param scopes The dashboard scopes to validate
     *               Must contain a site, project, and data type scope
     * @return A list of errors, if any
     */
    protected List<String> validate(final Map<Scope, DashboardScope> scopes) {
        List<String> errors = new ArrayList<>();

        if (scopes == null) {
            errors.add("Dashboard scopes cannot be null");
            return errors;
        } else if (scopes.isEmpty()) {
            errors.add("Dashboard scopes cannot be empty");
            return errors;
        }

        if (!scopes.containsKey(Scope.Site)) {
            errors.add("Dashboard scopes must contain a site scope");
        }

        if (!scopes.containsKey(Scope.Project)) {
            errors.add("Dashboard scopes must contain a project scope");
        }

        if (!scopes.containsKey(Scope.DataType)) {
            errors.add("Dashboard scopes must contain a data type scope");
        }

        return errors;
    }
}
