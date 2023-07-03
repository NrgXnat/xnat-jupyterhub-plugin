package org.nrg.xnatx.plugins.jupyterhub.preferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xnatx.plugins.jupyterhub.config.MockConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MockConfig.class)
public class JupyterHubPreferencesTest {

    @Autowired private NrgPreferenceService mockNrgPreferenceService;
    @Autowired private ConfigPaths mockConfigPaths;
    @Autowired private OrderedProperties mockOrderedProperties;

    private JupyterHubPreferences jupyterHubPreferences;

    @Before
    public void before() {
        assertNotNull(mockNrgPreferenceService);
        assertNotNull(mockConfigPaths);
        assertNotNull(mockOrderedProperties);
        jupyterHubPreferences = new JupyterHubPreferences(mockNrgPreferenceService, mockConfigPaths, mockOrderedProperties);
    }

    @After
    public void after() {
        Mockito.reset(
            mockNrgPreferenceService,
            mockConfigPaths,
            mockOrderedProperties
        );
    }

    @Test
    public void testGetPathTranslationArchivePrefix() {
        jupyterHubPreferences.getPathTranslationArchivePrefix();
    }

    @Test
    public void testGetPathTranslationArchiveDockerPrefix() {
        jupyterHubPreferences.getPathTranslationArchiveDockerPrefix();
    }

    @Test
    public void testGetPathTranslationWorkspacePrefix() {
        jupyterHubPreferences.getPathTranslationWorkspacePrefix();
    }

    @Test
    public void testGetPathTranslationWorkspaceDockerPrefix() {
        jupyterHubPreferences.getPathTranslationWorkspaceDockerPrefix();
    }

}