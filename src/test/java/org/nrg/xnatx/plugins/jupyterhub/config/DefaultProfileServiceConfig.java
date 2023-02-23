package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnatx.plugins.jupyterhub.services.ProfileEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultProfileServiceConfig {

    @Bean
    public DefaultProfileService defaultProfileService(final ProfileEntityService mockProfileEntityService) {
        return new DefaultProfileService(mockProfileEntityService);
    }

}
