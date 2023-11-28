package org.nrg.xnatx.plugins.jupyterhub.repositories;

import org.hibernate.Hibernate;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardEntity;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardEntityDao extends AbstractHibernateDAO<DashboardEntity> {

    @Override
    public void initialize(final DashboardEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
    }

}
