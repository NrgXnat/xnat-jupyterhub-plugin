package org.nrg.xnatx.plugins.jupyterhub.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.client.models.Server;
import org.nrg.xnatx.plugins.jupyterhub.client.models.User;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubApiConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.BindMount;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubApiConfig.class)
public class JupyterHubApiTest {

    private final static String ADMIN_USERNAME = "admin";
    private final static String NON_ADMIN_USERNAME = "non-admin";

    private Authentication ADMIN_AUTH;
    private Authentication NONADMIN_AUTH;

    private MockMvc mockMvc;

    private final MediaType JSON = MediaType.APPLICATION_JSON_UTF8;

    private UserI admin;
    private UserI nonAdmin;
    private User  nonAdmin_jh;

    private XnatUserOptions userOptions;
    private Server dummyServer;
    private String servername;
    private Server dummyNamedServer;

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private JupyterHubService mockJupyterHubService;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private UserOptionsService mockUserOptionsService;

    @Before
    public void before() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Mock the users
        final String adminPassword = "admin-pass";
        final Integer adminId = 2;
        admin = mock(UserI.class);
        when(admin.getLogin()).thenReturn(ADMIN_USERNAME);
        when(admin.getPassword()).thenReturn(adminPassword);
        when(admin.getID()).thenReturn(adminId);
        when(mockRoleService.isSiteAdmin(admin)).thenReturn(true);
        when(mockUserManagementService.getUser(ADMIN_USERNAME)).thenReturn(admin);
        ADMIN_AUTH = new TestingAuthenticationToken(admin, adminPassword);

        final String nonAdminPassword = "non-admin-pass";
        final Integer nonAdminId = 2;
        nonAdmin = mock(UserI.class);
        when(nonAdmin.getLogin()).thenReturn(NON_ADMIN_USERNAME);
        when(nonAdmin.getPassword()).thenReturn(nonAdminPassword);
        when(admin.getID()).thenReturn(nonAdminId);
        when(mockRoleService.isSiteAdmin(nonAdmin)).thenReturn(false);
        when(mockUserManagementService.getUser(NON_ADMIN_USERNAME)).thenReturn(nonAdmin);
        NONADMIN_AUTH = new TestingAuthenticationToken(nonAdmin, nonAdminPassword);

        // Mock the JupyterHub servers
        Map<String, String> environmentalVariables = new HashMap<>();
        environmentalVariables.put("XNAT_HOST", "fake://localhost");

        BindMount bindMount = BindMount.builder()
                .name("TestProject")
                .writable(false)
                .containerHostPath("/home/someone/xnat/data/archive/TestProject")
                .xnatHostPath("/data/xnat/archive/TestProject")
                .jupyterHostPath("/data/TestProject")
                .build();

        userOptions = XnatUserOptions.builder()
                .userId(nonAdminId)
                .servername("")
                .xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME)
                .itemId("TestProject")
                .projectId("TestProject")
                .eventTrackingId("20220822T201541799Z")
                .dockerImage("xnat/jupyterhub-user:latest")
                .environmentVariables(environmentalVariables)
                .bindMounts(Lists.newArrayList(bindMount))
                .build();

        dummyServer = Server.builder()
                .name("")
                .ready(true)
                .url("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .started(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                .last_activity(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                //.user_options(userOptions)
                .build();

        servername = "test-server";
        dummyNamedServer = Server.builder()
                .name(servername)
                .ready(true)
                .url("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/test-server")
                .started(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                .last_activity(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                //.user_options(userOptions)
                .build();

        // Mock the JupyterHub user
        nonAdmin_jh = User.builder()
                .name(NON_ADMIN_USERNAME)
                .admin(false)
                .roles(Collections.emptyList())
                .groups(Collections.emptyList())
                .server("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .last_activity(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                .servers(Collections.singletonMap(servername, dummyNamedServer))
                .build();
    }

    @After
    public void after() {
        Mockito.reset(admin);
        Mockito.reset(mockJupyterHubService);
    }

    @Test
    public void testGetUser() throws Exception {
        when(mockJupyterHubService.getUser(eq(nonAdmin))).thenReturn(Optional.of(nonAdmin_jh));

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + NON_ADMIN_USERNAME)
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        User responseUser = mapper.readValue(response, User.class);

        assertEquals(nonAdmin_jh, responseUser);
    }

    @Test
    public void testCreateUser() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/users/" + NON_ADMIN_USERNAME)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        verify(mockJupyterHubService, times(1)).createUser(nonAdmin);
    }

    @Test
    public void testGetServer() throws Exception {
        // Unnamed server
        when(mockJupyterHubService.getServer(eq(nonAdmin)))
                .thenReturn(Optional.of(dummyServer));

        // Named server
        when(mockJupyterHubService.getServer(eq(nonAdmin), anyString()))
                .thenReturn(Optional.of(dummyNamedServer));

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Server responseServer = mapper.readValue(response, Server.class);

        assertEquals(dummyServer, responseServer);
    }

    @Test
    public void testGetNamedAdminServer() throws Exception {
        // Unnamed server
        when(mockJupyterHubService.getServer(eq(nonAdmin)))
                .thenReturn(Optional.of(dummyServer));

        // Named server
        when(mockJupyterHubService.getServer(eq(nonAdmin), anyString()))
                .thenReturn(Optional.of(dummyNamedServer));


        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/" + servername)
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Server out = mapper.readValue(response, Server.class);

        assertEquals(out, dummyNamedServer);
    }

    @Test
    @Ignore("See CS-266. restrictTo property is not working")
    public void testGetServerDeniedWrongUser() throws Exception {
        // Unnamed server
        when(mockJupyterHubService.getServer(eq(admin)))
                .thenReturn(Optional.of(dummyServer));

        // Named server
        when(mockJupyterHubService.getServer(eq(admin), anyString()))
                .thenReturn(Optional.of(dummyNamedServer));


        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + ADMIN_USERNAME + "/server/" + servername)
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isForbidden());
    }

    @Test
    public void testStartServer() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .param("username", NON_ADMIN_USERNAME)
                .param("xsiType", userOptions.getXsiType())
                .param("itemId", userOptions.getItemId())
                .param("projectId", userOptions.getProjectId())
                .param("eventTrackingId", userOptions.getEventTrackingId())
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Unnamed server
        verify(mockJupyterHubService, times(1)).startServer(eq(nonAdmin),
                                                            eq(userOptions.getXsiType()),
                                                            eq(userOptions.getItemId()),
                                                            eq(userOptions.getProjectId()),
                                                            eq(userOptions.getEventTrackingId()));
        // Named Server
        verify(mockJupyterHubService, never()).startServer(any(UserI.class),
                                                           anyString(),
                                                           anyString(),
                                                           anyString(),
                                                           anyString(),
                                                           anyString());
    }

    @Test
    public void testStartNamedServer() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/" + servername)
                .param("username", NON_ADMIN_USERNAME)
                .param("xsiType", userOptions.getXsiType())
                .param("itemId", userOptions.getItemId())
                .param("projectId", userOptions.getProjectId())
                .param("eventTrackingId", userOptions.getEventTrackingId())
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Unnamed server
        verify(mockJupyterHubService, never()).startServer(any(UserI.class),
                                                           anyString(),
                                                           anyString(),
                                                           anyString(),
                                                           anyString());

        // Named Server
        verify(mockJupyterHubService, times(1)).startServer(eq(nonAdmin),
                                                            eq(servername),
                                                            eq(userOptions.getXsiType()),
                                                            eq(userOptions.getItemId()),
                                                            eq(userOptions.getProjectId()),
                                                            eq(userOptions.getEventTrackingId()));

    }

    @Test
    public void testStopServer() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .param("username", NON_ADMIN_USERNAME)
                .param("eventTrackingId", userOptions.getEventTrackingId())
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Unnamed server
        verify(mockJupyterHubService, times(1)).stopServer(eq(nonAdmin), eq(userOptions.getEventTrackingId()));
        // Named server
        verify(mockJupyterHubService, never()).stopServer(any(UserI.class), anyString(), anyString());
    }

    @Test
    public void testStopNamedServer() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/" + servername)
                .param("username", NON_ADMIN_USERNAME)
                .param("eventTrackingId", userOptions.getEventTrackingId())
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Unnamed server
        verify(mockJupyterHubService, never()).stopServer(any(UserI.class), anyString());
        // Named server
        verify(mockJupyterHubService, times(1)).stopServer(eq(nonAdmin), eq(servername), eq(userOptions.getEventTrackingId()));
    }

    @Test
    public void testGetUserOptions() throws Exception {
        when(mockUserOptionsService.retrieveUserOptions(eq(nonAdmin))).thenReturn(Optional.of(userOptions));

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/user-options")
                .param("username", NON_ADMIN_USERNAME)
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        XnatUserOptions responseUserOptions = mapper.readValue(response, XnatUserOptions.class);

        assertEquals(userOptions, responseUserOptions);
    }

    @Test
    public void testGetUserOptionsNamedServer() throws Exception {
        when(mockUserOptionsService.retrieveUserOptions(eq(nonAdmin), eq(servername))).thenReturn(Optional.of(userOptions));

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server/" + servername + "/user-options")
                .param("username", NON_ADMIN_USERNAME)
                .accept(JSON)
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        XnatUserOptions responseUserOptions = mapper.readValue(response, XnatUserOptions.class);

        assertEquals(userOptions, responseUserOptions);
    }

}