package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xnatx.plugins.jupyterhub.utils.impl.DefaultPermissionsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultPermissionsHelperConfig {

    @Bean
    public DefaultPermissionsHelper defaultPermissionsHelper(final SearchHelperServiceI mockSearchHelperService,
                                                             final PermissionsServiceI mockPermissionsService) {
        return new DefaultPermissionsHelper(mockSearchHelperService,
                                            mockPermissionsService);
    }

}
