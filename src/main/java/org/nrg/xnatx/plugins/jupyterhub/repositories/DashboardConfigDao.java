package org.nrg.xnatx.plugins.jupyterhub.repositories;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
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

    @Override
    public void initialize(final DashboardConfigEntity entity) {
        if (entity == null) {
            return;
        }

        super.initialize(entity);

        Hibernate.initialize(entity.getDashboard());

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

}
