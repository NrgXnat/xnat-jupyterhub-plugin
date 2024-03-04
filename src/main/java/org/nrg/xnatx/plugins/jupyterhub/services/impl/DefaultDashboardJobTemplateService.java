package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnat.compute.models.JobTemplate;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.impl.DefaultJobTemplateService;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardJobTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This works in conjunction with the JobTemplateService to resolve a job template for a dashboard config. Dashboards
 * availability is handled separately from compute environment and hardware availability checks. Any compute environment
 * and hardware can be used for a dashboard as long as the dashboard config is available to the given execution scope.
 */
@Service
@Slf4j
public class DefaultDashboardJobTemplateService extends DefaultJobTemplateService implements DashboardJobTemplateService {

    private final DashboardConfigService dashboardConfigService;
    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;
    private final DashboardFrameworkService dashboardFrameworkService;

    @Autowired
    public DefaultDashboardJobTemplateService(final ComputeEnvironmentConfigService computeEnvironmentConfigService,
                                              final HardwareConfigService hardwareConfigService,
                                              final ConstraintConfigService constraintConfigService,
                                              final DashboardConfigService dashboardConfigService,
                                              final DashboardFrameworkService dashboardFrameworkService) {
        super(computeEnvironmentConfigService, hardwareConfigService, constraintConfigService);
        this.dashboardConfigService = dashboardConfigService;
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
        this.dashboardFrameworkService = dashboardFrameworkService;
    }

    /**
     * Override the availability check for a compute environment and hardware config. They are always available for a
     * dashboard config.
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId           the hardware config id
     * @param executionScope             the execution scope to verify the compute environment config and hardware are
     * @return True if the compute environment and hardware are available, false otherwise
     */
    @Override
    public boolean isAvailable(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        // For dashboard, availability is stored in the dashboard config. Override the compute environment and hardware
        // availability checks. Any compute environment and hardware can be used for a dashboard as long as the
        // dashboard config is available to the given execution scope.
        return true;
    }

    /**
     * Resolve a job template for a dashboard config. The command is taken from the dashboard config. The variables
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId           the hardware config id
     * @param executionScope             the execution scope to verify the compute environment config and hardware are
     *                                   available
     * @return
     */
    @Override
    public JobTemplate resolve(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        // Should resolve the job template and override the compute environment and hardware availability checks.
        return super.resolve(computeEnvironmentConfigId, hardwareConfigId, executionScope);
    }

    /**
     * Check if a dashboard config is available given the execution scope (site, project, datatype,  ...). The dashboard config
     * @param dashboardConfigId The ID of the dashboard config to check availability for
     * @param computeEnvironmentConfigId The ID of the compute environment config to check availability for
     * @param hardwareConfigId The ID of the hardware config to check availability for
     * @param executionScope The execution scope to check availability for
     * @return True if the dashboard config is available, false otherwise
     */
    @Override
    public boolean isAvailable(Long dashboardConfigId, Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        // Check if the dashboard config is available given the execution scope (site, project, ...)
        boolean isDashboardAvailable = dashboardConfigService.isAvailable(dashboardConfigId, executionScope);

        if (!isDashboardAvailable) {
            return false;
        }

        // Check if the compute environment config and hardware config are available to the dashboard config
        DashboardConfig dashboardConfig = dashboardConfigService.retrieve(dashboardConfigId).orElse(null);

        if (dashboardConfig == null ||
            dashboardConfig.getComputeEnvironmentConfig() == null ||
            dashboardConfig.getComputeEnvironmentConfig().getId() == null ||
            dashboardConfig.getHardwareConfig() == null ||
            dashboardConfig.getHardwareConfig().getId() == null) {
            throw new IllegalArgumentException("DashboardConfig, ComputeEnvironmentConfig, and/or HardwareConfig ids must be specified");
        }

        boolean isComputeEnvironmentConfigAvailable = dashboardConfig.getComputeEnvironmentConfig().getId().equals(computeEnvironmentConfigId);
        boolean isHardwareConfigAvailable = dashboardConfig.getHardwareConfig().getId().equals(hardwareConfigId);

        if (!isComputeEnvironmentConfigAvailable || !isHardwareConfigAvailable) {
            return false;
        }

        // Check if the hardware config is available to the compute environment config
        ComputeEnvironmentConfig computeEnvironmentConfig = computeEnvironmentConfigService
                .retrieve(computeEnvironmentConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeEnvironmentConfig with id " + computeEnvironmentConfigId + " does not exist"));

        if (computeEnvironmentConfig.getHardwareOptions().isAllowAllHardware()) {
            return true;
        }

        return computeEnvironmentConfig.getHardwareOptions()
                                       .getHardwareConfigs().stream()
                                       .map(HardwareConfig::getId)
                                       .anyMatch(id -> id.equals(hardwareConfigId));
    }

    /**
     * Resolve a job template for a dashboard config. The command is taken from the dashboard config. The variables
     * @param dashboardConfigId The ID of the dashboard config to resolve the job template for
     * @param computeEnvironmentConfigId The ID of the compute environment config to resolve the job template for
     * @param hardwareConfigId The ID of the hardware config to resolve the job template for
     * @param executionScope The execution scope to resolve the job template for
     * @return The resolved job template
     */
    @Override
    public JobTemplate resolve(Long dashboardConfigId, Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        if (dashboardConfigId == null || computeEnvironmentConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("DashboardConfig, ComputeEnvironmentConfig, and HardwareConfig ids must be specified");
        }

        if (!isAvailable(dashboardConfigId, computeEnvironmentConfigId, hardwareConfigId, executionScope)) {
            throw new IllegalArgumentException("DashboardConfig, ComputeEnvironmentConfig, and HardwareConfig are not available");
        }

        JobTemplate jobTemplate = resolve(computeEnvironmentConfigId, hardwareConfigId, executionScope);

        // Override the command in the job template with the command from the dashboard config
        DashboardConfig dashboardConfig = dashboardConfigService.retrieve(dashboardConfigId).orElse(null);

        if (dashboardConfig != null &&
            dashboardConfig.getDashboard() != null) {

            Dashboard dashboard = dashboardConfig.getDashboard();
            String command = dashboardFrameworkService.resolveCommand(dashboard);

            if (!StringUtils.isBlank(command)) {
                jobTemplate.getComputeEnvironment().setCommand(command);
            }
        }

        return jobTemplate;
    }
}
