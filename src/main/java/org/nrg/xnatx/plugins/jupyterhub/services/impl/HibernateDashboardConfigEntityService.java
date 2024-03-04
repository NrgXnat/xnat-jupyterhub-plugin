package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardConfigEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardConfigDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Slf4j
public class HibernateDashboardConfigEntityService extends AbstractHibernateEntityService<DashboardConfigEntity, DashboardConfigDao> implements DashboardConfigEntityService {

    // For testing
    public HibernateDashboardConfigEntityService(final DashboardConfigDao dao) {
        super();
        setDao(dao);
    }

    /**
     * Checks if a compute environment config is in use.
     *
     * @param id The ID of the compute environment config to check.
     * @return True if the compute environment config is used by a dashboard config, otherwise false.
     */
    @Override
    @Transactional
    public boolean isComputeEnvironmentConfigInUse(Long id) {
        return getDao().isComputeEnvironmentConfigInUse(id);
    }

    /**
     * Checks if a hardware config is in use.
     *
     * @param id The ID of the hardware config to check.
     * @return True if the hardware config is used by a dashboard config, otherwise false.
     */
    @Override
    @Transactional
    public boolean isHardwareConfigInUse(Long id) {
        return getDao().isHardwareConfigInUse(id);
    }

}
