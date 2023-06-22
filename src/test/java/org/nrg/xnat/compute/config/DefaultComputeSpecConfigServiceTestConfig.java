package org.nrg.xnat.compute.config;

import org.nrg.xnat.compute.services.ComputeSpecConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultComputeSpecConfigService;
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
