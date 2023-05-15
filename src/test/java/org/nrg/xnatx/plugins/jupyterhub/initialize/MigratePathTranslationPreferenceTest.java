package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.config.MigratePathTranslationPreferenceTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MigratePathTranslationPreferenceTestConfig.class)
public class MigratePathTranslationPreferenceTest {

    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private NrgPreferenceService mockNrgPreferenceService;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;
    @Autowired private MigratePathTranslationPreference migratePathTranslationPreference;

    @Before
    public void before() {
        assertNotNull(mockXFTManagerHelper);
        assertNotNull(mockXnatAppInfo);
        assertNotNull(mockNrgPreferenceService);
        assertNotNull(mockJupyterHubPreferences);
        assertNotNull(migratePathTranslationPreference);
    }

    @After
    public void after() {
        resetMocks();
    }

    private void resetMocks() {
        Mockito.reset(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockNrgPreferenceService,
                mockJupyterHubPreferences
        );
    }

    @Test
    public void testGetTaskName() {
        // Test that the task name is not null
        final String taskName = migratePathTranslationPreference.getTaskName();
        assertNotNull(taskName);
    }

    @Test
    public void testCallImpl_XnatInitialized_NoExceptions() throws Exception {
        // XFT initialized and app initialized, we should not throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(new Properties());

        // Should not throw InitializingTaskException
        migratePathTranslationPreference.callImpl();
    }

    @Test(expected = InitializingTaskException.class)
    public void testCallImpl_XnatNotInitialized_Exception1() throws Exception {
        // XFT not initialized, we should throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);
        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(new Properties());

        // Should throw InitializingTaskException
        migratePathTranslationPreference.callImpl();
    }

    @Test(expected = InitializingTaskException.class)
    public void testCallImpl_XnatNotInitialized_Exception2() throws Exception {
        // XFT initialized, app not initialized, we should throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);
        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(new Properties());

        // Should throw InitializingTaskException
        migratePathTranslationPreference.callImpl();
    }

    @Test
    public void testCallImpl_MigrateOldPreferences() throws Exception {
        // XFT initialized and app initialized, we should not throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        Properties properties = new Properties();
        properties.setProperty("pathTranslationXnatPrefix", "/data/pixi/archive");
        properties.setProperty("pathTranslationDockerPrefix", "/data/xnat/archive");

        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(properties);

        // Should not throw InitializingTaskException
        migratePathTranslationPreference.callImpl();

        // Verify migration
        verify(mockJupyterHubPreferences).setPathTranslationArchivePrefix("/data/pixi/archive");
        verify(mockJupyterHubPreferences).setPathTranslationWorkspacePrefix("/data/pixi/archive");
        verify(mockJupyterHubPreferences).setPathTranslationArchiveDockerPrefix("/data/xnat/archive");
        verify(mockJupyterHubPreferences).setPathTranslationWorkspaceDockerPrefix("/data/xnat/archive");

        // Verify deletion of old preferences
        verify(mockNrgPreferenceService).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationXnatPrefix");
        verify(mockNrgPreferenceService).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationDockerPrefix");
    }

    @Test
    public void testCallImpl_DontMigrateEmptyPreferences() throws Exception {
        // XFT initialized and app initialized, we should not throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Test with blank values
        Properties properties = new Properties();
        properties.setProperty("pathTranslationXnatPrefix", "");
        properties.setProperty("pathTranslationDockerPrefix", "");

        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(properties);

        // Should not throw InitializingTaskException
        migratePathTranslationPreference.callImpl();

        // Verify migration did not occur
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchivePrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspacePrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchiveDockerPrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspaceDockerPrefix("");

        // Verify deletion of old preferences did occur
        verify(mockNrgPreferenceService).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationXnatPrefix");
        verify(mockNrgPreferenceService).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationDockerPrefix");
    }

    @Test
    public void testCallImpl_DontMigrateEmptyPreferences2() throws Exception {
        // XFT initialized and app initialized, we should not throw an exception
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Test with blank values
        Properties properties = new Properties();
        properties.setProperty("pathTranslationWorkspacePrefix", "/data/pixi/workspaces");
        properties.setProperty("pathTranslationWorkspaceDockerPrefix", "/data/xnat/workspaces");
        properties.setProperty("resourceSpecCpuLimit", "1");

        when(mockNrgPreferenceService.getToolProperties(any())).thenReturn(properties);

        // Should not throw InitializingTaskException
        migratePathTranslationPreference.callImpl();

        // Verify migration never called
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchivePrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspacePrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationArchiveDockerPrefix("");
        verify(mockJupyterHubPreferences, never()).setPathTranslationWorkspaceDockerPrefix("");

        // Verify deletion of old preferences never called
        verify(mockNrgPreferenceService, never()).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationXnatPrefix");
        verify(mockNrgPreferenceService, never()).deletePreference(JupyterHubPreferences.TOOL_ID, "pathTranslationDockerPrefix");
    }
}