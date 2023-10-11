package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubPreferenceInitializerTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.SystemHelper;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubPreferenceInitializerTestConfig.class)
public class JupyterHubPreferenceInitializerTest {

    @Autowired private JupyterHubPreferenceInitializer jupyterHubPreferenceInitializer;
    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;
    @Autowired private SiteConfigPreferences mockSiteConfigPreferences;
    @Autowired private SystemHelper mockSystemHelper;

    @Before
    public void setUp() {
        assertNotNull(jupyterHubPreferenceInitializer);
        assertNotNull(mockXFTManagerHelper);
        assertNotNull(mockXnatAppInfo);
        assertNotNull(mockJupyterHubPreferences);
        assertNotNull(mockSystemHelper);
    }

    @After
    public void tearDown() {
        Mockito.reset(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockJupyterHubPreferences,
                mockSystemHelper
        );
    }

    @Test
    public void getTaskName() {
        // Test
        String taskName = jupyterHubPreferenceInitializer.getTaskName();

        // Verify
        assertThat(taskName, notNullValue());
        assertThat(taskName, isA(String.class));
    }

    @Test(expected = InitializingTaskException.class)
    public void callImpl_notInitialized1() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        // Exception thrown
        verify(mockJupyterHubPreferences, never()).setJupyterHubHostUrl(any());
    }

    @Test(expected = InitializingTaskException.class)
    public void callImpl_notInitialized2() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        // Exception thrown
        verify(mockJupyterHubPreferences, never()).setJupyterHubHostUrl(any());
    }

    @Test
    public void callImpl_initialized() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences, times(1)).setJupyterHubHostUrl(any());
    }

    @Test
    public void callImpl_initialized_alreadySet() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockJupyterHubPreferences.getJupyterHubHostUrl()).thenReturn("https://my-xnat.com");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences, never()).setJupyterHubHostUrl(any());
    }


    @Test
    public void callImpl_initialized_envSet() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockJupyterHubPreferences.getJupyterHubHostUrl()).thenReturn("");

        when(mockSystemHelper.getEnv("JH_HOST_URL")).thenReturn("https://my-xnat.com");
        when(mockSiteConfigPreferences.getSiteUrl()).thenReturn("https://another-xnat.com");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences).setJupyterHubHostUrl("https://my-xnat.com");
    }

    @Test
    public void callImpl_initialized_envNotSet() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockJupyterHubPreferences.getJupyterHubHostUrl()).thenReturn("");

        when(mockSystemHelper.getEnv("JH_HOST_URL")).thenReturn("");
        when(mockSiteConfigPreferences.getSiteUrl()).thenReturn("https://another-xnat.com");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences).setJupyterHubHostUrl("https://another-xnat.com");
    }

}