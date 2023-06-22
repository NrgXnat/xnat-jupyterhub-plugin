package org.nrg.xnat.compute.services;

import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.models.HardwareConfig;

import java.util.List;
import java.util.Optional;

public interface HardwareConfigService {

    boolean exists(Long id);
    Optional<HardwareConfig> retrieve(Long id);
    List<HardwareConfig> retrieveAll();
    HardwareConfig create(HardwareConfig hardwareConfig);
    HardwareConfig update(HardwareConfig hardwareConfig) throws NotFoundException;
    void delete(Long id) throws NotFoundException;
    boolean isAvailable(String user, String project, Long id);

}
