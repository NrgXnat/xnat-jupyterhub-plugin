package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnat.compute.services.JobTemplateService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardJobTemplateService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserWorkspaceService;
import org.nrg.xnatx.plugins.jupyterhub.services.impl.DefaultUserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultUserOptionsServiceConfig {

    @Bean
    public DefaultUserOptionsService defaultUserOptionsService(final JupyterHubPreferences mockJupyterHubPreferences,
                                                               final UserWorkspaceService mockUserWorkspaceService,
                                                               final SearchHelperServiceI mockSearchHelperService,
                                                               final AliasTokenService mockAliasTokenService,
                                                               final SiteConfigPreferences mockSiteConfigPreferences,
                                                               final UserOptionsEntityService mockUserOptionsEntityService,
                                                               final PermissionsHelper mockPermissionsHelper,
                                                               final JobTemplateService mockJobTemplateService,
                                                               final DashboardJobTemplateService mockDashboardJobTemplateService) {
        return new DefaultUserOptionsService(mockJupyterHubPreferences,
                                             mockUserWorkspaceService,
                                             mockSearchHelperService,
                                             mockAliasTokenService,
                                             mockSiteConfigPreferences,
                                             mockUserOptionsEntityService,
                                             mockPermissionsHelper,
                                             mockJobTemplateService,
                                             mockDashboardJobTemplateService);
    }

}
