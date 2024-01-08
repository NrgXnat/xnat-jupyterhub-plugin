package org.nrg.xnatx.plugins.jupyterhub.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.compute.repositories.ComputeEnvironmentConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardConfigEntity;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardScopeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class DashboardConfigDao extends AbstractHibernateDAO<DashboardConfigEntity> {

    private final ComputeEnvironmentConfigDao computeEnvironmentConfigDao;
    private final HardwareConfigDao hardwareConfigDao;

    // For testing
    public DashboardConfigDao(final SessionFactory sessionFactory,
                              final ComputeEnvironmentConfigDao computeEnvironmentConfigDao,
                              final HardwareConfigDao hardwareConfigDao) {
        super(sessionFactory);
        this.computeEnvironmentConfigDao = computeEnvironmentConfigDao;
        this.hardwareConfigDao = hardwareConfigDao;
    }

    @Autowired
    public DashboardConfigDao(final ComputeEnvironmentConfigDao computeEnvironmentConfigDao,
                              final HardwareConfigDao hardwareConfigDao) {
        this.computeEnvironmentConfigDao = computeEnvironmentConfigDao;
        this.hardwareConfigDao = hardwareConfigDao;
    }

    /**
     * Initializes the entity, loading all collections and proxies.
     * @param entity The entity to initialize.
     */
    @Override
    public void initialize(final DashboardConfigEntity entity) {
        if (entity == null) {
            return;
        }

        super.initialize(entity);

        Hibernate.initialize(entity.getDashboard());
        if (entity.getDashboard() != null) {
            Hibernate.initialize(entity.getDashboard().getDashboardFramework());
        }

        Hibernate.initialize(entity.getScopes());
        if (entity.getScopes() != null) {
            entity.getScopes().forEach((scope, dashboardScopeEntity) -> {
                initialize(dashboardScopeEntity);
            });
        }

        if (entity.getComputeEnvironmentConfig() != null) {
            computeEnvironmentConfigDao.initialize(entity.getComputeEnvironmentConfig());
        }

        if (entity.getHardwareConfig() != null) {
            hardwareConfigDao.initialize(entity.getHardwareConfig());
        }
    }

    /**
     * Initializes the scope entity, loading all collections and proxies.
     * @param entity The scope entity to initialize.
     */
    public void initialize(DashboardScopeEntity entity) {
        if (entity == null) {
            return;
        }

        Hibernate.initialize(entity);
        Hibernate.initialize(entity.getIds());
    }

    /**
     * Checks if the given compute environment config is in use by a dashboard config.
     * @param id The ID of the compute environment config to check.
     * @return True if the compute environment config is used by a dashboard config, otherwise false.
     */
    public boolean isComputeEnvironmentConfigInUse(final long id) {
        Criteria criteria = getSession().createCriteria(DashboardConfigEntity.class);
        criteria.add(Restrictions.eq("computeEnvironmentConfig.id", id));
        criteria.setMaxResults(1);
        return criteria.uniqueResult() != null;
    }

    /**
     * Checks if the given hardware config is in use by a dashboard config.
     * @param id The ID of the hardware config to check.
     * @return True if the hardware config is used by a dashboard config, otherwise false.
     */
    public boolean isHardwareConfigInUse(final long id) {
        Criteria criteria = getSession().createCriteria(DashboardConfigEntity.class);
        criteria.add(Restrictions.eq("hardwareConfig.id", id));
        criteria.setMaxResults(1);
        return criteria.uniqueResult() != null;
    }

}
