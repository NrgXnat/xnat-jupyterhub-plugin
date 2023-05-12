package org.nrg.jobtemplates.config;

import org.nrg.jobtemplates.services.ConstraintConfigEntityService;
import org.nrg.jobtemplates.services.impl.DefaultConstraintConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultConstraintConfigServiceTestConfig {

    @Bean
    public DefaultConstraintConfigService defaultConstraintConfigService(final ConstraintConfigEntityService constraintConfigEntityService) {
        return new DefaultConstraintConfigService(
                constraintConfigEntityService
        );
    }

}
