package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.initialize.JupyterHubPreferenceInitializer;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.SystemHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class JupyterHubPreferenceInitializerTestConfig {

    @Bean
    public JupyterHubPreferenceInitializer JupyterHubPreferenceInitializer(final XFTManagerHelper mockXFTManagerHelper,
                                                                           final XnatAppInfo mockXnatAppInfo,
                                                                           final JupyterHubPreferences mockJupyterHubPreferences,
                                                                           final SiteConfigPreferences mockSiteConfigPreferences,
                                                                           final SystemHelper mockSystemHelper) {
        return new JupyterHubPreferenceInitializer(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockJupyterHubPreferences,
                mockSiteConfigPreferences,
                mockSystemHelper
        );
    }
}
