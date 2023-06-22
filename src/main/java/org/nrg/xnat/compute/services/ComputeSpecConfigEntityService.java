package org.nrg.xnat.compute.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.compute.entities.ComputeSpecConfigEntity;
import org.nrg.xnat.compute.models.ComputeSpecConfig;

import java.util.List;

public interface ComputeSpecConfigEntityService extends BaseHibernateService<ComputeSpecConfigEntity> {

    void addHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId);
    void removeHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId);
    List<ComputeSpecConfigEntity> findByType(ComputeSpecConfig.ConfigType type);

}
