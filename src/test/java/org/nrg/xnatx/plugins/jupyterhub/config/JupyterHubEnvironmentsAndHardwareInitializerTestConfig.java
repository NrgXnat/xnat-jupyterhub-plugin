package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
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
                                                                                         final ComputeEnvironmentConfigService mockComputeEnvironmentConfigService,
                                                                                         final HardwareConfigService mockHardwareConfigService) {
        return new JupyterHubEnvironmentsAndHardwareInitializer(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockComputeEnvironmentConfigService,
                mockHardwareConfigService
        );
    }

}
