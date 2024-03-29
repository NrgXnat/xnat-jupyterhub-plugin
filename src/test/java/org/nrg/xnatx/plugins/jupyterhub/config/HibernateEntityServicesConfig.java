package org.nrg.xnatx.plugins.jupyterhub.config;

import org.hibernate.SessionFactory;
import org.nrg.xnat.compute.repositories.ComputeEnvironmentConfigDao;
import org.nrg.xnat.compute.repositories.ConstraintConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.ConstraintConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateConstraintConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateHardwareConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardConfigDao;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardFrameworkEntityDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.HibernateDashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.HibernateDashboardFrameworkEntityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateConfig.class})
public class HibernateEntityServicesConfig {

    @Bean
    public DashboardFrameworkEntityDao dashboardFrameworkEntityDao(final SessionFactory sessionFactory) {
        return new DashboardFrameworkEntityDao(sessionFactory);
    }

    @Bean
    public DashboardFrameworkEntityService dashboardFrameworkEntityService(final DashboardFrameworkEntityDao dashboardFrameworkEntityDao) {
        return new HibernateDashboardFrameworkEntityService(dashboardFrameworkEntityDao);
    }

    @Bean
    public ConstraintConfigDao constraintConfigDao(final SessionFactory sessionFactory) {
        return new ConstraintConfigDao(sessionFactory);
    }

    @Bean
    public ConstraintConfigEntityService constraintConfigEntityService(final ConstraintConfigDao constraintConfigDao) {
        HibernateConstraintConfigEntityService service = new HibernateConstraintConfigEntityService();
        service.setDao(constraintConfigDao);
        return service;
    }

    @Bean
    public HardwareConfigDao hardwareConfigDao(final SessionFactory sessionFactory) {
        return new HardwareConfigDao(sessionFactory);
    }

    @Bean
    public ComputeEnvironmentConfigDao computeEnvironmentConfigDao(final SessionFactory sessionFactory,
                                                                   final HardwareConfigDao hardwareConfigDao) {
        return new ComputeEnvironmentConfigDao(sessionFactory, hardwareConfigDao);
    }

    @Bean
    public ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService(final ComputeEnvironmentConfigDao computeEnvironmentConfigDao,
                                                                                       final HardwareConfigDao hardwareConfigDao) {
        return new HibernateComputeEnvironmentConfigEntityService(
                computeEnvironmentConfigDao,
                hardwareConfigDao
        );
    }

    @Bean
    public HardwareConfigEntityService hardwareConfigEntityService(final HardwareConfigDao hardwareConfigDao) {
        HibernateHardwareConfigEntityService service = new HibernateHardwareConfigEntityService();
        service.setDao(hardwareConfigDao);
        return service;
    }

    @Bean
    public DashboardConfigDao dashboardConfigDao(final SessionFactory sessionFactory,
                                                 final ComputeEnvironmentConfigDao computeEnvironmentConfigDao,
                                                 final HardwareConfigDao hardwareConfigDao) {
        return new DashboardConfigDao(
            sessionFactory,
            computeEnvironmentConfigDao,
            hardwareConfigDao
        );
    }

    @Bean
    public DashboardConfigEntityService dashboardConfigEntityService(final DashboardConfigDao dashboardConfigDao) {
        return new HibernateDashboardConfigEntityService(
            dashboardConfigDao
        );
    }

}
