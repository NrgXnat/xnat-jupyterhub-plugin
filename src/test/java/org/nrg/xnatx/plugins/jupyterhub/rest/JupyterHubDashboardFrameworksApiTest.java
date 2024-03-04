package org.nrg.xnatx.plugins.jupyterhub.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubDashboardFrameworksApiTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JupyterHubDashboardFrameworksApiTestConfig.class})
public class JupyterHubDashboardFrameworksApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private DashboardFrameworkService mockDashboardFrameworkService;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private DashboardFramework panel;
    private DashboardFramework streamlit;
    private List<DashboardFramework> frameworks;

    @Before
    public void setup() {
        // Setup dashboards
        panel = DashboardFramework.builder()
                                  .name("Panel")
                                  .commandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug")
                                  .build();
        streamlit = DashboardFramework.builder()
                                      .name("Streamlit")
                                      .commandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug")
                                      .build();

        frameworks = Arrays.asList(panel, streamlit);

        // Setup mocks
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");
        when(mockUser.getEmail()).thenReturn("mockUser@mockuser.com");
        when(mockUser.getPassword()).thenReturn("mockUserPassword");
        when(mockUser.getID()).thenReturn(1);
        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(false);
        mockAuthentication = new TestingAuthenticationToken(mockUser, mockUser.getPassword());
    }

    @After
    public void after() throws Exception {
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockDashboardFrameworkService,
                mockUser
        );
    }

    @Test
    public void test_wiring() {
        assertNotNull(wac);
        assertNotNull(mapper);
        assertNotNull(mockRoleService);
        assertNotNull(mockUserManagementService);
        assertNotNull(mockDashboardFrameworkService);
    }

    @Test
    public void test_getAll() throws Exception {
        // Setup
        when(mockDashboardFrameworkService.getAll()).thenReturn(frameworks);

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/jupyterhub/dashboards/frameworks")
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
        assertEquals(mapper.writeValueAsString(frameworks), response);
        verify(mockDashboardFrameworkService).getAll();
    }

    @Test
    public void test_get() throws Exception {
        // Setup
        when(mockDashboardFrameworkService.get(panel.getName())).thenReturn(java.util.Optional.of(panel));

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/jupyterhub/dashboards/frameworks/" + panel.getName())
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
        assertEquals(mapper.writeValueAsString(panel), response);
        verify(mockDashboardFrameworkService).get(panel.getName());
    }

    @Test
    public void test_create() throws Exception {
        // Setup
        when(mockDashboardFrameworkService.create(panel)).thenReturn(panel);

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/jupyterhub/dashboards/frameworks")
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext())
                                                                            .content(mapper.writeValueAsString(panel))
                                                                            .contentType("application/json");

        final String response = mockMvc.perform(request)
                                       .andExpect(status().isOk())
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        assertEquals(mapper.writeValueAsString(panel), response);
        verify(mockDashboardFrameworkService).create(panel);
    }

    @Test
    public void test_update() throws Exception {
        // Setup
        when(mockDashboardFrameworkService.update(panel)).thenReturn(panel);

        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/jupyterhub/dashboards/frameworks/" + panel.getName())
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext())
                                                                            .content(mapper.writeValueAsString(panel))
                                                                            .contentType("application/json");

        final String response = mockMvc.perform(request)
                                       .andExpect(status().isOk())
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        // Verify
        assertNotNull(response);
        assertEquals(mapper.writeValueAsString(panel), response);
        verify(mockDashboardFrameworkService).update(panel);
    }

    @Test
    public void test_delete() throws Exception {
        // Setup
        // Execute
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/jupyterhub/dashboards/frameworks/" + panel.getName())
                                                                            .with(authentication(mockAuthentication))
                                                                            .with(csrf())
                                                                            .with(testSecurityContext());

        mockMvc.perform(request)
               .andExpect(status().isOk());

        // Verify
        verify(mockDashboardFrameworkService).delete(panel.getName());
    }

}