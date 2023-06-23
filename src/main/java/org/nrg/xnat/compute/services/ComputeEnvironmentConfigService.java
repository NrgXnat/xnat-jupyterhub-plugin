package org.nrg.xnat.compute.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;

import java.util.List;
import java.util.Optional;

public interface ComputeEnvironmentConfigService {

    boolean exists(Long id);
    Optional<ComputeEnvironmentConfig> retrieve(Long id);
    List<ComputeEnvironmentConfig> getAll();
    List<ComputeEnvironmentConfig> getByType(ComputeEnvironmentConfig.ConfigType type);
    List<ComputeEnvironmentConfig> getAvailable(String user, String project);
    List<ComputeEnvironmentConfig> getAvailable(String user, String project, ComputeEnvironmentConfig.ConfigType type);
    boolean isAvailable(String user, String project, Long id);
    ComputeEnvironmentConfig create(ComputeEnvironmentConfig computeEnvironmentConfig);
    ComputeEnvironmentConfig update(ComputeEnvironmentConfig computeEnvironmentConfig) throws NotFoundException;
    void delete(Long id) throws NotFoundException;

}
