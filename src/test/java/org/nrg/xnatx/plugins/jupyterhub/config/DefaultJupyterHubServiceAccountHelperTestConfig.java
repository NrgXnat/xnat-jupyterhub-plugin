package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.utils.impl.DefaultJupyterHubServiceAccountHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultJupyterHubServiceAccountHelperTestConfig {

    @Bean
    public DefaultJupyterHubServiceAccountHelper defaultJupyterHubServiceAccountHelper(final UserManagementServiceI mockUserManagementService,
                                                                                       final RoleServiceI mockRoleService) {
        return new DefaultJupyterHubServiceAccountHelper(
                mockUserManagementService,
                mockRoleService
        );
    }

}
