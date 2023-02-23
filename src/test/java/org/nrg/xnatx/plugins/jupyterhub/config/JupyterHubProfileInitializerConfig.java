package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnatx.plugins.jupyterhub.initialize.JupyterHubProfileInitializer;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class JupyterHubProfileInitializerConfig {

    @Bean
    public JupyterHubProfileInitializer defaultJupyterHubProfileInitializer(final ProfileService mockProfileService,
                                                                            final XFTManagerHelper mockXFTManagerHelper) {
        return new JupyterHubProfileInitializer(mockProfileService, mockXFTManagerHelper);
    }

}
