package org.nrg.jobtemplates.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.jobtemplates.config.ComputeSpecConfigsApiConfig;
import org.nrg.jobtemplates.models.ComputeSpecConfig;
import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
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
import org.springframework.web.util.NestedServletException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ComputeSpecConfigsApiConfig.class})
public class ComputeSpecConfigsApiTest {

    @Autowired private WebApplicationContext wac;
    @Autowired private ObjectMapper mapper;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private ComputeSpecConfigService mockComputeSpecConfigService;

    private MockMvc mockMvc;
    private UserI mockUser;
    private Authentication mockAuthentication;

    private ComputeSpecConfig computeSpecConfig;

    @Before
    public void before() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

        // Mock the user
        mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("mockUser");
        when(mockUser.getEmail()).thenReturn("mockUser@mockuser.com");
        when(mockUser.getPassword()).thenReturn("mockUserPassword");
        when(mockUser.getID()).thenReturn(1);
        when(mockRoleService.isSiteAdmin(mockUser)).thenReturn(true);
        mockAuthentication = new TestingAuthenticationToken(mockUser, mockUser.getPassword());

        // Setup the compute spec config
        computeSpecConfig = new ComputeSpecConfig();
        computeSpecConfig.setId(1L);
    }

    @After
    public void after() throws Exception {
        Mockito.reset(
                mockRoleService,
                mockUserManagementService,
                mockComputeSpecConfigService,
                mockUser
        );
    }

    @Test
    public void testGetAllComputeSpecConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/job-templates/compute-spec-configs")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).getAll();
        verify(mockComputeSpecConfigService, never()).getByType(any());
    }

    @Test
    public void testGetComputeSpecConfigsByType() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/job-templates/compute-spec-configs")
                .param("type", "JUPYTERHUB")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, never()).getAll();
        verify(mockComputeSpecConfigService, times(1)).getByType(any());
    }

    @Test
    public void testGetComputeSpecConfigById() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/job-templates/compute-spec-configs/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).retrieve(any());
    }

    @Test
    public void testGetComputeSpecConfigByIdNotFound() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/job-templates/compute-spec-configs/1")
                .accept(APPLICATION_JSON)
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.empty());

        mockMvc.perform(request).andExpect(status().isNotFound());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).retrieve(any());
    }

    @Test
    public void testCreateComputeSpecConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post("/job-templates/compute-spec-configs")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(new ComputeSpecConfig()))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isCreated());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).create(any());
    }

    @Test
    public void testUpdateComputeSpecConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/job-templates/compute-spec-configs/1")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(computeSpecConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).update(eq(computeSpecConfig));
    }

    @Test(expected = NestedServletException.class)
    public void testUpdateComputeSpecConfig_IdMismatch() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put("/job-templates/compute-spec-configs/2")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(computeSpecConfig))
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        // Throws NestedServletException in response to IllegalArgumentException
        mockMvc.perform(request).andExpect(status().isBadRequest());

        // Verify that the service was not called
        verify(mockComputeSpecConfigService, never()).update(any());
    }

    @Test
    public void testDeleteComputeSpecConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete("/job-templates/compute-spec-configs/1")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isNoContent());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).delete(eq(1L));
    }

    @Test
    public void testGetAvailableComputeSpecConfigs() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/job-templates/compute-spec-configs/available")
                .param("user", mockUser.getLogin())
                .param("project", "projectId")
                .param("type", "JUPYTERHUB")
                .with(authentication(mockAuthentication))
                .with(csrf())
                .with(testSecurityContext());

        mockMvc.perform(request).andExpect(status().isOk());

        // Verify that the service was called
        verify(mockComputeSpecConfigService, times(1)).getAvailable(eq(mockUser.getLogin()), eq("projectId"), eq(ComputeSpecConfig.ConfigType.JUPYTERHUB));
    }


}