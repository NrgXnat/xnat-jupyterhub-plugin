package org.nrg.xnat.compute.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.ComputeSpecConfig;

import java.util.List;
import java.util.Optional;

public interface ComputeSpecConfigService {

    boolean exists(Long id);
    Optional<ComputeSpecConfig> retrieve(Long id);
    List<ComputeSpecConfig> getAll();
    List<ComputeSpecConfig> getByType(ComputeSpecConfig.ConfigType type);
    List<ComputeSpecConfig> getAvailable(String user, String project);
    List<ComputeSpecConfig> getAvailable(String user, String project, ComputeSpecConfig.ConfigType type);
    boolean isAvailable(String user, String project, Long id);
    ComputeSpecConfig create(ComputeSpecConfig computeSpecConfig);
    ComputeSpecConfig update(ComputeSpecConfig computeSpecConfig) throws NotFoundException;
    void delete(Long id) throws NotFoundException;

}
