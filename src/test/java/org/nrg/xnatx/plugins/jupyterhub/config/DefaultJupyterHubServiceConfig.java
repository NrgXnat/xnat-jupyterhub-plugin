package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.jobtemplates.services.JobTemplateService;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultJupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultJupyterHubServiceConfig {

    @Bean
    public DefaultJupyterHubService defaultJupyterHubService(final JupyterHubClient mockJupyterHubClient,
                                                             final NrgEventServiceI mockNrgEventService,
                                                             final PermissionsHelper mockPermissionsHelper,
                                                             final UserOptionsService mockUserOptionsService,
                                                             final JupyterHubPreferences mockJupyterHubPreferences,
                                                             final UserManagementServiceI mockUserManagementService,
                                                             final JobTemplateService mockJobTemplateService) {
        return new DefaultJupyterHubService(mockJupyterHubClient,
                                            mockNrgEventService,
                                            mockPermissionsHelper,
                                            mockUserOptionsService,
                                            mockJupyterHubPreferences,
                                            mockUserManagementService,
                                            mockJobTemplateService);
    }

}
