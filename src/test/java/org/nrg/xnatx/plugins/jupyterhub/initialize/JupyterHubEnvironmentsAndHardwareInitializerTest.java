package org.nrg.xnatx.plugins.jupyterhub.initialize;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.jobtemplates.models.ComputeSpecConfig;
import org.nrg.jobtemplates.models.HardwareConfig;
import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.jobtemplates.services.HardwareConfigService;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.config.JupyterHubEnvironmentsAndHardwareInitializerTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JupyterHubEnvironmentsAndHardwareInitializerTestConfig.class)
public class JupyterHubEnvironmentsAndHardwareInitializerTest {

    @Autowired private JupyterHubEnvironmentsAndHardwareInitializer jupyterHubJobTemplateInitializer;
    @Autowired private XFTManagerHelper mockXFTManagerHelper;
    @Autowired private XnatAppInfo mockXnatAppInfo;
    @Autowired private ComputeSpecConfigService mockComputeSpecConfigService;
    @Autowired private HardwareConfigService mockHardwareConfigService;

    @After
    public void after() {
        Mockito.reset(
                mockXFTManagerHelper,
                mockXnatAppInfo,
                mockComputeSpecConfigService,
                mockHardwareConfigService
        );
    }

    @Test
    public void testGetTaskName() {
        // Test
        String taskName = jupyterHubJobTemplateInitializer.getTaskName();

        // Verify
        assertThat(taskName, notNullValue());
        assertThat(taskName, isA(String.class));
    }

    @Test(expected = InitializingTaskException.class)
    public void testCallImpl_XFTManagerNotInitialized() throws Exception {
        // XFT not initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(false);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Should throw InitializingTaskException
        jupyterHubJobTemplateInitializer.callImpl();
    }

    @Test(expected = InitializingTaskException.class)
    public void testCallImpl_AppNotInitialized() throws Exception {
        // XFT initialized but app not initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(false);

        // Should throw InitializingTaskException
        jupyterHubJobTemplateInitializer.callImpl();
    }

    @Test
    public void testCallImpl_ConfigsAlreadyExist() {
        // XNAT is initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);


        // Setup mock
        when(mockComputeSpecConfigService.getByType(any()))
                .thenReturn(Collections.singletonList(new ComputeSpecConfig()));
        when(mockHardwareConfigService.retrieveAll())
                .thenReturn(Collections.singletonList(new HardwareConfig()));

        // Test
        try {
            jupyterHubJobTemplateInitializer.callImpl();
        } catch (InitializingTaskException e) {
            fail("Should not throw InitializingTaskException");
        }

        // Verify
        verify(mockComputeSpecConfigService, never()).create(any());
        verify(mockHardwareConfigService, never()).create(any());
    }

    @Test
    public void testCallImpl() {
        // XNAT is initialized
        when(mockXFTManagerHelper.isInitialized()).thenReturn(true);
        when(mockXnatAppInfo.isInitialized()).thenReturn(true);

        // Setup mock
        when(mockComputeSpecConfigService.getByType(any()))
                .thenReturn(Collections.emptyList());
        when(mockHardwareConfigService.retrieveAll())
                .thenReturn(Collections.emptyList());

        // Test
        try {
            jupyterHubJobTemplateInitializer.callImpl();
        } catch (InitializingTaskException e) {
            fail("Should not throw InitializingTaskException");
        }

        // Verify
        verify(mockComputeSpecConfigService, atLeast(1)).create(any());
        verify(mockHardwareConfigService, atLeast(1)).create(any());
    }

}