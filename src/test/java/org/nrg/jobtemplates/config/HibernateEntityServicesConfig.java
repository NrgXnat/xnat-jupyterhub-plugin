package org.nrg.jobtemplates.config;

import org.hibernate.SessionFactory;
import org.nrg.jobtemplates.repositories.ComputeSpecConfigDao;
import org.nrg.jobtemplates.repositories.ConstraintConfigDao;
import org.nrg.jobtemplates.repositories.HardwareConfigDao;
import org.nrg.jobtemplates.services.ComputeSpecConfigEntityService;
import org.nrg.jobtemplates.services.ConstraintConfigEntityService;
import org.nrg.jobtemplates.services.HardwareConfigEntityService;
import org.nrg.jobtemplates.services.impl.HibernateComputeSpecConfigEntityService;
import org.nrg.jobtemplates.services.impl.HibernateConstraintConfigEntityService;
import org.nrg.jobtemplates.services.impl.HibernateHardwareConfigEntityService;
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
