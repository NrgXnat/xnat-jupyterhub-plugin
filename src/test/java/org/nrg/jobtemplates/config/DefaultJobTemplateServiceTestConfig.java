package org.nrg.jobtemplates.config;

import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.jobtemplates.services.ConstraintConfigService;
import org.nrg.jobtemplates.services.HardwareConfigService;
import org.nrg.jobtemplates.services.impl.DefaultJobTemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultJobTemplateServiceTestConfig {

    @Bean
    public DefaultJobTemplateService defaultJobTemplateService(final ComputeSpecConfigService mockComputeSpecConfigService,
                                                               final HardwareConfigService mockHardwareConfigService,
                                                               final ConstraintConfigService mockConstraintConfigService) {
        return new DefaultJobTemplateService(
                mockComputeSpecConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService
        );
    }

}
