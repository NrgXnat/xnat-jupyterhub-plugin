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
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardJobTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class DefaultDashboardJobTemplateService extends DefaultJobTemplateService implements DashboardJobTemplateService {

    private final DashboardConfigService dashboardConfigService;
    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;

    @Autowired
    public DefaultDashboardJobTemplateService(final ComputeEnvironmentConfigService computeEnvironmentConfigService,
                                              final HardwareConfigService hardwareConfigService,
                                              final ConstraintConfigService constraintConfigService,
                                              final DashboardConfigService dashboardConfigService) {
        super(computeEnvironmentConfigService, hardwareConfigService, constraintConfigService);
        this.dashboardConfigService = dashboardConfigService;
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
    }

    @Override
    public boolean isAvailable(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        // For dashboard, availability is stored in the dashboard config. Override the compute environment and hardware
        // availability checks. Any compute environment and hardware can be used for a dashboard as long as the
        // dashboard config is available to the given execution scope.
        return true;
    }

    @Override
    public JobTemplate resolve(Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        // Should resolve the job template and override the compute environment and hardware availability checks.
        return super.resolve(computeEnvironmentConfigId, hardwareConfigId, executionScope);
    }

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
            return false;
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

    @Override
    public JobTemplate resolve(Long dashboardConfigId, Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope) {
        if (dashboardConfigId == null || computeEnvironmentConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("DashboardConfig, ComputeEnvironmentConfig, and HardwareConfig ids must be specified");
        }

        if (!isAvailable(dashboardConfigId, computeEnvironmentConfigId, hardwareConfigId, executionScope)) {
            throw new IllegalArgumentException("DashboardConfig, ComputeEnvironmentConfig, and HardwareConfig are not available");
        }

        // Use the super resolve method to resolve the job template
        JobTemplate jobTemplate = super.resolve(computeEnvironmentConfigId, hardwareConfigId, executionScope);

        // Override the command in the job template with the command from the dashboard config
        DashboardConfig dashboardConfig = dashboardConfigService.retrieve(dashboardConfigId).orElse(null);

        if (dashboardConfig != null &&
            dashboardConfig.getDashboard() != null &&
            StringUtils.isNotBlank(dashboardConfig.getDashboard().getCommand())) {
            jobTemplate.getComputeEnvironment().setCommand(dashboardConfig.getDashboard().getCommand());
        }

        return jobTemplate;
    }
}
