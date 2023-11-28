package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardConfigEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardConfigDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HibernateDashboardConfigEntityService extends AbstractHibernateEntityService<DashboardConfigEntity, DashboardConfigDao> implements DashboardConfigEntityService {

}
