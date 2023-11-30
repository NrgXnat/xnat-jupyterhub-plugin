package org.nrg.xnatx.plugins.jupyterhub.config;

import org.hibernate.SessionFactory;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardFrameworkEntityDao;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.HibernateDashboardFrameworkEntityService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateConfig.class})
public class HibernateDashboardFrameworkEntityServiceTestConfig {

    @Bean
    public HibernateDashboardFrameworkEntityService hibernateDashboardFrameworkEntityService(@Qualifier("dashboardFrameworkEntityDaoImpl") DashboardFrameworkEntityDao dao) {
        return new HibernateDashboardFrameworkEntityService(dao);
    }

    @Bean
    @Qualifier("dashboardFrameworkEntityDaoImpl")
    public DashboardFrameworkEntityDao dashboardFrameworkEntityDao(final SessionFactory sessionFactory) {
        return new DashboardFrameworkEntityDao(sessionFactory);
    }

}
