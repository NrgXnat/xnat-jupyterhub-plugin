package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultUserWorkspaceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultUserWorkspaceServiceConfig {

    @Bean
    public DefaultUserWorkspaceService defaultUserWorkspaceService(final JupyterHubPreferences mockJupyterHubPreferences) {
        return new DefaultUserWorkspaceService(mockJupyterHubPreferences);
    }

}
