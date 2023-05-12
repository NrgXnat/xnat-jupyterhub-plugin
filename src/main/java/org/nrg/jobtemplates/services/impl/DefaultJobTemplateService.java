package org.nrg.jobtemplates.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.jobtemplates.models.*;
import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.jobtemplates.services.ConstraintConfigService;
import org.nrg.jobtemplates.services.HardwareConfigService;
import org.nrg.jobtemplates.services.JobTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultJobTemplateService implements JobTemplateService {

    private final ComputeSpecConfigService computeSpecConfigService;
    private final HardwareConfigService hardwareConfigService;
    private final ConstraintConfigService constraintConfigService;

    @Autowired
    public DefaultJobTemplateService(final ComputeSpecConfigService computeSpecConfigService,
                                     final HardwareConfigService hardwareConfigService,
                                     final ConstraintConfigService constraintConfigService) {
        this.computeSpecConfigService = computeSpecConfigService;
        this.hardwareConfigService = hardwareConfigService;
        this.constraintConfigService = constraintConfigService;
    }

    /**
     * Returns true if the specified compute spec config and hardware config are available for the specified user and
     * project and the hardware config is allowed by the compute spec config.
     * @param user the user
     * @param project the project
     * @param computeSpecConfigId the compute spec config id
     * @param hardwareConfigId  the hardware config id
     * @return true if the specified compute spec config and hardware config are available for the specified user and
     * project and the hardware config is allowed by the compute spec config
     */
    @Override
    public boolean isAvailable(String user, String project, Long computeSpecConfigId, Long hardwareConfigId) {
        if (user == null || project == null || computeSpecConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        boolean isComputeSpecConfigAvailable = computeSpecConfigService.isAvailable(user, project, computeSpecConfigId);
        boolean isHardwareConfigAvailable = hardwareConfigService.isAvailable(user, project, hardwareConfigId);

        if (!isComputeSpecConfigAvailable || !isHardwareConfigAvailable) {
            return false;
        }

        ComputeSpecConfig computeSpecConfig = computeSpecConfigService
                .retrieve(computeSpecConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeSpecConfig with id " + computeSpecConfigId + " does not exist"));

        if (computeSpecConfig.getHardwareOptions().isAllowAllHardware()) {
            return true;
        }

        return computeSpecConfig.getHardwareOptions()
                .getHardwareConfigs().stream()
                .map(HardwareConfig::getId)
                .anyMatch(id -> id.equals(hardwareConfigId));
    }

    /**
     * Returns a job template for the specified user, project, compute spec config, and hardware config.
     * @param user the user
     * @param project the project
     * @param computeSpecConfigId the compute spec config id
     * @param hardwareConfigId the hardware config id
     * @return a job template complete with compute spec and hardware
     */
    @Override
    public JobTemplate resolve(String user, String project, Long computeSpecConfigId, Long hardwareConfigId) {
        if (user == null || project == null || computeSpecConfigId == null || hardwareConfigId == null) {
            throw new IllegalArgumentException("One or more parameters is null");
        }

        if (!isAvailable(user, project, computeSpecConfigId, hardwareConfigId)) {
            throw new IllegalArgumentException("JobTemplate with user " + user + ", project " + project + ", computeSpecConfigId " + computeSpecConfigId + ", and hardwareConfigId " + hardwareConfigId + " is not available");
        }

        ComputeSpecConfig computeSpecConfig = computeSpecConfigService
                .retrieve(computeSpecConfigId)
                .orElseThrow(() -> new IllegalArgumentException("ComputeSpecConfig with id " + computeSpecConfigId + " does not exist"));

        HardwareConfig hardwareConfig = hardwareConfigService
                .retrieve(hardwareConfigId)
                .orElseThrow(() -> new IllegalArgumentException("HardwareConfig with id " + hardwareConfigId + " does not exist"));

        List<Constraint> constraints = constraintConfigService.getAvailable(project).stream()
                .map(ConstraintConfig::getConstraint)
                .collect(Collectors.toList());

        JobTemplate jobTemplate = JobTemplate.builder()
                .computeSpec(computeSpecConfig.getComputeSpec())
                .hardware(hardwareConfig.getHardware())
                .constraints(constraints)
                .build();

        return jobTemplate;
    }
}
