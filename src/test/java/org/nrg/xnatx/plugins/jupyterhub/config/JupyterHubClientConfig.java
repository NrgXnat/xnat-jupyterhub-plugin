package org.nrg.xnatx.plugins.jupyterhub.config;

import org.mockito.Mockito;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JupyterHubClientConfig {

    @Bean
    public JupyterHubClient mockJupyterHubClient() {
        return Mockito.mock(JupyterHubClient.class);
    }

}
