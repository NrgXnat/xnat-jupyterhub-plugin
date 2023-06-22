package org.nrg.xnat.compute.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.ConstraintConfig;

import java.util.List;
import java.util.Optional;

public interface ConstraintConfigService {

    Optional<ConstraintConfig> retrieve(Long id);
    List<ConstraintConfig> getAll();
    List<ConstraintConfig> getAvailable(String project);
    ConstraintConfig create(ConstraintConfig config);
    ConstraintConfig update(ConstraintConfig config) throws NotFoundException;
    void delete(Long id);

}