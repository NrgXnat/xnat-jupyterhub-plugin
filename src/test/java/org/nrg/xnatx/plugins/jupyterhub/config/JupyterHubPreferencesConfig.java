package org.nrg.xnatx.plugins.jupyterhub.config;

import org.mockito.Mockito;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({XnatConfig.class})
public class JupyterHubPreferencesConfig {

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

}
