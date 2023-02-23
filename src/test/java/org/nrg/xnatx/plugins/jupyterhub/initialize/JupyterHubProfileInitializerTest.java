package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubProfileInitializerConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubProfileInitializerConfig.class)
public class JupyterHubProfileInitializerTest {

    @Autowired private JupyterHubProfileInitializer jupyterHubProfileInitializer;
    @Autowired private ProfileService mockProfileService;
    @Autowired private XFTManagerHelper mockXFTManagerHelper;

    @After
    public void after() {
        Mockito.reset(mockProfileService);
        Mockito.reset(mockXFTManagerHelper);
    }

    @Test
    public void getTaskName() {
        assertNotNull(jupyterHubProfileInitializer.getTaskName());
    }

    @Test(expected = InitializingTaskException.class)
    public void callImpl_xftManagerNotInitialized() throws Exception {
        // XFT not initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);

        // Should throw InitializingTaskException
        jupyterHubProfileInitializer.callImpl();
    }

    @Test
    public void callImpl_profileServiceHasProfiles() throws Exception {
        // Setup
        // XFT initialized and profile service has profiles
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockProfileService.getAll()).thenReturn(Collections.singletonList(Profile.builder().build()));

        // Test
        jupyterHubProfileInitializer.callImpl();

        // Verify profile service not called
        verify(mockProfileService, never()).create(any(Profile.class));
    }

    @Test
    public void callImpl_initProfile() throws Exception {
        // Setup
        // XFT initialized and profile service has no profiles
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockProfileService.getAll()).thenReturn(Collections.emptyList());

        // Test
        jupyterHubProfileInitializer.callImpl();

        // Verify profile service called
        verify(mockProfileService, times(1)).create(any(Profile.class));
    }
}