package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.jobtemplates.services.HardwareConfigService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.initialize.JupyterHubEnvironmentsAndHardwareInitializer;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class JupyterHubEnvironmentsAndHardwareInitializerTestConfig {

    @Bean
    public JupyterHubEnvironmentsAndHardwareInitializer JupyterHubJobTemplateInitializer(final XFTManagerHelper mockXFTManagerHelper,
                                                                                         final XnatAppInfo mockXnatAppInfo,
                                                                                         final ComputeSpecConfigService mockComputeSpecConfigService,
                                                                                         final HardwareConfigService mockHardwareConfigService) {
        return new JupyterHubEnvironmentsAndHardwareInitializer(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockComputeSpecConfigService,
                mockHardwareConfigService
        );
    }

}
