package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultDashboardJobTemplateServiceTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultDashboardJobTemplateServiceTestConfig.class)
public class DefaultDashboardJobTemplateServiceTest {

    @Autowired private ComputeEnvironmentConfigService mockComputeEnvironmentConfigService;
    @Autowired private HardwareConfigService mockHardwareConfigService;
    @Autowired private ConstraintConfigService mockConstraintConfigService;
    @Autowired private DashboardConfigService mockDashboardConfigService;
    @Autowired private DashboardFrameworkService mockDashboardFrameworkService;
    @Autowired private DefaultDashboardJobTemplateService defaultDashboardJobTemplateService;

    @Before
    public void before() {
    }

    @After
    public void after() {
        Mockito.reset(
                mockComputeEnvironmentConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService
        );
    }

    @Test
    public void test_wiring() {
        assertNotNull(mockComputeEnvironmentConfigService);
        assertNotNull(mockHardwareConfigService);
        assertNotNull(mockConstraintConfigService);
        assertNotNull(mockDashboardConfigService);
        assertNotNull(mockDashboardFrameworkService);
        assertNotNull(defaultDashboardJobTemplateService);
    }

    @Test
    public void test_isCompEnvAndHardwareAvailable() {
        // Run
        boolean isAvailable = defaultDashboardJobTemplateService.isAvailable(1L, 1L, null);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void test_DashboardCompEnvAndHardwareUnavailable_1() {
        // Setup
        when(mockDashboardConfigService.isAvailable(any(), any())).thenReturn(false);

        // Run
        boolean isAvailable = defaultDashboardJobTemplateService.isAvailable(1L, 1L, null);

        // Verify
        assertTrue(isAvailable);
    }


    @Test
    public void test_DashboardCompEnvAndHardwareAvailable() {
        // Setup
        DashboardConfig dashboardConfig = DashboardConfig.builder().id(1L).build();

        when(mockDashboardConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockDashboardConfigService.retrieve(1L)).thenReturn(Optional.of(dashboardConfig));


        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().id(1L).build();
        ComputeEnvironment computeEnvironment = ComputeEnvironment.builder()
                                                                  .name("JupyterHub Data Science Notebook")
                                                                  .image("jupyter/datascience-notebook:latest")
                                                                  .build();
        computeEnvironmentConfig.setComputeEnvironment(computeEnvironment);
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        Hardware hardware = Hardware.builder()
                                    .name("Standard")
                                    .cpuLimit(1.0)
                                    .memoryLimit("4G")
                                    .build();
        hardwareConfig.setHardware(hardware);
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));

        Constraint constraint = Constraint.builder()
                                          .key("node.role")
                                          .operator(Constraint.Operator.IN)
                                          .values(new HashSet<>(Arrays.asList("worker")))
                                          .build();

        ConstraintConfig constraintConfig = ConstraintConfig.builder()
                                                            .id(1L)
                                                            .constraint(constraint)
                                                            .build();

        dashboardConfig.setComputeEnvironmentConfig(computeEnvironmentConfig);
        dashboardConfig.setHardwareConfig(hardwareConfig);

        when(mockComputeEnvironmentConfigService.retrieve(1L)).thenReturn(Optional.of(computeEnvironmentConfig));
        when(mockHardwareConfigService.retrieve(1L)).thenReturn(Optional.of(hardwareConfig));
        when(mockConstraintConfigService.retrieve(1L)).thenReturn(Optional.of(constraintConfig));

        // Run
        boolean isAvailable = defaultDashboardJobTemplateService.isAvailable(1L, 1L, 1L, null);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void test_ResolveDashboardConfig() {
        // Setup
        DashboardConfig dashboardConfig = DashboardConfig.builder().id(1L).build();

        when(mockDashboardConfigService.isAvailable(any(), any())).thenReturn(true);
        when(mockDashboardConfigService.retrieve(1L)).thenReturn(Optional.of(dashboardConfig));


        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder().id(1L).build();
        ComputeEnvironment computeEnvironment = ComputeEnvironment.builder()
                                                                  .name("JupyterHub Data Science Notebook")
                                                                  .image("jupyter/datascience-notebook:latest")
                                                                  .build();
        computeEnvironmentConfig.setComputeEnvironment(computeEnvironment);
        ComputeEnvironmentHardwareOptions hardwareOptions = ComputeEnvironmentHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeEnvironmentConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        Hardware hardware = Hardware.builder()
                                    .name("Standard")
                                    .cpuLimit(1.0)
                                    .memoryLimit("4G")
                                    .build();
        hardwareConfig.setHardware(hardware);
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));

        Constraint constraint = Constraint.builder()
                                          .key("node.role")
                                          .operator(Constraint.Operator.IN)
                                          .values(new HashSet<>(Arrays.asList("worker")))
                                          .build();

        ConstraintConfig constraintConfig = ConstraintConfig.builder()
                                                            .id(1L)
                                                            .constraint(constraint)
                                                            .build();

        dashboardConfig.setComputeEnvironmentConfig(computeEnvironmentConfig);
        dashboardConfig.setHardwareConfig(hardwareConfig);

        when(mockComputeEnvironmentConfigService.retrieve(1L)).thenReturn(Optional.of(computeEnvironmentConfig));
        when(mockHardwareConfigService.retrieve(1L)).thenReturn(Optional.of(hardwareConfig));
        when(mockConstraintConfigService.retrieve(1L)).thenReturn(Optional.of(constraintConfig));

        // Run
        JobTemplate jobTemplate = defaultDashboardJobTemplateService.resolve(1L, 1L, 1L, Collections.emptyMap());

    }
}