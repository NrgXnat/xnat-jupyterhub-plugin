package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.initialize.MigratePathTranslationPreference;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class MigratePathTranslationPreferenceTestConfig {

    @Bean
    public MigratePathTranslationPreference defaultMigratePathTranslationPreferenceTest(final XFTManagerHelper mockXFTManagerHelper,
                                                                                        final XnatAppInfo mockXnatAppInfo,
                                                                                        final NrgPreferenceService nrgPreferenceService,
                                                                                        final JupyterHubPreferences jupyterHubPreferences) {
        return new MigratePathTranslationPreference(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                nrgPreferenceService,
                jupyterHubPreferences
        );
    }

}
