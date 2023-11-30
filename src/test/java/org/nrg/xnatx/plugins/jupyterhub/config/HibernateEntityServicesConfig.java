package org.nrg.xnatx.plugins.jupyterhub.config;

import org.hibernate.SessionFactory;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardFrameworkEntityDao;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
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

}
