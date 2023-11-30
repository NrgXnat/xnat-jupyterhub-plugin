package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultDashboardJobTemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultDashboardJobTemplateServiceTestConfig {

    @Bean
    public DefaultDashboardJobTemplateService defaultDashboardJobTemplateService(final ComputeEnvironmentConfigService mockComputeEnvironmentConfigService,
                                                                                 final HardwareConfigService mockHardwareConfigService,
                                                                                 final ConstraintConfigService mockConstraintConfigService,
                                                                                 final DashboardConfigService mockDashboardConfigService,
                                                                                 final DashboardFrameworkService mockDashboardFrameworkService) {
        return new DefaultDashboardJobTemplateService(
                mockComputeEnvironmentConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService,
                mockDashboardConfigService,
                mockDashboardFrameworkService
        );
    }

}
