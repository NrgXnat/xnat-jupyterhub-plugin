package org.nrg.jobtemplates.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.jobtemplates.entities.ComputeSpecConfigEntity;
import org.nrg.jobtemplates.models.ComputeSpecConfig;

import java.util.List;

public interface ComputeSpecConfigEntityService extends BaseHibernateService<ComputeSpecConfigEntity> {

    void addHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId);
    void removeHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId);
    List<ComputeSpecConfigEntity> findByType(ComputeSpecConfig.ConfigType type);

}
