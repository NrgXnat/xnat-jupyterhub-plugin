package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.config.DashboardFrameworkInitializerTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DashboardFrameworkInitializerTestConfig.class)
public class DashboardFrameworkInitializerTest {

    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private DashboardFrameworkService mockDashboardFrameworkService;
    @Autowired private DashboardFrameworkInitializer initializer;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockDashboardFrameworkService
        );
    }

    @Test
    public void test_wiring() {
        assertNotNull(mockXFTManagerHelper);
        assertNotNull(mockXnatAppInfo);
        assertNotNull(mockDashboardFrameworkService);
    }

    @Test
    public void test_getTaskName() {
        // Execute
        final String taskName = initializer.getTaskName();

        // Verify
        assertThat(taskName, notNullValue());
        assertThat(taskName, isA(String.class));
    }

    @Test(expected = InitializingTaskException.class)
    public void test_callImpl_XFTManagerHelperNotInitialized() throws InitializingTaskException {
        // Set up
        Mockito.when(mockXFTManagerHelper.isInitialized()).thenReturn(false);

        // Execute
        initializer.callImpl();
    }

    @Test(expected = InitializingTaskException.class)
    public void test_callImpl_XnatAppInfoNotInitialized() throws InitializingTaskException {
        // Set up
        Mockito.when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        Mockito.when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Execute
        initializer.callImpl();
    }

    @Test
    public void test_callImpl() throws InitializingTaskException {
        // Set up
        Mockito.when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        Mockito.when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Execute
        initializer.callImpl();

        // Verify
        verify(mockDashboardFrameworkService, times(4)).create(any(DashboardFramework.class));
    }

    @Test
    public void test_callImpl_dashboardFrameworkAlreadyExists() throws InitializingTaskException {
        // Set up
        Mockito.when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        Mockito.when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        Mockito.when(mockDashboardFrameworkService.get("Panel")).thenReturn(Optional.of(DashboardFramework.builder().name("Panel").build()));
        Mockito.when(mockDashboardFrameworkService.get("Streamlit")).thenReturn(Optional.of(DashboardFramework.builder().name("Streamlit").build()));
        Mockito.when(mockDashboardFrameworkService.get("Voila")).thenReturn(Optional.of(DashboardFramework.builder().name("Voila").build()));
        Mockito.when(mockDashboardFrameworkService.get("Dash")).thenReturn(Optional.of(DashboardFramework.builder().name("Dash").build()));

        // Execute
        initializer.callImpl();

        // Verify
        verify(mockDashboardFrameworkService, never()).create(any(DashboardFramework.class));
    }

}