package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubUserInitializerConfig;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubUserInitializerConfig.class)
public class JupyterHubUserInitializerTest {

    @Autowired private JupyterHubUserInitializer jupyterHubUserInitializer;
    @Autowired private UserManagementServiceI mockUserManagementService;
    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private RoleServiceI mockRoleService;

    private final String username = "jupyterhub";

    @After
    public void after() {
        Mockito.reset(mockUserManagementService);
        Mockito.reset(mockXFTManagerHelper);
        Mockito.reset(mockRoleService);
    }

    @Test(expected = InitializingTaskException.class)
    public void test_XFTManagerNotInitialized() throws Exception {
        // XFT not initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);

        // Should throw InitializingTaskException
        jupyterHubUserInitializer.callImpl();
    }

    @Test(expected = InitializingTaskException.class)
    public void test_AppNotInitialized() throws Exception {
        // XFT initialized but app not initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Should throw InitializingTaskException
        jupyterHubUserInitializer.callImpl();
    }

    @Test
    public void test_JHUserAlreadyExists() throws Exception {
        // Setup
        // XFT initialized and user already exists
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockUserManagementService.exists(username)).thenReturn(true);

        // Test
        jupyterHubUserInitializer.callImpl();

        // Verify user and role not saved
        verify(mockUserManagementService, never()).save(any(UserI.class), any(UserI.class),
                                                        anyBoolean(), any(EventDetails.class));
        verify(mockRoleService, never()).addRole(any(), any(), any());
    }

    @Test(expected = InitializingTaskException.class)
    public void test_FailedToSaveUser() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockUserManagementService.exists(username)).thenReturn(false);
        when(mockUserManagementService.createUser()).thenReturn(mock(UserI.class));
        doThrow(Exception.class).when(mockUserManagementService).save(any(UserI.class), nullable(UserI.class),
                                                                      anyBoolean(), any(EventDetails.class));

        // Test
        jupyterHubUserInitializer.callImpl();

        // Verify
        verify(mockUserManagementService, times(1)).save(any(UserI.class), any(UserI.class),
                                                         anyBoolean(), any(EventDetails.class));
        verify(mockRoleService, never()).addRole(any(), any(), any());
    }

    @Test(expected = InitializingTaskException.class)
    public void test_FailedAddUserRole() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockUserManagementService.exists(username)).thenReturn(false);
        when(mockUserManagementService.createUser()).thenReturn(mock(UserI.class));
        when(mockRoleService.addRole(nullable(UserI.class), any(UserI.class), anyString())).thenReturn(false);

        // Test
        jupyterHubUserInitializer.callImpl();

        // Verify
        verify(mockUserManagementService, times(1)).save(any(UserI.class),
                                                         nullable(UserI.class), // Users.getAdminUser() is null during tests...
                                                         anyBoolean(),
                                                         any(EventDetails.class));
        verify(mockRoleService, times(1)).addRole(any(), any(), any());
    }

    @Test(expected = InitializingTaskException.class)
    public void test_FailedAddUserRole_Exception() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockUserManagementService.exists(username)).thenReturn(false);
        when(mockUserManagementService.createUser()).thenReturn(mock(UserI.class));
        doThrow(Exception.class).when(mockRoleService).addRole(nullable(UserI.class), any(UserI.class), anyString());

        // Test
        jupyterHubUserInitializer.callImpl();

        // Verify
        verify(mockUserManagementService, times(1)).save(any(UserI.class),
                                                         nullable(UserI.class), // Users.getAdminUser() is null during tests...
                                                         anyBoolean(),
                                                         any(EventDetails.class));
        verify(mockRoleService, times(1)).addRole(any(), any(), any());
    }

    @Test
    public void test_UserCreated() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockUserManagementService.exists(username)).thenReturn(false);
        UserI mockUser = mock(UserI.class);
        when(mockUserManagementService.createUser()).thenReturn(mockUser);
        when(mockRoleService.addRole(nullable(UserI.class), any(UserI.class), anyString())).thenReturn(true);

        // Test
        jupyterHubUserInitializer.callImpl();

        // Verify
        verify(mockUserManagementService, times(1)).save(eq(mockUser),
                                                         nullable(UserI.class), // Users.getAdminUser() is null during tests...
                                                         anyBoolean(),
                                                         any(EventDetails.class));
        verify(mockRoleService, times(1)).addRole(nullable(UserI.class), eq(mockUser), eq("JupyterHub"));

        verify(mockUser).setLogin(eq("jupyterhub"));
        verify(mockUser).setPassword(eq("jupyterhub"));
        verify(mockUser).setFirstname(anyString());
        verify(mockUser).setLastname(anyString());
        verify(mockUser).setEmail(anyString());
        verify(mockUser).setEnabled(eq(false));
    }

    @Test
    public void test_TaskName() {
        final String taskName = jupyterHubUserInitializer.getTaskName();
        assertNotNull(taskName);
    }

}