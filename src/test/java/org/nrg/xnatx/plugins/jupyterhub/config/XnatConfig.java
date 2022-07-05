package org.nrg.xnatx.plugins.jupyterhub.config;

import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XnatConfig {

    @Bean
    public ConfigPaths configPaths() {
        return Mockito.mock(ConfigPaths.class);
    }

    @Bean
    public OrderedProperties orderedProperties() {
        return Mockito.mock(OrderedProperties.class);
    }

    @Bean
    public JupyterHubPreferences mockJupyterHubPreferences() {
        return Mockito.mock(JupyterHubPreferences.class);
    }

    @Bean
    public SearchHelperServiceI mockSearchHelperServiceI() {
        return Mockito.mock(SearchHelperServiceI.class);
    }

}
