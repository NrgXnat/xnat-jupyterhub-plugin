package org.nrg.jobtemplates.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.jobtemplates.config.DefaultComputeSpecConfigServiceTestConfig;
import org.nrg.jobtemplates.entities.HardwareConfigEntity;
import org.nrg.jobtemplates.models.*;
import org.nrg.jobtemplates.services.HardwareConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.jobtemplates.models.ComputeSpecConfig.ConfigType.CONTAINER_SERVICE;
import static org.nrg.jobtemplates.models.ComputeSpecConfig.ConfigType.JUPYTERHUB;
import static org.nrg.jobtemplates.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = DefaultComputeSpecConfigServiceTestConfig.class)
public class DefaultComputeSpecConfigServiceTest {

    @Autowired private DefaultComputeSpecConfigService defaultComputeSpecConfigService;
    @Autowired private HardwareConfigEntityService hardwareConfigEntityService;

    private ComputeSpecConfig computeSpecConfig1;
    private ComputeSpecConfig computeSpecConfig2;
    private ComputeSpecConfig computeSpecConfig3;
    private ComputeSpecConfig computeSpecConfigInvalid;

    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;

    @Before
    public void before() {
        createDummyConfigs();
    }

    @Test
    @DirtiesContext
    public void testExists() {
        // Test
        ComputeSpecConfig created = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        boolean exists = defaultComputeSpecConfigService.exists(created.getId());

        // Verify
        assertTrue(exists);
    }

    @Test
    @DirtiesContext
    public void testDoesNotExist() {
        // Test
        ComputeSpecConfig created = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        boolean exists = defaultComputeSpecConfigService.exists(created.getId() + 1);

        // Verify
        assertFalse(exists);
    }

    @Test
    @DirtiesContext
    public void testGetDoesNotExist() {
        // Test
        Optional<ComputeSpecConfig> computeSpecConfig = defaultComputeSpecConfigService.retrieve(1L);

        // Verify
        assertFalse(computeSpecConfig.isPresent());
    }

    @Test
    @DirtiesContext
    public void testGet() {
        // Test
        ComputeSpecConfig created = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        Optional<ComputeSpecConfig> computeSpecConfig = defaultComputeSpecConfigService.retrieve(created.getId());

        // Verify
        assertTrue(computeSpecConfig.isPresent());
        assertEquals(created, computeSpecConfig.get());

        assertEquals(computeSpecConfig1.getComputeSpec(), computeSpecConfig.get().getComputeSpec());
        assertEquals(computeSpecConfig1.getConfigTypes(), computeSpecConfig.get().getConfigTypes());
        assertEquals(computeSpecConfig1.getScopes(), computeSpecConfig.get().getScopes());
    }

    @Test
    @DirtiesContext
    public void testGetAll() {
        // Test
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getAll();

        // Verify
        assertThat(computeSpecConfigs.size(), is(3));
        assertThat(computeSpecConfigs, hasItems(created1, created2, created3));
    }

    @Test
    @DirtiesContext
    public void testGetByType() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getByType(CONTAINER_SERVICE);

        // Verify
        assertThat(computeSpecConfigs.size(), is(2));
        assertThat(computeSpecConfigs, hasItems(created2, created3));

        // Test
        computeSpecConfigs = defaultComputeSpecConfigService.getByType(JUPYTERHUB);

        // Verify
        assertEquals(2, computeSpecConfigs.size());
        assertThat(computeSpecConfigs, containsInAnyOrder(created1, created3));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_WrongUser() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getAvailable("User2", "Project1", null);

        // Verify
        assertThat(computeSpecConfigs.size(), is(1));
        assertThat(computeSpecConfigs, hasItems(created1));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_WrongProject() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getAvailable("User1", "Project2");

        // Verify
        assertThat(computeSpecConfigs.size(), is(1));
        assertThat(computeSpecConfigs, hasItem(created1));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getAvailable("User1", "Project1");

        // Verify
        assertThat(computeSpecConfigs.size(), is(2));
        assertThat(computeSpecConfigs, hasItems(created1, created2));
    }

    @Test
    @DirtiesContext
    public void testGetAvailable_SpecificType() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        List<ComputeSpecConfig> computeSpecConfigs = defaultComputeSpecConfigService.getAvailable("User1", "Project1", CONTAINER_SERVICE);

        // Verify
        assertThat(computeSpecConfigs.size(), is(1));
        assertThat(computeSpecConfigs, hasItems(created2));
    }

    @Test
    @DirtiesContext
    public void testIsAvailable() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        boolean result = defaultComputeSpecConfigService.isAvailable("User1", "Project1", created1.getId());

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User1", "Project1", created2.getId());

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User1", "Project1", created3.getId());

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testNotAvailable_WrongUser() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        boolean result = defaultComputeSpecConfigService.isAvailable("User2", "Project1", created1.getId());

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User2", "Project1", created2.getId());

        // Verify
        assertFalse(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User2", "Project1", created3.getId());

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testNotAvailable_WrongProject() {
        // Create ComputeSpecConfigs
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();
        ComputeSpecConfig created3 = defaultComputeSpecConfigService.create(computeSpecConfig3);
        commitTransaction();

        // Test
        boolean result = defaultComputeSpecConfigService.isAvailable("User1", "Project2", created1.getId());

        // Verify
        assertTrue(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User1", "Project2", created2.getId());

        // Verify
        assertFalse(result);

        // Test
        result = defaultComputeSpecConfigService.isAvailable("User1", "Project2", created3.getId());

        // Verify
        assertFalse(result);
    }

    @Test
    @DirtiesContext
    public void testCreate_AllowAllHardware() {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute spec config
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute spec config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));
    }

    @Test
    @DirtiesContext
    public void testCreate_SelectHardware() {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute spec config
        ComputeSpecConfig created2 = defaultComputeSpecConfigService.create(computeSpecConfig2);
        commitTransaction();

        // Verify that only the selected hardware config is associated with the compute spec config
        assertThat(created2.getHardwareOptions().getHardwareConfigs().size(), is(1));
        assertThat(created2.getHardwareOptions().getHardwareConfigs(), hasItem(hardwareConfig2));
    }

    @Test
    @DirtiesContext
    public void testUpdate() throws NotFoundException {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute spec config
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute spec config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));

        // Update the compute spec config
        created1.getHardwareOptions().setAllowAllHardware(false);
        created1.getHardwareOptions().getHardwareConfigs().remove(hardwareConfig1);

        // Update other fields
        created1.getComputeSpec().setName("UpdatedName");
        created1.getComputeSpec().setImage("UpdatedImage");
        created1.getComputeSpec().getEnvironmentVariables().add(new EnvironmentVariable("UpdatedKey", "UpdatedValue"));

        // Update the compute spec config
        defaultComputeSpecConfigService.update(created1);
        commitTransaction();

        // Verify that only the selected hardware config is associated with the compute spec config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(1));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItem(hardwareConfig2));

        // Verify that the other fields were updated
        assertThat(created1.getComputeSpec().getName(), is("UpdatedName"));
        assertThat(created1.getComputeSpec().getImage(), is("UpdatedImage"));
        assertThat(created1.getComputeSpec().getEnvironmentVariables(), hasItem(new EnvironmentVariable("UpdatedKey", "UpdatedValue")));
    }

    @Test
    @DirtiesContext
    public void testDelete() throws Exception {
        // First create hardware configs
        HardwareConfigEntity hardwareConfigEntity1 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig1));
        HardwareConfigEntity hardwareConfigEntity2 = hardwareConfigEntityService.create(HardwareConfigEntity.fromPojo(hardwareConfig2));
        commitTransaction();

        hardwareConfig1.setId(hardwareConfigEntity1.getId());
        hardwareConfig2.setId(hardwareConfigEntity2.getId());

        // Next create compute spec config
        ComputeSpecConfig created1 = defaultComputeSpecConfigService.create(computeSpecConfig1);
        commitTransaction();

        // Verify that all hardware configs are associated with the compute spec config
        assertThat(created1.getHardwareOptions().getHardwareConfigs().size(), is(2));
        assertThat(created1.getHardwareOptions().getHardwareConfigs(), hasItems(hardwareConfig1, hardwareConfig2));

        hardwareConfigEntity1 = hardwareConfigEntityService.retrieve(hardwareConfigEntity1.getId());
        hardwareConfigEntity2 = hardwareConfigEntityService.retrieve(hardwareConfigEntity2.getId());

        assertThat(hardwareConfigEntity1.getComputeSpecHardwareOptions().size(), is(1));
        assertThat(hardwareConfigEntity2.getComputeSpecHardwareOptions().size(), is(1));

        // Delete the compute spec config
        defaultComputeSpecConfigService.delete(created1.getId());
        commitTransaction();

        // Verify that the compute spec config was deleted
        assertThat(defaultComputeSpecConfigService.exists(created1.getId()), is(false));

        // Verify that the hardware config entities were deleted
        hardwareConfigEntity1 = hardwareConfigEntityService.retrieve(hardwareConfigEntity1.getId());
        hardwareConfigEntity2 = hardwareConfigEntityService.retrieve(hardwareConfigEntity2.getId());

        assertThat(hardwareConfigEntity1.getComputeSpecHardwareOptions().size(), is(0));
        assertThat(hardwareConfigEntity2.getComputeSpecHardwareOptions().size(), is(0));
    }

    @Test
    public void testValidate() {
        try {
            defaultComputeSpecConfigService.validate(computeSpecConfigInvalid);
            fail("Expected exception to be thrown");
        } catch (IllegalArgumentException e) {
            // Verify that the exception message contains the expected validation errors
            // Note: the order of the validation errors is not guaranteed
            // Trying not to be too brittle here
            assertThat(e.getMessage(), containsString("name cannot be blank"));
            assertThat(e.getMessage(), containsString("image cannot be blank"));
            assertThat(e.getMessage(), containsString("must have at least one scope"));
            assertThat(e.getMessage(), containsString("hardware configs cannot be null"));
            assertThat(e.getMessage(), containsString("must have at least one config type"));
        }
    }

    public void createDummyConfigs() {
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
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Collections.singletonList("worker")))
                .build();

        Constraint hardwareConstraint2 = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
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

        // Setup hardware scopes
        HardwareScope hardwareSiteScope1 = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope1 = HardwareScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope userHardwareScope1 = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes1 = new HashMap<>();
        hardwareScopes1.put(Site, hardwareSiteScope1);
        hardwareScopes1.put(Project, hardwareProjectScope1);
        hardwareScopes1.put(User, userHardwareScope1);

        // Setup a hardware config entity
        hardwareConfig1 = HardwareConfig.builder()
                .hardware(hardware1)
                .scopes(hardwareScopes1)
                .build();

        // Setup second hardware config
        Hardware hardware2 = Hardware.builder()
                .name("Medium")
                .cpuReservation(4.0)
                .cpuLimit(4.0)
                .memoryReservation("8G")
                .memoryLimit("8G")
                .build();

        // Setup hardware constraints
        Constraint hardwareConstraint3 = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Collections.singletonList("worker")))
                .build();

        Constraint hardwareConstraint4 = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        hardware2.setConstraints(Arrays.asList(hardwareConstraint3, hardwareConstraint4));

        // Setup hardware environment variables
        EnvironmentVariable hardwareEnvironmentVariable3 = new EnvironmentVariable("MATLAB_LICENSE_FILE", "12345@myserver");
        EnvironmentVariable hardwareEnvironmentVariable4 = new EnvironmentVariable("NVIDIA_VISIBLE_DEVICES", "all");

        hardware2.setEnvironmentVariables(Arrays.asList(hardwareEnvironmentVariable3, hardwareEnvironmentVariable4));

        // Setup hardware generic resources
        GenericResource hardwareGenericResource3 = new GenericResource("nvidia.com/gpu", "2");
        GenericResource hardwareGenericResource4 = new GenericResource("fpga.com/fpga", "1");

        hardware2.setGenericResources(Arrays.asList(hardwareGenericResource3, hardwareGenericResource4));

        // Setup hardware scopes
        HardwareScope hardwareSiteScope2 = HardwareScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope hardwareProjectScope2 = HardwareScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        HardwareScope userHardwareScope2 = HardwareScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, HardwareScope> hardwareScopes2 = new HashMap<>();
        hardwareScopes2.put(Site, hardwareSiteScope2);
        hardwareScopes2.put(Project, hardwareProjectScope2);
        hardwareScopes2.put(User, userHardwareScope2);

        // Setup second hardware config entity
        hardwareConfig2 = HardwareConfig.builder()
                .hardware(hardware2)
                .scopes(hardwareScopes2)
                .build();

        // Setup first compute spec
        ComputeSpec computeSpec1 = ComputeSpec.builder()
                .name("Jupyter Datascience Notebook")
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeSpecScope computeSpecSiteScope1 = ComputeSpecScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeSpecScope computeSpecProjectScope1 = ComputeSpecScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeSpecScope computeSpecUserScope1 = ComputeSpecScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        Map<Scope, ComputeSpecScope> computeSpecScopes1 = new HashMap<>();
        computeSpecScopes1.put(Site, computeSpecSiteScope1);
        computeSpecScopes1.put(Project, computeSpecProjectScope1);
        computeSpecScopes1.put(User, computeSpecUserScope1);

        ComputeSpecHardwareOptions computeSpecHardwareOptions1 = ComputeSpecHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig1, hardwareConfig2)))
                .build();

        computeSpecConfig1 = ComputeSpecConfig.builder()
                .configTypes(new HashSet<>(Collections.singletonList(ComputeSpecConfig.ConfigType.JUPYTERHUB)))
                .computeSpec(computeSpec1)
                .scopes(computeSpecScopes1)
                .hardwareOptions(computeSpecHardwareOptions1)
                .build();

        // Setup second compute spec
        ComputeSpec computeSpec2 = ComputeSpec.builder()
                .name("XNAT Datascience Notebook")
                .image("xnat/datascience-notebook:latest")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeSpecScope computeSpecSiteScope2 = ComputeSpecScope.builder()
                .scope(Site)
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeSpecScope computeSpecProjectScope2 = ComputeSpecScope.builder()
                .scope(Project)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("Project1")))
                .build();

        ComputeSpecScope computeSpecUserScope2 = ComputeSpecScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("User1")))
                .build();

        Map<Scope, ComputeSpecScope> computeSpecScopes2 = new HashMap<>();
        computeSpecScopes2.put(Site, computeSpecSiteScope2);
        computeSpecScopes2.put(Project, computeSpecProjectScope2);
        computeSpecScopes2.put(User, computeSpecUserScope2);

        ComputeSpecHardwareOptions computeSpecHardwareOptions2 = ComputeSpecHardwareOptions.builder()
                .allowAllHardware(false)
                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig2)))
                .build();

        computeSpecConfig2 = ComputeSpecConfig.builder()
                .id(2L)
                .configTypes(new HashSet<>(Collections.singletonList(CONTAINER_SERVICE)))
                .computeSpec(computeSpec2)
                .scopes(computeSpecScopes2)
                .hardwareOptions(computeSpecHardwareOptions2)
                .build();

        // Setup third compute spec
        ComputeSpec computeSpec3 = ComputeSpec.builder()
                .name("MATLAB Datascience Notebook")
                .image("matlab/datascience-notebook:latest")
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        ComputeSpecScope computeSpecSiteScope3 = ComputeSpecScope.builder()
                .scope(Site)
                .enabled(false)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeSpecScope computeSpecProjectScope3 = ComputeSpecScope.builder()
                .scope(Project)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ComputeSpecScope computeSpecUserScope3 = ComputeSpecScope.builder()
                .scope(User)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        Map<Scope, ComputeSpecScope> computeSpecScopes3 = new HashMap<>();
        computeSpecScopes3.put(Site, computeSpecSiteScope3);
        computeSpecScopes3.put(Project, computeSpecProjectScope3);
        computeSpecScopes3.put(User, computeSpecUserScope3);

        ComputeSpecHardwareOptions computeSpecHardwareOptions3 = ComputeSpecHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(new HashSet<>())
                .build();

        computeSpecConfig3 = ComputeSpecConfig.builder()
                .configTypes(new HashSet<>(Arrays.asList(CONTAINER_SERVICE, JUPYTERHUB)))
                .computeSpec(computeSpec3)
                .scopes(computeSpecScopes3)
                .hardwareOptions(computeSpecHardwareOptions3)
                .build();

        // Setup invalid compute spec config
        ComputeSpec computeSpecInvalid = ComputeSpec.builder()
                .name("") // invalid
                .image("") // invalid
                .environmentVariables(new ArrayList<>())
                .mounts(new ArrayList<>())
                .build();

        Map<Scope, ComputeSpecScope> computeSpecScopesInvalid = new HashMap<>(); // invalid, no scopes

        ComputeSpecHardwareOptions computeSpecHardwareOptionsInvalid = ComputeSpecHardwareOptions.builder()
                .allowAllHardware(true)
                .hardwareConfigs(null) // invalid
                .build();

        computeSpecConfigInvalid = ComputeSpecConfig.builder()
                .configTypes(null) // invalid
                .computeSpec(computeSpecInvalid)
                .scopes(computeSpecScopesInvalid)
                .hardwareOptions(computeSpecHardwareOptionsInvalid)
                .build();
    }

}