package org.nrg.xnatx.plugins.jupyterhub.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.nrg.xnatx.plugins.jupyterhub.client.models.*;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubApiConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.*;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.*;
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
import java.util.*;

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
    private User admin_jh;
    private UserI nonAdmin;
    private User  nonAdmin_jh;

    private XnatUserOptions userOptions;
    private ServerStartRequest serverStartRequest;
    private Long profileId;
    private Server dummyServer;
    private String servername;
    private Server dummyNamedServer;
    private Hub hubLimited;
    private Hub hubFull;

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
        when(nonAdmin.getUsername()).thenReturn(NON_ADMIN_USERNAME);
        when(nonAdmin.getPassword()).thenReturn(nonAdminPassword);
        when(admin.getID()).thenReturn(nonAdminId);
        when(mockRoleService.isSiteAdmin(nonAdmin)).thenReturn(false);
        when(mockUserManagementService.getUser(NON_ADMIN_USERNAME)).thenReturn(nonAdmin);
        NONADMIN_AUTH = new TestingAuthenticationToken(nonAdmin, nonAdminPassword);

        // Mock the JupyterHub servers
        Map<String, String> environmentalVariables = new HashMap<>();
        environmentalVariables.put("XNAT_HOST", "fake://localhost");

        Mount mount = Mount.builder()
                .source("/home/someone/xnat/data/archive/TestProject")
                .target("/data/TestProject")
                .type("bind")
                .readOnly(true)
                .build();

        ContainerSpec containerSpec = ContainerSpec.builder()
                .image("xnat/jupyterhub-single-user:latest")
                .labels(Collections.singletonMap("label", "label"))
                .env(environmentalVariables)
                .mounts(Collections.singletonList(mount))
                .build();

        Placement placement = Placement.builder()
                .constraints(Collections.singletonList("engine.labels.instance.type==jupyter"))
                .build();

        Resources resources = Resources.builder()
                .cpuLimit(4.0)
                .cpuReservation(4.0)
                .memLimit("16G")
                .memReservation("16G")
                .build();

        TaskTemplate taskTemplate = TaskTemplate.builder()
                .containerSpec(containerSpec)
                .placement(placement)
                .resources(resources)
                .build();

        profileId = 2L;

        userOptions = XnatUserOptions.builder()
                .userId(nonAdminId)
                .servername("")
                .xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME)
                .itemId("TestProject")
                .itemLabel("TestProject")
                .projectId("TestProject")
                .eventTrackingId("20220822T201541799Z")
                .taskTemplate(taskTemplate)
                .build();

        serverStartRequest = ServerStartRequest.builder()
                .username(NON_ADMIN_USERNAME)
                .servername("")
                .xsiType(XnatProjectdata.SCHEMA_ELEMENT_NAME)
                .itemId("TestProject")
                .itemLabel("TestProject")
                .projectId("TestProject")
                .eventTrackingId("20220822T201541799Z")
                .computeEnvironmentConfigId(1L)
                .hardwareConfigId(1L)
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
        admin_jh = User.builder()
                .name(ADMIN_USERNAME)
                .admin(false)
                .roles(Collections.emptyList())
                .groups(Collections.emptyList())
                .server("/jupyterhub/users/" + ADMIN_USERNAME + "/server")
                .last_activity(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                .servers(Collections.singletonMap(servername, dummyNamedServer))
                .build();

        nonAdmin_jh = User.builder()
                .name(NON_ADMIN_USERNAME)
                .admin(false)
                .roles(Collections.emptyList())
                .groups(Collections.emptyList())
                .server("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/server")
                .last_activity(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                .servers(Collections.singletonMap(servername, dummyNamedServer))
                .build();

        // Mock hub info
        hubLimited = Hub.builder()
                .version("3.0.0")
                .build();

        Authenticator authenticator = Authenticator.builder()
                .version("unknown")
                .authenticatorClass("builtins.XnatAuthenticator")
                .build();

        Spawner spawner = Spawner.builder()
                .version("12.1.0")
                .spawnerClass("dockerspawner.swarmspawner.SwarmSpawner")
                .build();

        hubFull = Hub.builder()
                .version("3.0.0")
                .python("3.10.4 (main, Jun 29 2022, 12:14:53) [GCC 11.2.0]")
                .authenticator(authenticator)
                .spawner(spawner)
                .build();

    }

    @After
    public void after() {
        Mockito.reset(admin);
        Mockito.reset(mockJupyterHubService);
    }


    @Test
    public void testGetVersion() throws Exception {
        when(mockJupyterHubService.getVersion()).thenReturn(hubLimited);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/version")
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

        Hub responseHub = mapper.readValue(response, Hub.class);

        assertEquals(hubLimited, responseHub);
    }

    @Test
    public void testGetInfo() throws Exception {
        when(mockJupyterHubService.getInfo()).thenReturn(hubFull);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/info")
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

        Hub responseHub = mapper.readValue(response, Hub.class);

        assertEquals(hubFull, responseHub);
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
    public void testGetUsers() throws Exception {
        List<User> users = Arrays.asList(nonAdmin_jh, admin_jh);
        when(mockJupyterHubService.getUsers()).thenReturn(users);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/users")
                .accept(JSON)
                .with(authentication(ADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        final String response =
                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(JSON))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        User[] responseUsers = mapper.readValue(response, User[].class);

        assertEquals(users, Arrays.asList(responseUsers));
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
                .accept(JSON)
                .contentType(JSON)
                .content(mapper.writeValueAsString(serverStartRequest))
                .with(authentication(NONADMIN_AUTH))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        verify(mockJupyterHubService, times(1)).startServer(eq(nonAdmin), eq(serverStartRequest));
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

    @Test
    public void testGetToken() throws Exception {
        // Setup
        final String note = "token note";
        final Integer expiresIn = 60;
        final Token token = Token.builder()
                .note(note)
                .token("token1234567890")
                .build();

        when(mockJupyterHubService.createToken(eq(nonAdmin), eq(note), eq(expiresIn))).thenReturn(token);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/users/" + NON_ADMIN_USERNAME + "/tokens")
                .param("username", NON_ADMIN_USERNAME)
                .param("note", note)
                .param("expiresIn", expiresIn.toString())
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

        Token responseToken = mapper.readValue(response, Token.class);

        // Verify
        verify(mockJupyterHubService, times(1)).createToken(eq(nonAdmin), eq(note), eq(expiresIn));
        assertEquals(token, responseToken);
    }

}