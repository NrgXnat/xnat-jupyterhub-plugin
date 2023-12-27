package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of the dashboard framework service. This service is responsible for creating, updating,
 * retrieving, and deleting dashboard frameworks.
 */
@Service
@Slf4j
public class DefaultDashboardFrameworkService implements DashboardFrameworkService {

    private final DashboardFrameworkEntityService dashboardFrameworkEntityService;

    @Autowired
    public DefaultDashboardFrameworkService(final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        super();
        this.dashboardFrameworkEntityService = dashboardFrameworkEntityService;
    }

    /**
     * Create a new dashboard framework
     * @param framework The dashboard framework to create
     * @return The created dashboard framework
     */
    @Override
    public DashboardFramework create(DashboardFramework framework) {
        DashboardFrameworkEntity entity = dashboardFrameworkEntityService.create(DashboardFrameworkEntity.fromPojo(framework));
        return entity.toPojo();
    }

    /**
     * Update an existing dashboard framework
     * @param framework The dashboard framework to update
     * @return The updated dashboard framework
     * @throws NotFoundException When the dashboard framework cannot be found
     */
    @Override
    public DashboardFramework update(DashboardFramework framework) throws NotFoundException {
        Optional<DashboardFrameworkEntity> existing = Optional.ofNullable(dashboardFrameworkEntityService.retrieve(framework.getId()));

        if (existing.isPresent()) {
            existing.get().update(framework);
            dashboardFrameworkEntityService.update(existing.get());
        } else {
            throw new NotFoundException("DashboardFramework", framework.getId());
        }

        return dashboardFrameworkEntityService.retrieve(framework.getId()).toPojo();
    }

    /**
     * Get a dashboard framework by ID
     * @param id The ID of the dashboard framework to get
     * @return The dashboard framework if found, otherwise null
     */
    @Override
    public Optional<DashboardFramework> get(Long id) {
        return Optional.ofNullable(dashboardFrameworkEntityService.retrieve(id))
                       .map(DashboardFrameworkEntity::toPojo);
    }

    /**
     * Get a dashboard framework by name
     * @param name The name of the dashboard framework to get
     * @return The dashboard framework if found, otherwise null
     */
    @Override
    public Optional<DashboardFramework> get(String name) {
        return dashboardFrameworkEntityService.findFrameworkByName(name)
                                              .map(DashboardFrameworkEntity::toPojo);
    }

    /**
     * Get all dashboard frameworks
     * @return A list of all dashboard frameworks
     */
    @Override
    public List<DashboardFramework> getAll() {
        return dashboardFrameworkEntityService.getAll().stream()
                                              .map(DashboardFrameworkEntity::toPojo)
                                              .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Delete a dashboard framework by ID
     * @param id The ID of the dashboard framework to delete
     */
    @Override
    public void delete(Long id) {
        dashboardFrameworkEntityService.delete(id);
    }

    /**
     * Delete a dashboard framework by name
     * @param name The name of the dashboard framework to delete
     */
    @Override
    public void delete(String name) {
        get(name).map(DashboardFramework::getId)
                 .ifPresent(this::delete);
    }

    /**
     * Resolve the command for a dashboard. If the dashboard framework is "custom", the command is taken from the
     * dashboard. Otherwise, the command is taken from the dashboard framework. The variables {repo}, {repobranch},
     * and {mainFilePath} are replaced with the dashboard's git repo URL, git repo branch, and main file path,
     * respectively.
     *
     * @param dashboard The dashboard to resolve the command for
     * @return The resolved command
     */
    @Override
    public String resolveCommand(Dashboard dashboard) {
        String command;

        if (StringUtils.isBlank(dashboard.getFramework()) || dashboard.getFramework().equalsIgnoreCase("custom")) {
            command = dashboard.getCommand();
        } else {
            final DashboardFramework framework = get(dashboard.getFramework()).orElseThrow(() -> new IllegalArgumentException("DashboardFramework " + dashboard.getFramework() + " does not exist"));
            command = framework.getCommandTemplate();
        }

        command = command.replaceAll("\\{repo\\}", dashboard.getGitRepoUrl())
                         .replaceAll("\\{repobranch\\}", dashboard.getGitRepoBranch())
                         .replaceAll("\\{mainFilePath\\}", dashboard.getMainFilePath());

        // remove extra spaces
        command = removeExtraSpaces(command);

        return command;
    }

    /**
     * Replace multiple spaces with a single space
     * @param command The command to remove extra spaces from
     * @return The command with extra spaces removed
     */
    protected String removeExtraSpaces(String command) {
        return command.replaceAll("\\s+", " ");
    }

}
