package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DashboardComputeEnvironmentConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DashboardHardwareConfigService;
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
                                                                                  final HardwareConfigEntityService hardwareConfigEntityService,
                                                                                  final DashboardConfigEntityService dashboardConfigEntityService) {
        return new DashboardComputeEnvironmentConfigService(
                computeEnvironmentConfigEntityService,
                hardwareConfigEntityService,
                dashboardConfigEntityService
        );
    }

    @Bean
    public HardwareConfigService defaultHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                                              final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                              final DashboardConfigEntityService dashboardConfigEntityService) {
        return new DashboardHardwareConfigService(
                hardwareConfigEntityService,
                computeEnvironmentConfigEntityService,
                dashboardConfigEntityService
        );
    }

    @Bean
    public DashboardFrameworkService defaultDashboardFrameworkService(final DashboardFrameworkEntityService dashboardFrameworkEntityService) {
        return new DefaultDashboardFrameworkService(dashboardFrameworkEntityService);
    }

}
