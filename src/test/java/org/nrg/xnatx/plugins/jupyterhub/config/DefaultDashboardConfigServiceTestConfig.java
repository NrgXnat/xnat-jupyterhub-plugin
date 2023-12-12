package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.impl.DefaultComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.impl.DefaultHardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultDashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultDashboardFrameworkService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultDashboardConfigServiceTestConfig {

    @Bean
    public DefaultDashboardConfigService defaultDashboardConfigService(final DashboardConfigEntityService dashboardConfigEntityService,
                                                                       final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                                       final HardwareConfigEntityService hardwareConfigEntityService,
                                                                       final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        return new DefaultDashboardConfigService(
                dashboardConfigEntityService,
                computeEnvironmentConfigEntityService,
                hardwareConfigEntityService,
                dashboardFrameworkEntityService
        );
    }

    @Bean
    public ComputeEnvironmentConfigService defaultComputeEnvironmentConfigService(final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                                                  final HardwareConfigEntityService hardwareConfigEntityService) {
        return new DefaultComputeEnvironmentConfigService(
                computeEnvironmentConfigEntityService,
                hardwareConfigEntityService
        );
    }

    @Bean
    public HardwareConfigService defaultHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                                              final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService) {
        return new DefaultHardwareConfigService(
                hardwareConfigEntityService,
                computeEnvironmentConfigEntityService
        );
    }

    @Bean
    public DashboardFrameworkService defaultDashboardFrameworkService(final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        return new DefaultDashboardFrameworkService(dashboardFrameworkEntityService);
    }

}
