package org.nrg.xnatx.plugins.jupyterhub.authorization;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterAuthorizationConfig;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterAuthorizationConfig.class)
public class JupyterUserAuthorizationTest {

    @Autowired private JupyterUserAuthorization jupyterUserAuthorization;
    @Autowired private RoleServiceI mockRoleService;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;

    private UserI user;
    private String username;

    @Before
    public void before() {
        // Mock the user
        user = mock(UserI.class);
        username = "user";
        when(user.getUsername()).thenReturn(username);
    }

    @After
    public void after() {
        Mockito.reset(mockRoleService, mockJupyterHubPreferences);
    }

    @Test
    @Ignore
    public void testCheckImpl() {
        // Tough to test this. super.checkImpl(...) is protected so can't spy with Mockito.
        // If you decouple JupyterUserAuthorization from UserXapiAuthorization then you have to deal with
        // the JoinPoint in checkImpl(...)
    }

    @Test
    public void testJupyter_AllUsers() {
        // Setup
        when(mockJupyterHubPreferences.getAllUsersCanStartJupyter()).thenReturn(true);
        when(mockRoleService.checkRole(any(), eq("Jupyter"))).thenReturn(false);
        when(mockRoleService.isSiteAdmin(user)).thenReturn(false);

        // Test
        boolean check = jupyterUserAuthorization.checkJupyter(user);

        // Verify
        assertTrue("Jupyter All User preference is enabled. This should allow all.", check);
    }

    @Test
    public void testJupyter_JupyterUsers() {
        // Setup
        when(mockJupyterHubPreferences.getAllUsersCanStartJupyter()).thenReturn(false);
        when(mockRoleService.checkRole(any(), eq("Jupyter"))).thenReturn(true);
        when(mockRoleService.isSiteAdmin(user)).thenReturn(false);

        // Test
        boolean check = jupyterUserAuthorization.checkJupyter(user);

        // Verify
        assertTrue("Jupyter All User preference is enabled. This should allow all.", check);
    }

    @Test
    public void testJupyter_Admin() {
        // Setup
        when(mockJupyterHubPreferences.getAllUsersCanStartJupyter()).thenReturn(false);
        when(mockRoleService.checkRole(any(), eq("Jupyter"))).thenReturn(false);
        when(mockRoleService.isSiteAdmin(user)).thenReturn(true);

        // Test
        boolean check = jupyterUserAuthorization.checkJupyter(user);

        // Verify
        assertTrue("Jupyter All User preference is enabled. This should allow all.", check);
    }

    @Test
    public void testGuestUserNeverAuthorized() {
        boolean check = jupyterUserAuthorization.considerGuests();
        assertFalse("Guest users should not be authorized", check);
    }

}