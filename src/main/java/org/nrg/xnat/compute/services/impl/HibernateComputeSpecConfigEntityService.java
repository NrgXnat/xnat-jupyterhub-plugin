package org.nrg.xnat.compute.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.compute.entities.ComputeSpecConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.ComputeSpecConfig;
import org.nrg.xnat.compute.repositories.ComputeSpecConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.ComputeSpecConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class HibernateComputeSpecConfigEntityService extends AbstractHibernateEntityService<ComputeSpecConfigEntity, ComputeSpecConfigDao> implements ComputeSpecConfigEntityService {

    private final HardwareConfigDao hardwareConfigDao;

    // For testing
    public HibernateComputeSpecConfigEntityService(final ComputeSpecConfigDao computeSpecConfigDao,
                                                   final HardwareConfigDao hardwareConfigDao) {
        super();
        this.hardwareConfigDao = hardwareConfigDao;
        setDao(computeSpecConfigDao);
    }

    @Autowired
    public HibernateComputeSpecConfigEntityService(HardwareConfigDao hardwareConfigDao) {
        this.hardwareConfigDao = hardwareConfigDao;
    }

    /**
     * Associates a hardware config with a compute spec config
     * @param computeSpecConfigId The compute spec config id
     * @param hardwareConfigId The hardware config id to associate with the compute spec config
     */
    @Override
    @Transactional
    public void addHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId) {
        ComputeSpecConfigEntity computeSpecConfigEntity = getDao().retrieve(computeSpecConfigId);
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigDao.retrieve(hardwareConfigId);
        computeSpecConfigEntity.getHardwareOptions().addHardwareConfig(hardwareConfigEntity);
        getDao().update(computeSpecConfigEntity);
        hardwareConfigDao.update(hardwareConfigEntity);
    }

    /**
     * Removes association between a hardware config and a compute spec config
     * @param computeSpecConfigId The compute spec config id
     * @param hardwareConfigId The hardware config id to remove from the compute spec config
     */
    @Override
    @Transactional
    public void removeHardwareConfigEntity(Long computeSpecConfigId, Long hardwareConfigId) {
        ComputeSpecConfigEntity computeSpecConfigEntity = getDao().retrieve(computeSpecConfigId);
        HardwareConfigEntity hardwareConfigEntity = hardwareConfigDao.retrieve(hardwareConfigId);
        computeSpecConfigEntity.getHardwareOptions().removeHardwareConfig(hardwareConfigEntity);
        getDao().update(computeSpecConfigEntity);
        hardwareConfigDao.update(hardwareConfigEntity);
    }

    /**
     * Returns a list of compute spec configs by type
     * @param type The type of compute spec configs to return
     * @return A list of compute spec configs of the specified type
     */
    @Override
    @Transactional
    public List<ComputeSpecConfigEntity> findByType(ComputeSpecConfig.ConfigType type) {
        return getDao().findByType(type.name());
    }

}
