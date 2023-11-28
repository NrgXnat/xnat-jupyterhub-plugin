package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardEntityDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class DefaultDashboardEntityService extends AbstractHibernateEntityService<DashboardEntity, DashboardEntityDao> implements DashboardEntityService {

}
