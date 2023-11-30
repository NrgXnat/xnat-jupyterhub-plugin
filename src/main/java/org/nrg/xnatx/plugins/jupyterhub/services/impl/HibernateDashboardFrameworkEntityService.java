package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardFrameworkEntityDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class HibernateDashboardFrameworkEntityService extends AbstractHibernateEntityService<DashboardFrameworkEntity, DashboardFrameworkEntityDao> implements DashboardFrameworkEntityService {

    // For testing
    public HibernateDashboardFrameworkEntityService(final DashboardFrameworkEntityDao dao) {
        super();
        setDao(dao);
    }

    /**
     * Finds a dashboard framework by name.
     * @param name The name of the dashboard framework to find.
     * @return The dashboard framework if found, otherwise empty optional.
     */
    @Override
    public Optional<DashboardFrameworkEntity> findFrameworkByName(String name) {
        return getDao().findFrameworkByName(name);
    }

}
