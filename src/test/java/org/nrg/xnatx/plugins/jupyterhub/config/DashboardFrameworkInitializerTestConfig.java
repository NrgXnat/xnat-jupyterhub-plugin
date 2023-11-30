package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.initialize.DashboardFrameworkInitializer;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DashboardFrameworkInitializerTestConfig {

    @Bean
    public DashboardFrameworkInitializer dashboardFrameworkInitializer(final XFTManagerHelper mockXFTManagerHelper,
                                                                       final XnatAppInfo mockXnatAppInfo,
                                                                       final DashboardFrameworkService mockDashboardFrameworkService) {
        return new DashboardFrameworkInitializer(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockDashboardFrameworkService
        );
    }

}
