package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultJupyterHubServiceAccountHelperTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultJupyterHubServiceAccountHelperTestConfig.class)
public class DefaultJupyterHubServiceAccountHelperTest extends TestCase {

    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private RoleServiceI mockRoleService;

    @Autowired private DefaultJupyterHubServiceAccountHelper defaultJupyterHubServiceAccountHelper;

    @Test
    public void testServiceAccountDisabled_noUsersWithRole() {
        when(mockRoleService.getUsers("JupyterHub")).thenReturn(Collections.emptyList());
        assertFalse(defaultJupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled());
    }

    @Test
    public void testServiceAccountDisabled_userWithRoleButNotEnabled() throws Exception {
        // Setup mock user. User has role but is not enabled.
        UserI mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("jupyterhub");
        when(mockUser.isEnabled()).thenReturn(false);
        when(mockUserManagementService.getUser("jupyterhub")).thenReturn(mockUser);
        when(mockRoleService.getUsers("JupyterHub")).thenReturn(Collections.singletonList("jupyterhub"));

        // Test
        assertFalse(defaultJupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled());
    }

    @Test
    public void testServiceAccountEnabled_userWithRoleAndEnabled() throws Exception {
        // Setup mock user. User has role and is enabled.
        UserI mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("jupyterhub");
        when(mockUser.isEnabled()).thenReturn(true);
        when(mockUserManagementService.getUser("jupyterhub")).thenReturn(mockUser);
        when(mockRoleService.getUsers("JupyterHub")).thenReturn(Collections.singletonList("jupyterhub"));

        // Test
        assertTrue(defaultJupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled());
    }

    @Test
    public void testServiceAccountEnabled_multipleUsersWithRole() throws Exception {
        // Setup mock users. One user has role and is enabled. Another user has role but is not enabled.
        UserI mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("jupyterhub");
        when(mockUser.isEnabled()).thenReturn(false);
        when(mockUserManagementService.getUser("jupyterhub")).thenReturn(mockUser);

        UserI mockUser2 = Mockito.mock(UserI.class);
        when(mockUser2.getLogin()).thenReturn("jupyterhub-service-account");
        when(mockUser2.isEnabled()).thenReturn(true);
        when(mockUserManagementService.getUser("jupyterhub-service-account")).thenReturn(mockUser2);

        when(mockRoleService.getUsers("JupyterHub")).thenReturn(Arrays.asList("jupyterhub", "jupyterhub-service-account"));

        // Test
        assertTrue(defaultJupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled());
    }

    @Test
    public void testServiceAccountEnabled_multipleUsersWithRoleButNoEnabledUsers() throws Exception {
        // Setup mock users. One user has role but is not enabled. Another user has role but is not enabled.
        UserI mockUser = Mockito.mock(UserI.class);
        when(mockUser.getLogin()).thenReturn("jupyterhub");
        when(mockUser.isEnabled()).thenReturn(false);
        when(mockUserManagementService.getUser("jupyterhub")).thenReturn(mockUser);

        UserI mockUser2 = Mockito.mock(UserI.class);
        when(mockUser2.getLogin()).thenReturn("jupyterhub-service-account");
        when(mockUser2.isEnabled()).thenReturn(false);
        when(mockUserManagementService.getUser("jupyterhub-service-account")).thenReturn(mockUser2);

        when(mockRoleService.getUsers("JupyterHub")).thenReturn(Arrays.asList("jupyterhub", "jupyterhub-service-account"));

        // Test
        assertFalse(defaultJupyterHubServiceAccountHelper.isJupyterHubServiceAccountEnabled());
    }
}