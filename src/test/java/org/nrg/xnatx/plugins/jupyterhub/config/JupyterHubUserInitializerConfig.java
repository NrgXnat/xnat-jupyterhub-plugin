package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.initialize.JupyterHubUserInitializer;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class JupyterHubUserInitializerConfig {

    @Bean
    public JupyterHubUserInitializer defaultJupyterHubUserInitializer(final UserManagementServiceI mockUserManagementService,
                                                                      final RoleHolder mockRoleHolder,
                                                                      final XFTManagerHelper mockXFTManagerHelper,
                                                                      final XnatAppInfo mockXnatAppInfo) {
        return new JupyterHubUserInitializer(mockUserManagementService,
                                             mockRoleHolder,
                                             mockXFTManagerHelper,
                                             mockXnatAppInfo);
    }

}
