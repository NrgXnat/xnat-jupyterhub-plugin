package org.nrg.jobtemplates.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.jobtemplates.config.DefaultJobTemplateServiceTestConfig;
import org.nrg.jobtemplates.models.*;
import org.nrg.jobtemplates.services.ComputeSpecConfigService;
import org.nrg.jobtemplates.services.ConstraintConfigService;
import org.nrg.jobtemplates.services.HardwareConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultJobTemplateServiceTestConfig.class)
public class DefaultJobTemplateServiceTest {

    @Autowired private DefaultJobTemplateService jobTemplateService;
    @Autowired private ComputeSpecConfigService mockComputeSpecConfigService;
    @Autowired private HardwareConfigService mockHardwareConfigService;
    @Autowired private ConstraintConfigService mockConstraintConfigService;

    @Before
    public void before() {
    }

    @After
    public void after() {
        Mockito.reset(
                mockComputeSpecConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService
        );
    }

    @Test
    public void testIsAvailable_ComputeSpecNotAvailable() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(false);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(true);

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 1L);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_HardwareNotAvailable() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(false);

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 1L);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeSpecAndHardwareNotAvailable() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(false);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(false);

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 1L);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeSpecConfigAllHardwareIsAvailable() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(true);

        ComputeSpecConfig computeSpecConfig = ComputeSpecConfig.builder().build();
        ComputeSpecHardwareOptions hardwareOptions = ComputeSpecHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(true);
        computeSpecConfig.setHardwareOptions(hardwareOptions);
        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 1L);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeSpecConfigSpecificHardwareAllowed() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(true);

        ComputeSpecConfig computeSpecConfig = ComputeSpecConfig.builder().id(1L).build();
        ComputeSpecHardwareOptions hardwareOptions = ComputeSpecHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeSpecConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));
        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 1L);

        // Verify
        assertTrue(isAvailable);
    }

    @Test
    public void testIsAvailable_ComputeSpecConfigSpecificHardwareNotAllowed() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(true);

        ComputeSpecConfig computeSpecConfig = ComputeSpecConfig.builder().id(1L).build();
        ComputeSpecHardwareOptions hardwareOptions = ComputeSpecHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeSpecConfig.setHardwareOptions(hardwareOptions);
        HardwareConfig hardwareConfig = HardwareConfig.builder().id(1L).build();
        hardwareOptions.setHardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig)));
        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));

        // Run
        boolean isAvailable = jobTemplateService.isAvailable("user", "project", 1L, 2L);

        // Verify
        assertFalse(isAvailable);
    }

    @Test
    public void testResolve() {
        // Setup
        when(mockComputeSpecConfigService.isAvailable(any(), any(), any())).thenReturn(true);
        when(mockHardwareConfigService.isAvailable(any(), any(), any())).thenReturn(true);

        ComputeSpecConfig computeSpecConfig = ComputeSpecConfig.builder().id(1L).build();
        ComputeSpec computeSpec = ComputeSpec.builder()
                .name("JupyterHub Data Science Notebook")
                .image("jupyter/datascience-notebook:latest")
                .build();
        computeSpecConfig.setComputeSpec(computeSpec);
        ComputeSpecHardwareOptions hardwareOptions = ComputeSpecHardwareOptions.builder().build();
        hardwareOptions.setAllowAllHardware(false);
        computeSpecConfig.setHardwareOptions(hardwareOptions);
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

        when(mockComputeSpecConfigService.retrieve(any())).thenReturn(Optional.of(computeSpecConfig));
        when(mockHardwareConfigService.retrieve(any())).thenReturn(Optional.of(hardwareConfig));
        when(mockConstraintConfigService.getAvailable(any())).thenReturn(Collections.singletonList(constraintConfig));

        // Run
        JobTemplate jobTemplate = jobTemplateService.resolve("user", "project", 1L, 1L);

        // Verify
        assertNotNull(jobTemplate);
        assertEquals(computeSpecConfig.getComputeSpec(), jobTemplate.getComputeSpec());
        assertEquals(hardware, jobTemplate.getHardware());
        assertEquals(Collections.singletonList(constraint), jobTemplate.getConstraints());
    }


}