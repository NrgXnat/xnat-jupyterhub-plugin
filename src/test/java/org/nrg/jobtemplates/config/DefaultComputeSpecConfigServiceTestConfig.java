package org.nrg.jobtemplates.config;

import org.nrg.jobtemplates.services.ComputeSpecConfigEntityService;
import org.nrg.jobtemplates.services.HardwareConfigEntityService;
import org.nrg.jobtemplates.services.impl.DefaultComputeSpecConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultComputeSpecConfigServiceTestConfig {

    @Bean
    public DefaultComputeSpecConfigService defaultComputeSpecConfigService(final ComputeSpecConfigEntityService computeSpecConfigEntityService,
                                                                           final HardwareConfigEntityService hardwareConfigEntityService) {
        return new DefaultComputeSpecConfigService(
                computeSpecConfigEntityService,
                hardwareConfigEntityService
        );
    }

}
