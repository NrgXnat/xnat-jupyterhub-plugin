package org.nrg.jobtemplates.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.constants.Scope;
import org.nrg.jobtemplates.config.HibernateComputeSpecConfigEntityServiceTestConfig;
import org.nrg.jobtemplates.entities.ComputeSpecConfigEntity;
import org.nrg.jobtemplates.entities.HardwareConfigEntity;
import org.nrg.jobtemplates.models.*;
import org.nrg.jobtemplates.repositories.ComputeSpecConfigDao;
import org.nrg.jobtemplates.repositories.HardwareConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.jobtemplates.models.ComputeSpecConfig.ConfigType.CONTAINER_SERVICE;
import static org.nrg.jobtemplates.models.ComputeSpecConfig.ConfigType.JUPYTERHUB;
import static org.nrg.jobtemplates.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateComputeSpecConfigEntityServiceTestConfig.class)
public class HibernateComputeSpecConfigEntityServiceTest {

    @Autowired private HibernateComputeSpecConfigEntityService hibernateComputeSpecConfigEntityService;
    @Autowired @Qualifier("hardwareConfigDaoImpl") private HardwareConfigDao hardwareConfigDaoImpl;
    @Autowired @Qualifier("computeSpecConfigDaoImpl") private ComputeSpecConfigDao computeSpecConfigDaoImpl;

    private ComputeSpecConfig computeSpecConfig1;
    private ComputeSpecConfig computeSpecConfig2;
    private ComputeSpecConfig computeSpecConfig3;

    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;

    @Before
    public void before() {
        hibernateComputeSpecConfigEntityService.setDao(computeSpecConfigDaoImpl);
        createDummyConfigsAndEntities();
    }

    @After
    public void after() {
        Mockito.reset();
    }

    @Test
    public void test() {
        assertNotNull(hibernateComputeSpecConfigEntityService);
        assertNotNull(hardwareConfigDaoImpl);
        assertNotNull(computeSpecConfigDaoImpl);
    }

    @Test
    @DirtiesContext
    public void testCreate() {
        // Setup
        ComputeSpecConfigEntity computeSpecConfigEntity1 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig1);

        // Execute
        ComputeSpecConfigEntity created = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity1);
        commitTransaction();
        ComputeSpecConfigEntity retrieved = hibernateComputeSpecConfigEntityService.retrieve(created.getId());

        // Verify
        assertNotNull(retrieved);
        assertThat(retrieved.toPojo(), is(created.toPojo()));
    }

    @Test
    @DirtiesContext
    public void testAddHardwareConfigEntity() {
        // Setup
        HardwareConfigEntity hardwareConfigEntity1 = HardwareConfigEntity.fromPojo(hardwareConfig1);
        HardwareConfigEntity hardwareConfigEntity2 = HardwareConfigEntity.fromPojo(hardwareConfig2);
        ComputeSpecConfigEntity computeSpecConfigEntity1 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig1);
        ComputeSpecConfigEntity computeSpecConfigEntity2 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig2);

        // Commit hardware configs
        Long hardwareConfigEntity1_id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity1);
        Long hardwareConfigEntity2_Id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity2);

        ComputeSpecConfigEntity createdComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity1);
        ComputeSpecConfigEntity createdComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity2);

        commitTransaction();

        // Verify
        assertNotNull(hardwareConfigEntity1_id);
        assertNotNull(hardwareConfigEntity2_Id);
        assertNotNull(createdComputeSpecConfigEntity1);
        assertNotNull(createdComputeSpecConfigEntity2);

        // Execute
        // Add hardware configs to compute spec configs
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity2_Id);
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        ComputeSpecConfigEntity retrievedComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity1.getId());
        ComputeSpecConfigEntity retrievedComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity2.getId());

        assertNotNull(retrievedComputeSpecConfigEntity1);
        assertNotNull(retrievedComputeSpecConfigEntity2);
        assertEquals(2, retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(1, retrievedComputeSpecConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
        assertTrue(retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
        assertTrue(retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity2_Id)));
        assertTrue(retrievedComputeSpecConfigEntity2.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
    }

    @Test
    @DirtiesContext
    public void testRemoveHardwareConfigEntity() {
        // Setup
        HardwareConfigEntity hardwareConfigEntity1 = HardwareConfigEntity.fromPojo(hardwareConfig1);
        HardwareConfigEntity hardwareConfigEntity2 = HardwareConfigEntity.fromPojo(hardwareConfig2);
        ComputeSpecConfigEntity computeSpecConfigEntity1 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig1);
        ComputeSpecConfigEntity computeSpecConfigEntity2 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig2);

        // Commit hardware configs
        Long hardwareConfigEntity1_id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity1);
        Long hardwareConfigEntity2_Id = (Long) hardwareConfigDaoImpl.create(hardwareConfigEntity2);

        ComputeSpecConfigEntity createdComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity1);
        ComputeSpecConfigEntity createdComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity2);

        commitTransaction();

        // Verify
        assertNotNull(hardwareConfigEntity1_id);
        assertNotNull(hardwareConfigEntity2_Id);
        assertNotNull(createdComputeSpecConfigEntity1);
        assertNotNull(createdComputeSpecConfigEntity2);

        // Execute
        // Add hardware configs to compute spec configs
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity2_Id);
        hibernateComputeSpecConfigEntityService.addHardwareConfigEntity(createdComputeSpecConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        ComputeSpecConfigEntity retrievedComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity1.getId());
        ComputeSpecConfigEntity retrievedComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity2.getId());

        assertNotNull(retrievedComputeSpecConfigEntity1);
        assertNotNull(retrievedComputeSpecConfigEntity2);
        assertEquals(2, retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(1, retrievedComputeSpecConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
        assertTrue(retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));
        assertTrue(retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity2_Id)));
        assertTrue(retrievedComputeSpecConfigEntity2.getHardwareOptions().getHardwareConfigs().stream().map(HardwareConfigEntity::getId).anyMatch(id -> id.equals(hardwareConfigEntity1_id)));

        // Remove hardware configs from compute spec configs
        hibernateComputeSpecConfigEntityService.removeHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity1_id);
        hibernateComputeSpecConfigEntityService.removeHardwareConfigEntity(createdComputeSpecConfigEntity1.getId(), hardwareConfigEntity2_Id);

        commitTransaction();

        hibernateComputeSpecConfigEntityService.removeHardwareConfigEntity(createdComputeSpecConfigEntity2.getId(), hardwareConfigEntity1_id);

        commitTransaction();

        // Verify
        retrievedComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity1.getId());
        retrievedComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.retrieve(createdComputeSpecConfigEntity2.getId());

        assertNotNull(retrievedComputeSpecConfigEntity1);
        assertNotNull(retrievedComputeSpecConfigEntity2);
        assertEquals(0, retrievedComputeSpecConfigEntity1.getHardwareOptions().getHardwareConfigs().size());
        assertEquals(0, retrievedComputeSpecConfigEntity2.getHardwareOptions().getHardwareConfigs().size());
    }

    @Test
    @DirtiesContext
    public void testFindByType() {
        // Setup
        ComputeSpecConfigEntity computeSpecConfigEntity1 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig1);
        ComputeSpecConfigEntity computeSpecConfigEntity2 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig2);
        ComputeSpecConfigEntity computeSpecConfigEntity3 = ComputeSpecConfigEntity.fromPojo(computeSpecConfig3);

        // Commit compute spec configs
        ComputeSpecConfigEntity createdComputeSpecConfigEntity1 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity1);
        ComputeSpecConfigEntity createdComputeSpecConfigEntity2 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity2);
        ComputeSpecConfigEntity createdComputeSpecConfigEntity3 = hibernateComputeSpecConfigEntityService.create(computeSpecConfigEntity3);

        commitTransaction();

        // Verify
        assertNotNull(createdComputeSpecConfigEntity1);
        assertNotNull(createdComputeSpecConfigEntity2);
        assertNotNull(createdComputeSpecConfigEntity3);

        // Execute
        List<ComputeSpecConfigEntity> retrievedComputeSpecConfigEntities = hibernateComputeSpecConfigEntityService.findByType(JUPYTERHUB);

        // Verify
        assertNotNull(retrievedComputeSpecConfigEntities);
        assertEquals(2, retrievedComputeSpecConfigEntities.size());
        assertEquals(createdComputeSpecConfigEntity1.getId(), retrievedComputeSpecConfigEntities.get(0).getId());
        assertEquals(createdComputeSpecConfigEntity3.getId(), retrievedComputeSpecConfigEntities.get(1).getId());

        // Execute
        retrievedComputeSpecConfigEntities = hibernateComputeSpecConfigEntityService.findByType(CONTAINER_SERVICE);

        // Verify
        assertNotNull(retrievedComputeSpecConfigEntities);
        assertEquals(2, retrievedComputeSpecConfigEntities.size());
        assertEquals(createdComputeSpecConfigEntity2.getId(), retrievedComputeSpecConfigEntities.get(0).getId());
        assertEquals(createdComputeSpecConfigEntity3.getId(), retrievedComputeSpecConfigEntities.get(1).getId());
    }

    public void createDummyConfigsAndEntities() {
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
        // Create mount first
        Mount mount1 = Mount.builder()
                .localPath("/home/jovyan/work")
                .containerPath("/home/jovyan/work")
                .readOnly(false)
                .build();

        Mount mount2 = Mount.builder()
                .localPath("/tools/MATLAB/R2019b")
                .containerPath("/tools/MATLAB/R2019b")
                .readOnly(true)
                .build();

        ComputeSpec computeSpec1 = ComputeSpec.builder()
                .name("Jupyter Datascience Notebook")
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .environmentVariables(Collections.singletonList(new EnvironmentVariable("JUPYTER_ENABLE_LAB", "yes")))
                .mounts(Arrays.asList(mount1, mount2))
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
                .hardwareConfigs(new HashSet<>())
                .build();

        computeSpecConfig1 = ComputeSpecConfig.builder()
                .configTypes(new HashSet<>(Collections.singletonList(JUPYTERHUB)))
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
                .hardwareConfigs(new HashSet<>())
                .build();

        computeSpecConfig2 = ComputeSpecConfig.builder()
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
                .enabled(true)
                .ids(new HashSet<>(Collections.emptyList()))
                .build();

        ComputeSpecScope computeSpecProjectScope3 = ComputeSpecScope.builder()
                .scope(Project)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("Project1")))
                .build();

        ComputeSpecScope computeSpecUserScope3 = ComputeSpecScope.builder()
                .scope(User)
                .enabled(false)
                .ids(new HashSet<>(Collections.singletonList("User1")))
                .build();

        Map<Scope, ComputeSpecScope> computeSpecScopes3 = new HashMap<>();
        computeSpecScopes2.put(Site, computeSpecSiteScope3);
        computeSpecScopes2.put(Project, computeSpecProjectScope3);
        computeSpecScopes2.put(User, computeSpecUserScope3);

        ComputeSpecHardwareOptions computeSpecHardwareOptions3 = ComputeSpecHardwareOptions.builder()
                .allowAllHardware(false)
                .hardwareConfigs(new HashSet<>())
                .build();

        computeSpecConfig3 = ComputeSpecConfig.builder()
                .configTypes(new HashSet<>(Arrays.asList(CONTAINER_SERVICE, JUPYTERHUB)))
                .computeSpec(computeSpec3)
                .scopes(computeSpecScopes3)
                .hardwareOptions(computeSpecHardwareOptions3)
                .build();
    }

}