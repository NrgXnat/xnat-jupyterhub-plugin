package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardConfigEntity;

public interface DashboardConfigEntityService extends BaseHibernateService<DashboardConfigEntity> {

    boolean isComputeEnvironmentConfigInUse(Long id);
    boolean isHardwareConfigInUse(Long id);

}
