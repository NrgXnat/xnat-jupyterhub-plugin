package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultJupyterHubService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JupyterHubPreferencesConfig.class, JupyterHubClientConfig.class})
public class JupyterHubServiceConfig {

    @Bean
    public JupyterHubService JupyterHubService(final JupyterHubPreferences mockJupyterHubPreferences,
                                               final JupyterHubClient mockJupyterHubClient,
                                               final SearchHelperServiceI mockSearchHelperService) {
        return new DefaultJupyterHubService(mockJupyterHubPreferences, mockJupyterHubClient, mockSearchHelperService);
    }

}
