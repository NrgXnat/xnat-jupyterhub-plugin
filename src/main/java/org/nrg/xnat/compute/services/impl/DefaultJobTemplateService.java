package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.JobTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultJobTemplateService implements JobTemplateService {

    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;
    private final HardwareConfigService hardwareConfigService;
    private final ConstraintConfigService constraintConfigService;

    @Autowired
    public DefaultJobTemplateService(final ComputeEnvironmentConfigService computeEnvironmentConfigService,
                                     final HardwareConfigService hardwareConfigService,
                                     final ConstraintConfigService constraintConfigService) {
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
        this.hardwareConfigService = hardwareConfigService;
        this.constraintConfigService = constraintConfigService;
    }

    /**
     * Returns true if the specified compute environment config and hardware config are available for the specified user and
     * project and the hardware config is allowed by the compute environment config.
     * @param user the user
     * @param project the project
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId  the hardware config id
     * @return true if the specified compute environment config and hardware config are available for the specified user and
     * project and the hardware config is allowed by the compute environment config
     */
    @Override
    public boolean isAvailable(String user, String project, Long computeEnvironmentConfigId, Long hardwareConfigId) {
        if (user == null || project == null || computeEnvironmentConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        boolean isComputeEnvironmentConfigAvailable = computeEnvironmentConfigService.isAvailable(user, project, computeEnvironmentConfigId);
        boolean isHardwareConfigAvailable = hardwareConfigService.isAvailable(user, project, hardwareConfigId);

        if (!isComputeEnvironmentConfigAvailable || !isHardwareConfigAvailable) {
            return false;
        }

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
     * Returns a job template for the specified user, project, compute environment config, and hardware config.
     * @param user the user
     * @param project the project
     * @param computeEnvironmentConfigId the compute environment config id
     * @param hardwareConfigId the hardware config id
     * @return a job template complete with compute environment and hardware
     */
    @Override
    public JobTemplate resolve(String user, String project, Long computeEnvironmentConfigId, Long hardwareConfigId) {
        if (user == null || project == null || computeEnvironmentConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        if (!isAvailable(user, project, computeEnvironmentConfigId, hardwareConfigId)) {
            throw new IllegalArgumentException("JobTemplate with user " + user + ", project " + project + ", computeEnvironmentConfigId " + computeEnvironmentConfigId + ", and hardwareConfigId " + hardwareConfigId + " is not available");
        }

        ComputeEnvironmentConfig computeEnvironmentConfig = computeEnvironmentConfigService
                .retrieve(computeEnvironmentConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeEnvironmentConfig with id " + computeEnvironmentConfigId + " does not exist"));

        HardwareConfig hardwareConfig = hardwareConfigService
                .retrieve(hardwareConfigId)
                .orElseThrow(() -> new IllegalArgumentException("HardwareConfig with id " + hardwareConfigId + " does not exist"));

        List<Constraint> constraints = constraintConfigService.getAvailable(project).stream()
                .map(ConstraintConfig::getConstraint)
                .collect(Collectors.toList());

        JobTemplate jobTemplate = JobTemplate.builder()
                .computeEnvironment(computeEnvironmentConfig.getComputeEnvironment())
                .hardware(hardwareConfig.getHardware())
                .constraints(constraints)
                .build();

        return jobTemplate;
    }
}
