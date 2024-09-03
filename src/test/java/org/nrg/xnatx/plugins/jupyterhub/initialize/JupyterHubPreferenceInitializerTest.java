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
    public void callImpl_jhHostUrlAlreadySet() throws Exception {
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
    public void callImpl_jhHostUrlSetToSiteUrl() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockSiteConfigPreferences.getSiteUrl()).thenReturn("https://my-xnat.com");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences).setJupyterHubHostUrl("https://my-xnat.com");
    }

    @Test
    public void callImpl_initialized_envSet() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_HOST_URL")).thenReturn("https://my-xnat.com");
        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_API_URL")).thenReturn("https://my-xnat.com/jupyterhub/hub/api");
        when(mockSystemHelper.getEnv("JH_XNAT_SERVICE_TOKEN")).thenReturn("secret-token");
        when(mockSystemHelper.getEnv("JH_XNAT_START_TIMEOUT")).thenReturn("60");
        when(mockSystemHelper.getEnv("JH_XNAT_STOP_TIMEOUT")).thenReturn("60");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_PREFIX")).thenReturn("/data/xnat/archive");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_DOCKER_PREFIX")).thenReturn("/home/andy/xnat-docker-compose/xnat-data/archive");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_SERVER_PREFIX")).thenReturn("/home/andy/xnat-docker-compose/xnat-data/archive");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_PREFIX")).thenReturn("/data/xnat/workspaces");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_DOCKER_PREFIX")).thenReturn("/home/andy/xnat-docker-compose/xnat-data/workspaces");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_SERVER_PREFIX")).thenReturn("/home/andy/xnat-docker-compose/xnat-data/workspaces");
        when(mockSystemHelper.getEnv("JH_XNAT_WORKSPACE_PATH")).thenReturn("/data/xnat/workspaces");
        when(mockSystemHelper.getEnv("JH_XNAT_INACTIVITY_TIMEOUT")).thenReturn("24");
        when(mockSystemHelper.getEnv("JH_XNAT_MAX_SERVER_LIFETIME")).thenReturn("48");
        when(mockSystemHelper.getEnv("JH_XNAT_ALL_USERS_CAN_START_JUPYTER")).thenReturn("true");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences).setJupyterHubHostUrl("https://my-xnat.com");
        verify(mockJupyterHubPreferences).setJupyterHubApiUrl("https://my-xnat.com/jupyterhub/hub/api");
        verify(mockJupyterHubPreferences).setJupyterHubToken("secret-token");
        verify(mockJupyterHubPreferences).setStartTimeout(60);
        verify(mockJupyterHubPreferences).setStopTimeout(60);
        verify(mockJupyterHubPreferences).setPathTranslationArchivePrefix("/data/xnat/archive");
        verify(mockJupyterHubPreferences, times(2)).setPathTranslationArchiveDockerPrefix("/home/andy/xnat-docker-compose/xnat-data/archive");
        verify(mockJupyterHubPreferences).setPathTranslationWorkspacePrefix("/data/xnat/workspaces");
        verify(mockJupyterHubPreferences, times(2)).setPathTranslationWorkspaceDockerPrefix("/home/andy/xnat-docker-compose/xnat-data/workspaces");
        verify(mockJupyterHubPreferences).setWorkspacePath("/data/xnat/workspaces");
        verify(mockJupyterHubPreferences).setInactivityTimeout(24L);
        verify(mockJupyterHubPreferences).setMaxServerLifetime(48L);
    }

    @Test
    public void callImpl_initialized_envNotSet() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_HOST_URL")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_API_URL")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_SERVICE_TOKEN")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_START_TIMEOUT")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_STOP_TIMEOUT")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_DOCKER_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_SERVER_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_DOCKER_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_SERVER_PREFIX")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_WORKSPACE_PATH")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_INACTIVITY_TIMEOUT")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_MAX_SERVER_LIFETIME")).thenReturn("");
        when(mockSystemHelper.getEnv("JH_XNAT_ALL_USERS_CAN_START_JUPYTER")).thenReturn("");

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences, never()).setJupyterHubHostUrl(any());
        verify(mockJupyterHubPreferences, never()).setJupyterHubApiUrl(any());
        verify(mockJupyterHubPreferences, never()).setJupyterHubToken(any());
        verify(mockJupyterHubPreferences, never()).setStartTimeout(anyInt());
        verify(mockJupyterHubPreferences, never()).setStopTimeout(anyInt());
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchivePrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchiveDockerPrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspacePrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspaceDockerPrefix(any());
        verify(mockJupyterHubPreferences, never()).setWorkspacePath(any());
        verify(mockJupyterHubPreferences, never()).setInactivityTimeout(anyLong());
        verify(mockJupyterHubPreferences, never()).setMaxServerLifetime(anyLong());
    }

    @Test
    public void callImpl_initialized_envNotSet2() throws Exception {
        // Setup
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_HOST_URL")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_JUPYTERHUB_API_URL")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_SERVICE_TOKEN")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_START_TIMEOUT")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_STOP_TIMEOUT")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_DOCKER_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_ARCHIVE_SERVER_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_DOCKER_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_PT_WORKSPACE_SERVER_PREFIX")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_WORKSPACE_PATH")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_INACTIVITY_TIMEOUT")).thenReturn(null);
        when(mockSystemHelper.getEnv("JH_XNAT_MAX_SERVER_LIFETIME")).thenReturn(null);

        // Test
        jupyterHubPreferenceInitializer.callImpl();

        // Verify
        verify(mockJupyterHubPreferences, never()).setJupyterHubHostUrl(any());
        verify(mockJupyterHubPreferences, never()).setJupyterHubApiUrl(any());
        verify(mockJupyterHubPreferences, never()).setJupyterHubToken(any());
        verify(mockJupyterHubPreferences, never()).setStartTimeout(anyInt());
        verify(mockJupyterHubPreferences, never()).setStopTimeout(anyInt());
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchivePrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchiveDockerPrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspacePrefix(any());
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspaceDockerPrefix(any());
        verify(mockJupyterHubPreferences, never()).setWorkspacePath(any());
        verify(mockJupyterHubPreferences, never()).setInactivityTimeout(anyLong());
        verify(mockJupyterHubPreferences, never()).setMaxServerLifetime(anyLong());
    }

}