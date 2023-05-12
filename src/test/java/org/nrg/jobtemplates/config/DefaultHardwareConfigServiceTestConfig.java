package org.nrg.jobtemplates.config;

import org.nrg.jobtemplates.services.ComputeSpecConfigEntityService;
import org.nrg.jobtemplates.services.HardwareConfigEntityService;
import org.nrg.jobtemplates.services.impl.DefaultHardwareConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultHardwareConfigServiceTestConfig {

    @Bean
    public DefaultHardwareConfigService defaultHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                                                     final ComputeSpecConfigEntityService computeSpecConfigEntityService) {
        return new DefaultHardwareConfigService(
                hardwareConfigEntityService,
                computeSpecConfigEntityService
        );
    }

}
