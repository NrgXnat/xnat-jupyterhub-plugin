package org.nrg.xnatx.plugins.jupyterhub.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubProfilesApiConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.ContainerSpec;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubProfilesApiConfig.class)
public class JupyterHubProfilesApiTest {

    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private ProfileService mockProfileService;

    private MockMvc mockMvc;

    private final MediaType JSON = MediaType.APPLICATION_JSON_UTF8;

    private Authentication mockAuthentication;

    private Profile profile;

    @Before
    public void setUp() throws Exception {
        // Setup mock web context
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Setup user
        String mockUsername = "mockUser";
        String mockPassword = "mockPassword";
        Integer mockUserId = 1;
        UserI mockUser = Mockito.mock(UserI.class);
        Mockito.when(mockUser.getID()).thenReturn(mockUserId);
        Mockito.when(mockUser.getLogin()).thenReturn(mockUsername);
        Mockito.when(mockUser.getUsername()).thenReturn(mockUsername);
        Mockito.when(mockUser.getPassword()).thenReturn(mockPassword);

        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(false);
        when(mockUserManagementService.getUser(mockUsername)).thenReturn(mockUser);

        mockAuthentication = new TestingAuthenticationToken(mockUser, mockPassword);

        // Setup profile
        ContainerSpec containerSpec = ContainerSpec.builder()
                .image("image")
                .build();

        TaskTemplate taskTemplate = TaskTemplate.builder()
                .containerSpec(containerSpec)
                .build();

        profile = Profile.builder()
                .id(1L)
                .name("name")
                .description("description")
                .taskTemplate(taskTemplate)
                .build();
    }

    @After
    public void tearDown() {
        Mockito.reset(mockUserManagementService);
        Mockito.reset(mockRoleService);
        Mockito.reset(mockProfileService);
        Mockito.reset(mockRoleService);
        Mockito.reset(mockProfileService);
    }

    @Test
    public void getProfile() throws Exception {
        // Setup mock
        when(mockProfileService.get(profile.getId())).thenReturn(Optional.of(profile));

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Profile responseProfile = mapper.readValue(response, Profile.class);

        assertEquals(profile, responseProfile);
    }

    @Test
    public void getProfile_doesNotExist() throws Exception {
        // Setup mock
        when(mockProfileService.get(profile.getId())).thenReturn(Optional.empty());

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void getAllProfiles() throws Exception {
        // Setup mock
        when(mockProfileService.getAll()).thenReturn(Collections.singletonList(profile));

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/jupyterhub/profiles")
                .accept(JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Profile> responseProfiles = mapper.readValue(response, new TypeReference<List<Profile>>() {});

        assertEquals(Collections.singletonList(profile), responseProfiles);
    }

    @Test
    public void createProfile() throws Exception {
        // Setup mock
        when(mockProfileService.create(profile)).thenReturn(profile.getId());

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/jupyterhub/profiles")
                .accept(JSON)
                .contentType(JSON)
                .content(mapper.writeValueAsString(profile))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        final String response = mockMvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long responseProfileId = mapper.readValue(response, Long.class);

        assertEquals(profile.getId(), responseProfileId);
    }

    @Test
    public void updateProfile() throws Exception {
        // Setup mock
        when(mockProfileService.exists(profile.getId())).thenReturn(true);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .contentType(JSON)
                .content(mapper.writeValueAsString(profile))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify mock is called
        verify(mockProfileService, times(1)).update(profile);
    }

    @Test
    public void updateProfile_doesNotExist() throws Exception {
        // Setup mock
        when(mockProfileService.exists(profile.getId())).thenReturn(false);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .contentType(JSON)
                .content(mapper.writeValueAsString(profile))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void updateProfile_cantHaveDifferentId() throws Exception {
        // Setup mock
        when(mockProfileService.exists(profile.getId())).thenReturn(true);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .contentType(JSON)
                .content(mapper.writeValueAsString(Profile.builder().id(2L).build()))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void deleteProfile() throws Exception {
        // Setup mock
        when(mockProfileService.exists(profile.getId())).thenReturn(true);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify mock is called
        verify(mockProfileService, times(1)).delete(profile.getId());
    }

    @Test
    public void deleteProfile_doesNotExist() throws Exception {
        // Setup mock
        when(mockProfileService.exists(profile.getId())).thenReturn(false);

        // Test
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/jupyterhub/profiles/" + profile.getId())
                .accept(JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}
