package org.nrg.xnatx.plugins.jupyterhub.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubDashboardConfigsApiTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardScope;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.nrg.framework.constants.Scope.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JupyterHubDashboardConfigsApiTestConfig.class})
public class JupyterHubDashboardConfigsApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private DashboardConfigService mockDashboardConfigService;
    @Autowired private JupyterHubDashboardConfigsApi jupyterHubDashboardConfigsApi;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private DashboardConfig dashboardConfig1;
    private DashboardConfig dashboardConfig2;

    @Before
    public void setup() {
        // Setup mocks
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");
        when(mockUser.getEmail()).thenReturn("mockUser@mockuser.com");
        when(mockUser.getPassword()).thenReturn("mockUserPassword");
        when(mockUser.getID()).thenReturn(1);
        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(false);
        mockAuthentication = new TestingAuthenticationToken(mockUser, mockUser.getPassword());

        // Setup dashboard config
        Dashboard dashboard1 = Dashboard.builder()
                                        .name("Panel Dashboard")
                                        .description("A dashboard using Panel")
                                        .framework("Panel")
                                        .command(null)
                                        .fileSource("git")
                                        .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                        .gitRepoBranch("main")
                                        .mainFilePath("pane/panel_dashboard.ipynb")
                                        .build();



        DashboardScope dashboardSiteScope1 = DashboardScope.builder()
                                                           .scope(Site)
                                                           .enabled(true)
                                                           .ids(new HashSet<>(Collections.emptyList()))
                                                           .build();

        DashboardScope dashboardProjectScope1 = DashboardScope.builder()
                                                              .scope(Project)
                                                              .enabled(false)
                                                              .ids(new HashSet<>(new ArrayList<>(Arrays.asList("Project1", "Project2", "Project3"))))
                                                              .build();

        DashboardScope dashboardDatatypeScope1 = DashboardScope.builder()
                                                               .scope(DataType)
                                                               .enabled(false)
                                                               .ids(new HashSet<>(Arrays.asList("xnat:mrSessionData", "xnat:petSessionData", "xnat:ctSessionData", "xnat:projectData")))
                                                               .build();


        Map<Scope, DashboardScope> dashboardScopes1 = new HashMap<>();
        dashboardScopes1.put(Site, dashboardSiteScope1);
        dashboardScopes1.put(Project, dashboardProjectScope1);
        dashboardScopes1.put(DataType, dashboardDatatypeScope1);

        dashboardConfig1 = DashboardConfig.builder()
                                         .dashboard(dashboard1)
                                         .scopes(dashboardScopes1)
                                         .computeEnvironmentConfig(ComputeEnvironmentConfig.builder().id(1L).build())
                                         .hardwareConfig(HardwareConfig.builder().id(1L).build())
                                         .build();

        // Setup second dashboard config
        Dashboard dashboard2 = Dashboard.builder()
                                        .name("Voila Dashboard")
                                        .description("A dashboard using Voila")
                                        .framework("Voila")
                                        .command(null)
                                        .fileSource("git")
                                        .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                        .gitRepoBranch("main")
                                        .mainFilePath("voila/voila_dashboard.ipynb")
                                        .build();

        DashboardScope dashboardSiteScope2 = DashboardScope.builder()
                                                           .scope(Site)
                                                           .enabled(true)
                                                           .ids(new HashSet<>(Collections.emptyList()))
                                                           .build();

        DashboardScope dashboardProjectScope2 = DashboardScope.builder()
                                                              .scope(Project)
                                                              .enabled(false)
                                                              .ids(new HashSet<>(new ArrayList<>(Collections.singletonList("Project1"))))
                                                              .build();

        DashboardScope dashboardDatatypeScope2 = DashboardScope.builder()
                                                               .scope(DataType)
                                                               .enabled(false)
                                                               .ids(new HashSet<>(Collections.singletonList("xnat:projectData")))
                                                               .build();

        Map<Scope, DashboardScope> dashboardScopes2 = new HashMap<>();
        dashboardScopes2.put(Site, dashboardSiteScope2);
        dashboardScopes2.put(Project, dashboardProjectScope2);
        dashboardScopes2.put(DataType, dashboardDatatypeScope2);

        dashboardConfig2 = DashboardConfig.builder()
                                          .dashboard(dashboard2)
                                          .scopes(dashboardScopes2)
                                          .computeEnvironmentConfig(ComputeEnvironmentConfig.builder().id(2L).build())
                                          .hardwareConfig(HardwareConfig.builder().id(2L).build())
                                          .build();
    }

    @After
    public void tearDown() {
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockDashboardConfigService,
                mockUser
        );
    }

    @Test
    public void test_wiring() {
        assertNotNull(wac);
        assertNotNull(mapper);
        assertNotNull(mockRoleService);
        assertNotNull(mockUserManagementService);
        assertNotNull(mockDashboardConfigService);
        assertNotNull(jupyterHubDashboardConfigsApi);
    }

    @Test
    public void test_getDashboardConfigs() throws Exception {
        // Setup mocks
        when(mockDashboardConfigService.getAll()).thenReturn(Arrays.asList(dashboardConfig1, dashboardConfig2));

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/jupyterhub/dashboards/configs")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext());


        final String response = mockMvc.perform(request)
                                       .andExpect(status().isOk())
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        final List<DashboardConfig> dashboardConfigs = Arrays.asList(mapper.readValue(response, DashboardConfig[].class));
        assertThat(dashboardConfigs.size(), is(2));
        assertThat(dashboardConfigs, hasItems(dashboardConfig1, dashboardConfig2));
        verify(mockDashboardConfigService).getAll();
    }

    @Test
    public void test_getDashboardConfig() throws Exception {
        // Setup mocks
        when(mockDashboardConfigService.retrieve(1L)).thenReturn(Optional.of(dashboardConfig1));

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/jupyterhub/dashboards/configs/1")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext());

        final String response = mockMvc.perform(request)
                                       .andExpect(status().isOk())
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        final DashboardConfig dashboardConfig = mapper.readValue(response, DashboardConfig.class);
        assertThat(dashboardConfig, is(dashboardConfig1));
        verify(mockDashboardConfigService).retrieve(1L);
    }

    @Test
    public void test_getDashboardConfig_notFound() throws Exception {
        // Setup mocks
        when(mockDashboardConfigService.retrieve(1L)).thenReturn(Optional.empty());

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/jupyterhub/dashboards/configs/1")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext());

        mockMvc.perform(request)
               .andExpect(status().isNotFound()) // 404 Not Found
               .andReturn()
               .getResponse()
               .getContentAsString();

        verify(mockDashboardConfigService).retrieve(1L);
    }

    @Test
    public void test_createDashboardConfig() throws Exception {
        // Setup mocks
        when(mockDashboardConfigService.create(dashboardConfig1)).thenReturn(dashboardConfig1);

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/jupyterhub/dashboards/configs")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext())
                                                                            .content(mapper.writeValueAsString(dashboardConfig1))
                                                                            .contentType("application/json");

        final String response = mockMvc.perform(request)
                                       .andExpect(status().isCreated()) // 201 Created
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        final DashboardConfig dashboardConfig = mapper.readValue(response, DashboardConfig.class);
        assertThat(dashboardConfig, is(dashboardConfig1));

        verify(mockDashboardConfigService).create(dashboardConfig1);
    }

    @Test
    public void test_updateDashboardConfig() throws Exception {
        // Setup mocks
        dashboardConfig1.setId(1L);
        when(mockDashboardConfigService.update(dashboardConfig1)).thenReturn(dashboardConfig1);

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/jupyterhub/dashboards/configs/1")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext())
                                                                            .content(mapper.writeValueAsString(dashboardConfig1))
                                                                            .contentType("application/json");

        final String response = mockMvc.perform(request)
                                       .andExpect(status().isOk()) // 200 OK
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        final DashboardConfig dashboardConfig = mapper.readValue(response, DashboardConfig.class);
        assertThat(dashboardConfig, is(dashboardConfig1));
        verify(mockDashboardConfigService).update(dashboardConfig1);
    }

    @Test
    public void test_updateDashboardConfig_notFound() throws Exception {
        // Setup mocks
        dashboardConfig1.setId(1L);
        when(mockDashboardConfigService.update(dashboardConfig1)).thenThrow(new NotFoundException("Dashboard config not found."));

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/jupyterhub/dashboards/configs/1")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext())
                                                                            .content(mapper.writeValueAsString(dashboardConfig1))
                                                                            .contentType("application/json");

        mockMvc.perform(request)
               .andExpect(status().isNotFound()) // 404 Not Found
               .andReturn()
               .getResponse()
               .getContentAsString();
    }

    @Test
    public void test_deleteDashboardConfig() throws Exception {
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/jupyterhub/dashboards/configs/1")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext());

        mockMvc.perform(request)
               .andExpect(status().isNoContent()) // 204 No Content
               .andReturn()
               .getResponse()
               .getContentAsString();

        // Verify
        verify(mockDashboardConfigService).delete(1L);
    }

    @Test
    public void test_getAvailableDashboardConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/dashboards/configs/available")
                .param("site", "XNAT")
                .param("user", mockUser.getLogin())
                .param("prj", "projectId")
                .param("datatype", "xnat:mrSessionData")
                .param("type", "JUPYTERHUB")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify conversion of parameters to scope map
        Map<Scope, String> expectedScopeMap = new HashMap<>();
        expectedScopeMap.put(Site, "XNAT");
        expectedScopeMap.put(User, mockUser.getLogin());
        expectedScopeMap.put(Project, "projectId");
        expectedScopeMap.put(DataType, "xnat:mrSessionData");

        verify(mockDashboardConfigService).getAvailable(eq(expectedScopeMap));
    }

    @Test
    public void test_enableDashboardConfigAtSite() throws Exception {
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/dashboards/configs/1/scope/site")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify
        verify(mockDashboardConfigService).enableForSite(1L);
    }

    @Test
    public void test_disableDashboardConfigAtSite() throws Exception {
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/dashboards/configs/1/scope/site")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify
        verify(mockDashboardConfigService).disableForSite(1L);
    }

    @Test
    public void test_enableDashboardConfigAtProject() throws Exception {
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/dashboards/configs/1/scope/project/projectId")
                .param("projectId", "projectId")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify
        verify(mockDashboardConfigService).enableForProject(1L, "projectId");
    }

    @Test
    public void test_disableDashboardConfigAtProject() throws Exception {
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/dashboards/configs/1/scope/project/projectId")
                .param("projectId", "projectId")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify
        verify(mockDashboardConfigService).disableForProject(1L, "projectId");
    }

}