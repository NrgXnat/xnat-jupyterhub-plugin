package org.nrg.xnat.compute.config;

import org.hibernate.SessionFactory;
import org.nrg.xnat.compute.repositories.ComputeSpecConfigDao;
import org.nrg.xnat.compute.repositories.ConstraintConfigDao;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.ComputeSpecConfigEntityService;
import org.nrg.xnat.compute.services.ConstraintConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateComputeSpecConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateConstraintConfigEntityService;
import org.nrg.xnat.compute.services.impl.HibernateHardwareConfigEntityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateConfig.class})
public class HibernateEntityServicesConfig {

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
    public ComputeSpecConfigDao computeSpecConfigDao(final SessionFactory sessionFactory,
                                                     final HardwareConfigDao hardwareConfigDao) {
        return new ComputeSpecConfigDao(sessionFactory, hardwareConfigDao);
    }

    @Bean
    public ComputeSpecConfigEntityService computeSpecConfigEntityService(final ComputeSpecConfigDao computeSpecConfigDao,
                                                                             final HardwareConfigDao hardwareConfigDao) {
        return new HibernateComputeSpecConfigEntityService(
                computeSpecConfigDao,
                hardwareConfigDao
        );
    }

    @Bean
    public HardwareConfigEntityService hardwareConfigEntityService(final HardwareConfigDao hardwareConfigDao) {
        HibernateHardwareConfigEntityService service = new HibernateHardwareConfigEntityService();
        service.setDao(hardwareConfigDao);
        return service;
    }

}
