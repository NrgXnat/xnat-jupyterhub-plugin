package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xnatx.plugins.jupyterhub.authorization.JupyterUserAuthorization;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class JupyterAuthorizationConfig {

    @Bean
    public JupyterUserAuthorization defaultJupyterAuthorization(final RoleHolder mockRoleHolder,
                                                                final JupyterHubPreferences mockJupyterHubPreferences) {
        return new JupyterUserAuthorization(mockRoleHolder, mockJupyterHubPreferences);
    }

}
