package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultDashboardFrameworkService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultDashboardFrameworkServiceTestConfig {

    @Bean
    public DefaultDashboardFrameworkService defaultDashboardFrameworkService(final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        return new DefaultDashboardFrameworkService(
                dashboardFrameworkEntityService
        );
    }

}
