package org.nrg.xnatx.plugins.jupyterhub.config;

import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserWorkspaceService;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockConfig {

    @Bean
    public ConfigPaths configPaths() {
        return Mockito.mock(ConfigPaths.class);
    }

    @Bean
    public OrderedProperties orderedProperties() {
        return Mockito.mock(OrderedProperties.class);
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }

    @Bean
    public JupyterHubPreferences mockJupyterHubPreferences() {
        return Mockito.mock(JupyterHubPreferences.class);
    }

    @Bean
    public SearchHelperServiceI mockSearchHelperServiceI() {
        return Mockito.mock(SearchHelperServiceI.class);
    }

    @Bean
    public NrgEventServiceI mockNrgEventService() {
        return Mockito.mock(NrgEventServiceI.class);
    }

    @Bean
    public AliasTokenService mockAliasTokenService() {
        return Mockito.mock(AliasTokenService.class);
    }

    @Bean
    public SiteConfigPreferences mockSiteConfigPreferences() {
        return Mockito.mock(SiteConfigPreferences.class);
    }

    @Bean
    public UserOptionsEntityService mockUserOptionsEntityService() {
        return Mockito.mock(UserOptionsEntityService.class);
    }

    @Bean
    public PermissionsServiceI mockPermissionsService() {
        return Mockito.mock(PermissionsServiceI.class);
    }

    @Bean
    public UserOptionsService mockUserOptionsService() {
        return Mockito.mock(UserOptionsService.class);
    }

    @Bean
    public JupyterHubClient mockJupyterHubClient() {
        return Mockito.mock(JupyterHubClient.class);
    }

    @Bean
    public PermissionsHelper mockPermissionsHelper() {
        return Mockito.mock(PermissionsHelper.class);
    }

    @Bean
    public UserWorkspaceService mockUserWorkspaceService() {
        return Mockito.mock(UserWorkspaceService.class);
    }

    @Bean
    public JupyterHubService mockJupyterHubService() {
        return Mockito.mock(JupyterHubService.class);
    }

}
