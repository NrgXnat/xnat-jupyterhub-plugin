package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultUserOptionsServiceConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultUserOptionsServiceConfig.class)
public class DefaultUserOptionsServiceTest {

    @Autowired private DefaultUserOptionsService userOptionsService;
    @Autowired private JupyterHubPreferences mockJupyterHubPreferences;

    @After
    public void after() {
        Mockito.reset(
                mockJupyterHubPreferences
        );
    }

    @Test
    public void testPathTranslationArchive() {
        // Setup mocks
        when(mockJupyterHubPreferences.getPathTranslationArchivePrefix()).thenReturn("/data/xnat/archive");
        when(mockJupyterHubPreferences.getPathTranslationArchiveDockerPrefix()).thenReturn("/docker/data/xnat/archive");
        when(mockJupyterHubPreferences.getPathTranslationWorkspacePrefix()).thenReturn("/data/xnat/workspaces");
        when(mockJupyterHubPreferences.getPathTranslationWorkspaceDockerPrefix()).thenReturn("/docker/data/xnat/workspaces");

        // Test archive path translation
        String translated = userOptionsService.translateArchivePath("/data/xnat/archive/Project1");

        // Verify archive path translation
        assertThat(translated, is(("/docker/data/xnat/archive/Project1")));
    }

    @Test
    public void testPathTranslationWorkspace() {
        // Setup mocks
        when(mockJupyterHubPreferences.getPathTranslationArchivePrefix()).thenReturn("/data/xnat/archive");
        when(mockJupyterHubPreferences.getPathTranslationArchiveDockerPrefix()).thenReturn("/docker/data/xnat/archive");
        when(mockJupyterHubPreferences.getPathTranslationWorkspacePrefix()).thenReturn("/data/xnat/workspaces");
        when(mockJupyterHubPreferences.getPathTranslationWorkspaceDockerPrefix()).thenReturn("/docker/data/xnat/workspaces");

        // Test workspace path translation
        String translated = userOptionsService.translateWorkspacePath("/data/xnat/workspaces/users/andy");

        // Verify workspace path translation
        assertThat(translated, is(("/docker/data/xnat/workspaces/users/andy")));
    }

    @Test
    public void testToTaskTemplate() {
        // Setup hardware
        Hardware hardware1 = Hardware.builder()
                .name("Small")
                .cpuReservation(2.0)
                .cpuLimit(4.0)
                .memoryReservation("4G")
                .memoryLimit("8G")
                .build();

        // Setup hardware constraints
        Constraint hardwareConstraint1 = Constraint.builder()
                .key("node.hardware")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Collections.singletonList("gpu")))
                .build();

        Constraint hardwareConstraint2 = Constraint.builder()
                .key("node.labels")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Collections.singletonList("special")))
                .build();

        hardware1.setConstraints(Arrays.asList(hardwareConstraint1, hardwareConstraint2));

        // Setup hardware environment variables
        EnvironmentVariable hardwareEnvironmentVariable1 = new EnvironmentVariable("MATLAB_LICENSE_FILE", "12345@myserver");
        EnvironmentVariable hardwareEnvironmentVariable2 = new EnvironmentVariable("NVIDIA_VISIBLE_DEVICES", "all");

        hardware1.setEnvironmentVariables(Arrays.asList(hardwareEnvironmentVariable1, hardwareEnvironmentVariable2));

        // Setup hardware generic resources
        GenericResource hardwareGenericResource1 = new GenericResource("nvidia.com/gpu", "2");
        GenericResource hardwareGenericResource2 = new GenericResource("fpga.com/fpga", "1");

        hardware1.setGenericResources(Arrays.asList(hardwareGenericResource1, hardwareGenericResource2));

        // Setup Mount
        Mount mount1 = Mount.builder()
                .localPath("/data/xnat/archive/Project1")
                .containerPath("/data/xnat/archive/Project1")
                .readOnly(true)
                .build();

        Mount mount2 = Mount.builder()
                .localPath("/data/xnat/workspaces/users/andy")
                .containerPath("/workspaces/users/andy")
                .readOnly(false)
                .build();

        // Setup first compute spec
        ComputeSpec computeSpec1 = ComputeSpec.builder()
                .name("Jupyter Datascience Notebook")
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .environmentVariables(Collections.singletonList(new EnvironmentVariable("COMPUTE_SPEC", "1")))
                .mounts(Arrays.asList(mount1, mount2))
                .build();

        Constraint constraint1 = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("worker")))
                .build();

        Constraint constraint2 = Constraint.builder()
                .key("node.labels.project")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("ProjectA", "ProjectB")))
                .build();

        Constraint constraint3 = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        // Now create the job template
        JobTemplate template = JobTemplate.builder()
                .computeSpec(computeSpec1)
                .hardware(hardware1)
                .constraints(Arrays.asList(constraint1, constraint2, constraint3))
                .build();

        // Run the test
        TaskTemplate result = userOptionsService.toTaskTemplate(template);

        // Verify the results
        assertEquals(computeSpec1.getImage(), result.getContainerSpec().getImage());

        // Verify the environment variables from the hardware and compute spec are merged
        Map<String, String> expectedEnv = new HashMap<>();
        expectedEnv.put("COMPUTE_SPEC", "1");
        expectedEnv.put("MATLAB_LICENSE_FILE", "12345@myserver");
        expectedEnv.put("NVIDIA_VISIBLE_DEVICES", "all");
        assertEquals(expectedEnv, result.getContainerSpec().getEnv());

        // Verify the mounts from the hardware and compute spec are merged
        assertEquals(2, result.getContainerSpec().getMounts().size());
        assertEquals("/data/xnat/archive/Project1", result.getContainerSpec().getMounts().get(0).getSource());
        assertEquals("/data/xnat/archive/Project1", result.getContainerSpec().getMounts().get(0).getTarget());
        assertEquals(true, result.getContainerSpec().getMounts().get(0).isReadOnly());
        assertEquals("/data/xnat/workspaces/users/andy", result.getContainerSpec().getMounts().get(1).getSource());
        assertEquals("/workspaces/users/andy", result.getContainerSpec().getMounts().get(1).getTarget());
        assertEquals(false, result.getContainerSpec().getMounts().get(1).isReadOnly());

        // Check resources
        assertEquals(hardware1.getCpuLimit(), result.getResources().getCpuLimit());
        assertEquals(hardware1.getCpuReservation(), result.getResources().getCpuReservation());
        assertEquals(hardware1.getMemoryLimit(), result.getResources().getMemLimit());
        assertEquals(hardware1.getMemoryReservation(), result.getResources().getMemReservation());
        assertEquals(2, result.getResources().getGenericResources().size());
        assertTrue(result.getResources().getGenericResources().entrySet().stream().anyMatch(e -> e.getKey().equals("nvidia.com/gpu") && e.getValue().equals("2")));
        assertTrue(result.getResources().getGenericResources().entrySet().stream().anyMatch(e -> e.getKey().equals("fpga.com/fpga") && e.getValue().equals("1")));

        // Check constraints
        assertEquals(7, result.getPlacement().getConstraints().size());
        assertTrue(result.getPlacement()
                           .getConstraints().stream()
                           .map(StringUtils::deleteWhitespace)
                           .anyMatch(c -> c.equals("node.role==worker")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.labels.project==ProjectA")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.labels.project==ProjectB")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.instance.type!=spot")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.instance.type!=demand")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.hardware==gpu")));
        assertTrue(result.getPlacement()
                            .getConstraints().stream()
                            .map(StringUtils::deleteWhitespace)
                            .anyMatch(c -> c.equals("node.labels!=special")));
    }



}