package org.nrg.xnatx.plugins.jupyterhub.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class DashboardFrameworkEntityDao extends AbstractHibernateDAO<DashboardFrameworkEntity> {

    // For testing
    public DashboardFrameworkEntityDao(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * Finds a dashboard framework by name.
     * @param name The name of the dashboard framework to find.
     * @return The dashboard framework if found, otherwise null.
     */
    public Optional<DashboardFrameworkEntity> findFrameworkByName(final String name) {
        log.debug("Looking for dashboard framework with name {}", name);
        return Optional.ofNullable(this.findByUniqueProperty("name", name));
    }

}
