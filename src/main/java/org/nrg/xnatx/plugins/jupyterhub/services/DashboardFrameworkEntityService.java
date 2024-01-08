package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;

import java.util.Optional;

public interface DashboardFrameworkEntityService extends BaseHibernateService<DashboardFrameworkEntity> {

    Optional<DashboardFrameworkEntity> findFrameworkByName(String name);

}
