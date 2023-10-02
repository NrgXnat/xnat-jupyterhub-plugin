package org.nrg.xnatx.plugins.jupyterhub.config;

import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.services.NrgEventServiceI;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.JobTemplateService;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.*;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.tracking.services.EventTrackingDataHibernateService;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.services.*;
import org.nrg.xnatx.plugins.jupyterhub.utils.JupyterHubServiceAccountHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.PermissionsHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.SystemHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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
    public SystemHelper mockSystemHelper() {
        return Mockito.mock(SystemHelper.class);
    }

    @Bean
    public UserWorkspaceService mockUserWorkspaceService() {
        return Mockito.mock(UserWorkspaceService.class);
    }

    @Bean
    public JupyterHubService mockJupyterHubService() {
        return Mockito.mock(JupyterHubService.class);
    }

    @Bean
    public NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }

    @Bean
    @Qualifier("mockRoleService")
    public RoleServiceI mockRoleService() {
        return Mockito.mock(RoleServiceI.class);
    }

    @Bean
    public RoleHolder mockRoleHolder(@Qualifier("mockRoleService") final RoleServiceI mockRoleService,
                                     final NamedParameterJdbcTemplate mockNamedParameterJdbcTemplate) {
        return new RoleHolder(mockRoleService, mockNamedParameterJdbcTemplate);
    }

    @Bean
    public XFTManagerHelper mockXFTManagerHelper() {
        return Mockito.mock(XFTManagerHelper.class);
    }

    @Bean
    public EventTrackingDataHibernateService mockEventTrackingDataHibernateService() {
        return Mockito.mock(EventTrackingDataHibernateService.class);
    }

    @Bean
    public SerializerService mockSerializerService() {
        return Mockito.mock(SerializerService.class);
    }

    @Bean
    public XnatAppInfo mockXnatAppInfo() {
        return Mockito.mock(XnatAppInfo.class);
    }

    @Bean
    public ComputeEnvironmentConfigService mockComputeEnvironmentConfigService() {
        return Mockito.mock(ComputeEnvironmentConfigService.class);
    }

    @Bean
    public HardwareConfigService mockHardwareConfigService() {
        return Mockito.mock(HardwareConfigService.class);
    }

    @Bean
    public JobTemplateService mockJobTemplateService() {
        return Mockito.mock(JobTemplateService.class);
    }

    @Bean
    public JupyterHubServiceAccountHelper mockJupyterHubServiceAccountHelper() {
        return Mockito.mock(JupyterHubServiceAccountHelper.class);
    }
}
