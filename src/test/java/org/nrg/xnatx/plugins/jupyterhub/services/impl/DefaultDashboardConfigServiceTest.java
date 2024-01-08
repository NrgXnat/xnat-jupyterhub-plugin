package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultDashboardConfigServiceTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardScope;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.nrg.framework.constants.Scope.*;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.CONTAINER_SERVICE;
import static org.nrg.xnat.compute.models.ComputeEnvironmentConfig.ConfigType.JUPYTERHUB;
import static org.nrg.xnatx.plugins.jupyterhub.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DefaultDashboardConfigServiceTestConfig.class})
@Transactional
public class DefaultDashboardConfigServiceTest {

    @Autowired private DefaultDashboardConfigService defaultDashboardConfigService;
    @Autowired private DashboardConfigEntityService dashboardConfigEntityService;
    @Autowired private ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService;
    @Autowired private ComputeEnvironmentConfigService computeEnvironmentConfigService;
    @Autowired private HardwareConfigEntityService hardwareConfigEntityService;
    @Autowired private HardwareConfigService hardwareConfigService;
    @Autowired private DashboardFrameworkService dashboardFrameworkService;

    private ComputeEnvironmentConfig computeEnvironmentConfig1;
    private ComputeEnvironmentConfig computeEnvironmentConfig2;
    private ComputeEnvironmentConfig computeEnvironmentConfig3;

    private HardwareConfig hardwareConfig1;
    private HardwareConfig hardwareConfig2;
    private HardwareConfig hardwareConfig3;

    private DashboardFramework panel;
    private DashboardFramework streamlit;
    private DashboardFramework voila;
    private DashboardFramework dash;

    private DashboardConfig dashboardConfig1;
    private DashboardConfig dashboardConfig2;
    private DashboardConfig dashboardConfig3;
    private DashboardConfig dashboardConfigInvalid;

    @Before
    public void setUp()  {
        createDummyConfigs();
    }

    @After
    public void tearDown()  {
    }

    @Test
    public void test_wiring() {
        assertNotNull(defaultDashboardConfigService);
        assertNotNull(dashboardConfigEntityService);
        assertNotNull(computeEnvironmentConfigEntityService);
        assertNotNull(computeEnvironmentConfigService);
        assertNotNull(hardwareConfigEntityService);
        assertNotNull(hardwareConfigService);
    }

    @Test
    public void testValidateDashboardConfigs() {
        defaultDashboardConfigService.validate(dashboardConfig1);
        defaultDashboardConfigService.validate(dashboardConfig2);
        defaultDashboardConfigService.validate(dashboardConfig3);

        assertTrue(defaultDashboardConfigService.isValid(dashboardConfig1));
        assertTrue(defaultDashboardConfigService.isValid(dashboardConfig2));
        assertTrue(defaultDashboardConfigService.isValid(dashboardConfig3));
    }

    @Test
    public void testValidateDashboards() {
        defaultDashboardConfigService.validate(dashboardConfig1.getDashboard());
        defaultDashboardConfigService.validate(dashboardConfig2.getDashboard());
        defaultDashboardConfigService.validate(dashboardConfig3.getDashboard());
    }

    @Test
    public void testValidateDashboardScopes() {
        defaultDashboardConfigService.validate(dashboardConfig1.getScopes());
        defaultDashboardConfigService.validate(dashboardConfig2.getScopes());
        defaultDashboardConfigService.validate(dashboardConfig3.getScopes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoComputeEnvironmentConfig() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setComputeEnvironmentConfig(null);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test
    public void test_InvalidDashboardConfig_NoComputeEnvironmentConfig_2() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setComputeEnvironmentConfig(null);
        assertFalse(defaultDashboardConfigService.isValid(dashboardConfigInvalid));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoHardwareConfig() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setHardwareConfig(null);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoDashboard() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setDashboard(null);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoScopes() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setScopes(null);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_EmptyScopes() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.setScopes(new HashMap<>());
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoScopeSite() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.getScopes().remove(Site);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoScopeProject() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.getScopes().remove(Project);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_InvalidDashboardConfig_NoScopeDataType() {
        dashboardConfigInvalid = dashboardConfig1;
        dashboardConfigInvalid.getScopes().remove(DataType);
        defaultDashboardConfigService.validate(dashboardConfigInvalid);
    }

    @Test
    @DirtiesContext
    public void test_create() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        assertEquals(dashboardConfig1.getDashboard(), created1.getDashboard());
        assertEquals(dashboardConfig2.getDashboard(), created2.getDashboard());
        assertEquals(dashboardConfig3.getDashboard(), created3.getDashboard());

        assertEquals(dashboardConfig1.getScopes(), created1.getScopes());
        assertEquals(dashboardConfig2.getScopes(), created2.getScopes());
        assertEquals(dashboardConfig3.getScopes(), created3.getScopes());

        // When checking pojos, the compute environment and hardware configs will only have ids and names, not the full objects
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getId(), created1.getComputeEnvironmentConfig().getId());
        assertEquals(dashboardConfig2.getComputeEnvironmentConfig().getId(), created2.getComputeEnvironmentConfig().getId());
        assertEquals(dashboardConfig3.getComputeEnvironmentConfig().getId(), created3.getComputeEnvironmentConfig().getId());

        assertEquals(dashboardConfig1.getHardwareConfig().getId(), created1.getHardwareConfig().getId());
        assertEquals(dashboardConfig2.getHardwareConfig().getId(), created2.getHardwareConfig().getId());
        assertEquals(dashboardConfig3.getHardwareConfig().getId(), created3.getHardwareConfig().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    @DirtiesContext
    public void test_create_invalid() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Create dashboard config
        DashboardConfig createdInvalid = defaultDashboardConfigService.create(dashboardConfigInvalid);

        commitTransaction();
    }

    @Test
    @DirtiesContext
    public void test_exists() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        assertTrue(defaultDashboardConfigService.exists(created1.getId()));
        assertTrue(defaultDashboardConfigService.exists(created2.getId()));
        assertTrue(defaultDashboardConfigService.exists(created3.getId()));
    }

    @Test
    @DirtiesContext
    public void test_retrieve() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        Optional<DashboardConfig> retrieved1 = defaultDashboardConfigService.retrieve(created1.getId());
        Optional<DashboardConfig> retrieved2 = defaultDashboardConfigService.retrieve(created2.getId());
        Optional<DashboardConfig> retrieved3 = defaultDashboardConfigService.retrieve(created3.getId());

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved3.isPresent());

        assertEquals(created1, retrieved1.get());
        assertEquals(created2, retrieved2.get());
        assertEquals(created3, retrieved3.get());
    }

    @Test
    @DirtiesContext
    public void test_retrieve_dne() {
        Optional<DashboardConfig> retrievedDne = defaultDashboardConfigService.retrieve(999L);
        assertFalse(retrievedDne.isPresent());
    }

    @Test
    @DirtiesContext
    public void test_getAll() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        List<DashboardConfig> retrieved = defaultDashboardConfigService.getAll();

        assertThat(retrieved.size(), is(3));
        assertThat(retrieved, hasItems(created1, created2, created3));
    }

    @Test
    @DirtiesContext
    public void test_update_updateDashboard() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Update dashboard config
        dashboardConfig1.setId(created1.getId());
        dashboardConfig1.getDashboard().setName("New Name");
        dashboardConfig1.getDashboard().setDescription("New Description");
        dashboardConfig1.getDashboard().setFramework(streamlit.getName());
        dashboardConfig1.getDashboard().setCommand("New Command");
        dashboardConfig1.getDashboard().setFileSource("New File Source");
        dashboardConfig1.getDashboard().setGitRepoUrl("New Git Repo Url");
        dashboardConfig1.getDashboard().setGitRepoBranch("New Git Repo Branch");
        dashboardConfig1.getDashboard().setMainFilePath("New Main File Path");

        DashboardConfig updated1 = defaultDashboardConfigService.update(dashboardConfig1);

        commitTransaction();

        // Verify updated dashboard config
        assertNotNull(updated1);

        // These are not equal because the updated dashboardConfig# has the full compute environment and hardware config
        // pojos while the retrieved updated1 only has the ids and names
        // assertEquals(dashboardConfig1, updated1);

        assertEquals(dashboardConfig1.getDashboard(), updated1.getDashboard());
        assertNotEquals(created1.getDashboard(), updated1.getDashboard());
        assertEquals(dashboardConfig1.getScopes(), updated1.getScopes());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getId(),
                             updated1.getComputeEnvironmentConfig().getId());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getComputeEnvironment().getName(),
                             updated1.getComputeEnvironmentConfig().getComputeEnvironment().getName());
        assertEquals(dashboardConfig1.getHardwareConfig().getId(),
                             updated1.getHardwareConfig().getId());
        assertEquals(dashboardConfig1.getHardwareConfig().getHardware().getName(),
                             updated1.getHardwareConfig().getHardware().getName());
    }

    @Test
    @DirtiesContext
    public void test_update_updateScopes() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Update dashboard config
        dashboardConfig1.setId(created1.getId());
        dashboardConfig1.getScopes().get(Site).setEnabled(false);
        dashboardConfig1.getScopes().get(Project).setIds(new HashSet<>(Arrays.asList("ProjectA", "ProjectB", "ProjectC")));
        dashboardConfig1.getScopes().get(DataType).setIds(new HashSet<>(Arrays.asList("pixi:bliSessionData", "xnat:petSessionData")));

        DashboardConfig updated1 = defaultDashboardConfigService.update(dashboardConfig1);

        commitTransaction();

        // Verify updated dashboard config
        assertNotNull(updated1);

        // These are not equal because the updated dashboardConfig# has the full compute environment and hardware config
        // pojos while the retrieved updated1 only has the ids and names
        // assertEquals(dashboardConfig1, updated1);

        assertEquals(dashboardConfig1.getDashboard(), updated1.getDashboard());
        assertEquals(dashboardConfig1.getScopes(), updated1.getScopes());
        assertNotEquals(created1.getScopes(), updated1.getScopes());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getId(),
                             updated1.getComputeEnvironmentConfig().getId());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getComputeEnvironment().getName(),
                             updated1.getComputeEnvironmentConfig().getComputeEnvironment().getName());
        assertEquals(dashboardConfig1.getHardwareConfig().getId(),
                             updated1.getHardwareConfig().getId());
        assertEquals(dashboardConfig1.getHardwareConfig().getHardware().getName(),
                             updated1.getHardwareConfig().getHardware().getName());
    }

    @Test
    @DirtiesContext
    public void test_update_updateComputeEnvAndHardware() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Update dashboard config
        dashboardConfig1.setId(created1.getId());
        dashboardConfig1.setComputeEnvironmentConfig(computeEnvironmentConfig2);
        dashboardConfig1.setHardwareConfig(hardwareConfig2);

        DashboardConfig updated1 = defaultDashboardConfigService.update(dashboardConfig1);

        commitTransaction();

        // Verify updated dashboard config
        assertNotNull(updated1);

        // These are not equal because the updated dashboardConfig# has the full compute environment and hardware config
        // pojos while the retrieved updated1 only has the ids and names
        // assertEquals(dashboardConfig1, updated1);

        assertEquals(dashboardConfig1.getDashboard(), updated1.getDashboard());
        assertEquals(dashboardConfig1.getScopes(), updated1.getScopes());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getId(),
                             updated1.getComputeEnvironmentConfig().getId());
        assertEquals(dashboardConfig1.getComputeEnvironmentConfig().getComputeEnvironment().getName(),
                             updated1.getComputeEnvironmentConfig().getComputeEnvironment().getName());
        assertNotEquals(created1.getComputeEnvironmentConfig().getId(),
                        updated1.getComputeEnvironmentConfig().getId());
        assertNotEquals(created1.getComputeEnvironmentConfig().getComputeEnvironment().getName(),
                        updated1.getComputeEnvironmentConfig().getComputeEnvironment().getName());
        assertEquals(dashboardConfig1.getHardwareConfig().getId(),
                             updated1.getHardwareConfig().getId());
        assertEquals(dashboardConfig1.getHardwareConfig().getHardware().getName(),
                             updated1.getHardwareConfig().getHardware().getName());
        assertNotEquals(created1.getHardwareConfig().getId(),
                        updated1.getHardwareConfig().getId());
        assertNotEquals(created1.getHardwareConfig().getHardware().getName(),
                        updated1.getHardwareConfig().getHardware().getName());
    }

    @Test
    @DirtiesContext
    public void testDelete() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        // Delete dashboard config
        defaultDashboardConfigService.delete(created1.getId());
        defaultDashboardConfigService.delete(created2.getId());
        defaultDashboardConfigService.delete(created3.getId());

        commitTransaction();

        // Verify deleted dashboard configs
        assertFalse(defaultDashboardConfigService.exists(created1.getId()));
        assertFalse(defaultDashboardConfigService.exists(created2.getId()));
        assertFalse(defaultDashboardConfigService.exists(created3.getId()));
    }

    @Test
    @DirtiesContext
    public void test_isAvailable() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard config
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Site, "XNAT");
        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:mrSessionData");

        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project2");
        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project3");
        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project4");
        assertFalse(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:petSessionData");
        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "pixi:bliSessionData");
        assertFalse(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:mrSessionData");
        executionScope.put(Scope.User, "User1"); // Should not matter
        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:ctSessionData");
        assertTrue(defaultDashboardConfigService.isAvailable(created1.getId(), executionScope));

        executionScope.put(Scope.Project, "Project2");
        executionScope.put(Scope.DataType, "xnat:ctSessionData");
        assertFalse(defaultDashboardConfigService.isAvailable(created2.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:projectData");
        assertTrue(defaultDashboardConfigService.isAvailable(created2.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:ctSessionData");
        assertFalse(defaultDashboardConfigService.isAvailable(created2.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:projectData");
        assertFalse(defaultDashboardConfigService.isAvailable(created3.getId(), executionScope));

        executionScope.put(Scope.Project, "Project2");
        executionScope.put(Scope.DataType, "xnat:projectData");
        assertFalse(defaultDashboardConfigService.isAvailable(created3.getId(), executionScope));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:mrSessionData");
        assertFalse(defaultDashboardConfigService.isAvailable(created3.getId(), executionScope));
    }

    @Test
    @DirtiesContext
    public void test_getAvailable() {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        Map<Scope, String> executionScope = new HashMap<>();
        executionScope.put(Scope.Site, "XNAT");
        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:mrSessionData");

        List<DashboardConfig> available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));

        executionScope.put(Scope.Project, "Project2");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));

        executionScope.put(Scope.Project, "Project3");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));

        executionScope.put(Scope.Project, "Project4");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(0));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:petSessionData");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "pixi:bliSessionData");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(0));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:mrSessionData");
        executionScope.put(Scope.User, "User1"); // Should not matter
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));

        executionScope.put(Scope.Project, "Project1");
        executionScope.put(Scope.DataType, "xnat:projectData");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(2));
        assertThat(available, hasItems(created1, created2));

        executionScope.put(Scope.Project, "Project2");
        executionScope.put(Scope.DataType, "xnat:projectData");
        available = defaultDashboardConfigService.getAvailable(executionScope);

        assertThat(available.size(), is(1));
        assertThat(available, hasItems(created1));
    }

    @Test
    @DirtiesContext
    public void test_enableForSite() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        // Enable dashboard config for site
        defaultDashboardConfigService.enableForSite(created1.getId());
        defaultDashboardConfigService.enableForSite(created2.getId());
        defaultDashboardConfigService.enableForSite(created3.getId());

        commitTransaction();

        // Verify enabled dashboard configs
        Optional<DashboardConfig> retrieved1 = defaultDashboardConfigService.retrieve(created1.getId());
        Optional<DashboardConfig> retrieved2 = defaultDashboardConfigService.retrieve(created2.getId());
        Optional<DashboardConfig> retrieved3 = defaultDashboardConfigService.retrieve(created3.getId());

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved3.isPresent());

        assertTrue(retrieved1.get().getScopes().get(Scope.Site).isEnabled());
        assertTrue(retrieved2.get().getScopes().get(Scope.Site).isEnabled());
        assertTrue(retrieved3.get().getScopes().get(Scope.Site).isEnabled());
    }

    @Test
    @DirtiesContext
    public void test_disableForSite() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        // Disable dashboard config for site
        defaultDashboardConfigService.disableForSite(created1.getId());
        defaultDashboardConfigService.disableForSite(created2.getId());
        defaultDashboardConfigService.disableForSite(created3.getId());

        commitTransaction();

        // Verify disabled dashboard configs
        Optional<DashboardConfig> retrieved1 = defaultDashboardConfigService.retrieve(created1.getId());
        Optional<DashboardConfig> retrieved2 = defaultDashboardConfigService.retrieve(created2.getId());
        Optional<DashboardConfig> retrieved3 = defaultDashboardConfigService.retrieve(created3.getId());

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved3.isPresent());

        assertFalse(retrieved1.get().getScopes().get(Scope.Site).isEnabled());
        assertFalse(retrieved2.get().getScopes().get(Scope.Site).isEnabled());
        assertFalse(retrieved3.get().getScopes().get(Scope.Site).isEnabled());
    }

    @Test
    @DirtiesContext
    public void test_enable_and_disableForProject() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        DashboardConfig created1 = defaultDashboardConfigService.create(dashboardConfig1);
        DashboardConfig created2 = defaultDashboardConfigService.create(dashboardConfig2);
        DashboardConfig created3 = defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Verify created dashboard configs
        assertNotNull(created1);
        assertNotNull(created2);
        assertNotNull(created3);

        // Enable dashboard config for project
        defaultDashboardConfigService.enableForProject(created1.getId(), "ProjectA");
        defaultDashboardConfigService.enableForProject(created1.getId(), "ProjectB");
        defaultDashboardConfigService.enableForProject(created1.getId(), "ProjectC");
        defaultDashboardConfigService.enableForProject(created2.getId(), "ProjectA");
        defaultDashboardConfigService.enableForProject(created2.getId(), "ProjectB");
        defaultDashboardConfigService.enableForProject(created2.getId(), "ProjectC");
        defaultDashboardConfigService.enableForProject(created3.getId(), "ProjectA");
        defaultDashboardConfigService.enableForProject(created3.getId(), "ProjectB");
        defaultDashboardConfigService.enableForProject(created3.getId(), "ProjectC");
        commitTransaction();

        // Verify enabled dashboard configs
        Optional<DashboardConfig> retrieved1 = defaultDashboardConfigService.retrieve(created1.getId());
        Optional<DashboardConfig> retrieved2 = defaultDashboardConfigService.retrieve(created2.getId());
        Optional<DashboardConfig> retrieved3 = defaultDashboardConfigService.retrieve(created3.getId());

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved3.isPresent());

        assertThat(retrieved1.get().getScopes().get(Scope.Project).getIds(), hasItems("ProjectA", "ProjectB", "ProjectC"));
        assertThat(retrieved2.get().getScopes().get(Scope.Project).getIds(), hasItems("ProjectA", "ProjectB", "ProjectC"));
        assertThat(retrieved3.get().getScopes().get(Scope.Project).getIds(), hasItems("ProjectA", "ProjectB", "ProjectC"));

        // Disable dashboard config for project
        // Disable dashboard config for project
        defaultDashboardConfigService.disableForProject(created1.getId(), "ProjectA");
        defaultDashboardConfigService.disableForProject(created1.getId(), "ProjectB");
        defaultDashboardConfigService.disableForProject(created1.getId(), "ProjectC");
        defaultDashboardConfigService.disableForProject(created2.getId(), "ProjectA");
        defaultDashboardConfigService.disableForProject(created2.getId(), "ProjectB");
        defaultDashboardConfigService.disableForProject(created2.getId(), "ProjectC");
        defaultDashboardConfigService.disableForProject(created3.getId(), "ProjectA");
        defaultDashboardConfigService.disableForProject(created3.getId(), "ProjectB");
        defaultDashboardConfigService.disableForProject(created3.getId(), "ProjectC");
        commitTransaction();

        // Verify disabled dashboard configs
        retrieved1 = defaultDashboardConfigService.retrieve(created1.getId());
        retrieved2 = defaultDashboardConfigService.retrieve(created2.getId());
        retrieved3 = defaultDashboardConfigService.retrieve(created3.getId());

        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertTrue(retrieved3.isPresent());

        assertThat(retrieved1.get().getScopes().get(Scope.Project).getIds(), not(hasItems("ProjectA", "ProjectB", "ProjectC")));
        assertThat(retrieved2.get().getScopes().get(Scope.Project).getIds(), not(hasItems("ProjectA", "ProjectB", "ProjectC")));
        assertThat(retrieved3.get().getScopes().get(Scope.Project).getIds(), not(hasItems("ProjectA", "ProjectB", "ProjectC")));
    }

    @Test(expected = DataIntegrityViolationException.class)
    @DirtiesContext
    public void testCantDeleteFrameworkInUse() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        defaultDashboardConfigService.create(dashboardConfig1);
        defaultDashboardConfigService.create(dashboardConfig2);
        defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Try to delete dashboard framework in use
        dashboardFrameworkService.delete(panel.getName());
        commitTransaction();
        fail("Should not be able to delete dashboard framework in use by dashboard configs");
    }

    @Test
    @DirtiesContext
    public void testCantDeleteComputeEnvironmentConfigInUse() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        defaultDashboardConfigService.create(dashboardConfig1);
        defaultDashboardConfigService.create(dashboardConfig2);
        defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Try to delete compute environment config in use
        try {
            computeEnvironmentConfigService.delete(computeEnvironmentConfig1.getId());
        } catch (RuntimeException e) {
            assertTrue(computeEnvironmentConfigService.exists(computeEnvironmentConfig1.getId()));
            return;
        }

        fail("Should not be able to delete compute environment config in use by dashboard configs");
    }

    @Test
    @DirtiesContext
    public void testCanDeleteComputeEnvironmentConfigNotInUse() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        defaultDashboardConfigService.create(dashboardConfig1);
        defaultDashboardConfigService.create(dashboardConfig2);
        defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Try to delete compute environment config not in use
        computeEnvironmentConfigService.delete(computeEnvironmentConfig2.getId());
        commitTransaction();
        assertFalse(computeEnvironmentConfigService.exists(computeEnvironmentConfig2.getId()));
    }

    @Test
    @DirtiesContext
    public void testCantDeleteHardwareConfigInUse() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        defaultDashboardConfigService.create(dashboardConfig1);
        defaultDashboardConfigService.create(dashboardConfig2);
        defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Try to delete hardware config in use
        try {
            hardwareConfigService.delete(hardwareConfig1.getId());
        } catch (RuntimeException e) {
            assertTrue(hardwareConfigService.exists(hardwareConfig1.getId()));
            return;
        }

        fail("Should not be able to delete hardware config in use by dashboard configs");
    }

    @Test
    @DirtiesContext
    public void testCanDeleteHardwareConfigNotInUse() throws Exception {
        // Create compute environment and hardware configs
        commitComputeEnvironmentAndHardwareConfigs();

        // Commit dashboard frameworks
        commitDashboardFrameworks();

        // Create dashboard configs
        defaultDashboardConfigService.create(dashboardConfig1);
        defaultDashboardConfigService.create(dashboardConfig2);
        defaultDashboardConfigService.create(dashboardConfig3);

        commitTransaction();

        // Try to delete hardware config not in use
        hardwareConfigService.delete(hardwareConfig2.getId());
        commitTransaction();
        assertFalse(hardwareConfigService.exists(hardwareConfig2.getId()));
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

        // Setup second hardware config
        Hardware hardware3 = Hardware.builder()
                                     .name("Large")
                                     .cpuReservation(8.0)
                                     .cpuLimit(8.0)
                                     .memoryReservation("16G")
                                     .memoryLimit("16G")
                                     .build();

        // No constraints, environment variables or generic resources
        hardware3.setConstraints(Collections.emptyList());
        hardware3.setEnvironmentVariables(Collections.emptyList());
        hardware3.setGenericResources(Collections.emptyList());

        // Setup hardware scopes
        HardwareScope hardwareSiteScope3 = HardwareScope.builder()
                                                        .scope(Site)
                                                        .enabled(true)
                                                        .ids(new HashSet<>(Collections.emptyList()))
                                                        .build();

        HardwareScope hardwareProjectScope3 = HardwareScope.builder()
                                                           .scope(Project)
                                                           .enabled(false)
                                                           .ids(new HashSet<>(Collections.singletonList("ProjectABCDE")))
                                                           .build();

        HardwareScope userHardwareScope3 = HardwareScope.builder()
                                                        .scope(User)
                                                        .enabled(true)
                                                        .ids(new HashSet<>(Collections.emptyList()))
                                                        .build();

        Map<Scope, HardwareScope> hardwareScopes3 = new HashMap<>();
        hardwareScopes3.put(Site, hardwareSiteScope3);
        hardwareScopes3.put(Project, hardwareProjectScope3);
        hardwareScopes3.put(User, userHardwareScope3);

        // Setup second hardware config entity
        hardwareConfig3 = HardwareConfig.builder()
                                        .hardware(hardware3)
                                        .scopes(hardwareScopes3)
                                        .build();

        // Setup first compute environment
        ComputeEnvironment computeEnvironment1 = ComputeEnvironment.builder()
                                                                   .name("Jupyter Datascience Notebook")
                                                                   .image("jupyter/datascience-notebook:hub-3.0.0")
                                                                   .environmentVariables(new ArrayList<>())
                                                                   .mounts(new ArrayList<>())
                                                                   .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope1 = ComputeEnvironmentScope.builder()
                                                                                      .scope(Site)
                                                                                      .enabled(true)
                                                                                      .ids(new HashSet<>(Collections.emptyList()))
                                                                                      .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope1 = ComputeEnvironmentScope.builder()
                                                                                         .scope(Project)
                                                                                         .enabled(true)
                                                                                         .ids(new HashSet<>(Collections.emptyList()))
                                                                                         .build();

        ComputeEnvironmentScope computeEnvironmentUserScope1 = ComputeEnvironmentScope.builder()
                                                                                      .scope(User)
                                                                                      .enabled(true)
                                                                                      .ids(new HashSet<>(Collections.emptyList()))
                                                                                      .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes1 = new HashMap<>();
        computeEnvironmentScopes1.put(Site, computeEnvironmentSiteScope1);
        computeEnvironmentScopes1.put(Project, computeEnvironmentProjectScope1);
        computeEnvironmentScopes1.put(User, computeEnvironmentUserScope1);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions1 = ComputeEnvironmentHardwareOptions.builder()
                                                                                                                .allowAllHardware(true)
                                                                                                                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig1, hardwareConfig2, hardwareConfig3)))
                                                                                                                .build();

        computeEnvironmentConfig1 = ComputeEnvironmentConfig.builder()
                                                            .configTypes(new HashSet<>(Collections.singletonList(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB)))
                                                            .computeEnvironment(computeEnvironment1)
                                                            .scopes(computeEnvironmentScopes1)
                                                            .hardwareOptions(computeEnvironmentHardwareOptions1)
                                                            .build();

        // Setup second compute environment
        ComputeEnvironment computeEnvironment2 = ComputeEnvironment.builder()
                                                                   .name("XNAT Datascience Notebook")
                                                                   .image("xnat/datascience-notebook:latest")
                                                                   .environmentVariables(new ArrayList<>())
                                                                   .mounts(new ArrayList<>())
                                                                   .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope2 = ComputeEnvironmentScope.builder()
                                                                                      .scope(Site)
                                                                                      .enabled(true)
                                                                                      .ids(new HashSet<>(Collections.emptyList()))
                                                                                      .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope2 = ComputeEnvironmentScope.builder()
                                                                                         .scope(Project)
                                                                                         .enabled(false)
                                                                                         .ids(new HashSet<>(new ArrayList<>(Arrays.asList("Project1", "Project10", "Project100"))))
                                                                                         .build();

        ComputeEnvironmentScope computeEnvironmentUserScope2 = ComputeEnvironmentScope.builder()
                                                                                      .scope(User)
                                                                                      .enabled(false)
                                                                                      .ids(new HashSet<>(new ArrayList<>(Arrays.asList("User1", "User10", "User100"))))
                                                                                      .build();

        ComputeEnvironmentScope computeEnvironmentDatatypeScope2 = ComputeEnvironmentScope.builder()
                                                                                          .scope(DataType)
                                                                                          .enabled(false)
                                                                                          .ids(new HashSet<>(Arrays.asList("xnat:mrSessionData", "xnat:petSessionData", "xnat:ctSessionData")))
                                                                                          .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes2 = new HashMap<>();
        computeEnvironmentScopes2.put(Site, computeEnvironmentSiteScope2);
        computeEnvironmentScopes2.put(Project, computeEnvironmentProjectScope2);
        computeEnvironmentScopes2.put(User, computeEnvironmentUserScope2);
        computeEnvironmentScopes2.put(DataType, computeEnvironmentDatatypeScope2);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions2 = ComputeEnvironmentHardwareOptions.builder()
                                                                                                                .allowAllHardware(false)
                                                                                                                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig2, hardwareConfig3)))
                                                                                                                .build();

        computeEnvironmentConfig2 = ComputeEnvironmentConfig.builder()
                                                            .configTypes(new HashSet<>(Collections.singletonList(CONTAINER_SERVICE)))
                                                            .computeEnvironment(computeEnvironment2)
                                                            .scopes(computeEnvironmentScopes2)
                                                            .hardwareOptions(computeEnvironmentHardwareOptions2)
                                                            .build();

        // Setup third compute environment
        ComputeEnvironment computeEnvironment3 = ComputeEnvironment.builder()
                                                                   .name("MATLAB Datascience Notebook")
                                                                   .image("matlab/datascience-notebook:latest")
                                                                   .environmentVariables(new ArrayList<>())
                                                                   .mounts(new ArrayList<>())
                                                                   .build();

        ComputeEnvironmentScope computeEnvironmentSiteScope3 = ComputeEnvironmentScope.builder()
                                                                                      .scope(Site)
                                                                                      .enabled(false)
                                                                                      .ids(new HashSet<>(Collections.emptyList()))
                                                                                      .build();

        ComputeEnvironmentScope computeEnvironmentProjectScope3 = ComputeEnvironmentScope.builder()
                                                                                         .scope(Project)
                                                                                         .enabled(true)
                                                                                         .ids(new HashSet<>())
                                                                                         .build();

        ComputeEnvironmentScope computeEnvironmentUserScope3 = ComputeEnvironmentScope.builder()
                                                                                      .scope(User)
                                                                                      .enabled(true)
                                                                                      .ids(new HashSet<>())
                                                                                      .build();

        Map<Scope, ComputeEnvironmentScope> computeEnvironmentScopes3 = new HashMap<>();
        computeEnvironmentScopes3.put(Site, computeEnvironmentSiteScope3);
        computeEnvironmentScopes3.put(Project, computeEnvironmentProjectScope3);
        computeEnvironmentScopes3.put(User, computeEnvironmentUserScope3);

        ComputeEnvironmentHardwareOptions computeEnvironmentHardwareOptions3 = ComputeEnvironmentHardwareOptions.builder()
                                                                                                                .allowAllHardware(false)
                                                                                                                .hardwareConfigs(new HashSet<>(Arrays.asList(hardwareConfig2, hardwareConfig3)))
                                                                                                                .build();

        computeEnvironmentConfig3 = ComputeEnvironmentConfig.builder()
                                                            .configTypes(new HashSet<>(Arrays.asList(CONTAINER_SERVICE, JUPYTERHUB)))
                                                            .computeEnvironment(computeEnvironment3)
                                                            .scopes(computeEnvironmentScopes3)
                                                            .hardwareOptions(computeEnvironmentHardwareOptions3)
                                                            .build();

        // Setup dashboard frameworks
        panel = DashboardFramework.builder()
                                  .name("Panel")
                                  .commandTemplate("jhsingle-native-proxy ...")
                                  .build();
        streamlit = DashboardFramework.builder()
                                      .name("Streamlit")
                                      .commandTemplate("jhsingle-native-proxy ...")
                                      .build();

        voila = DashboardFramework.builder()
                                  .name("Voila")
                                  .commandTemplate("jhsingle-native-proxy ...")
                                  .build();

        dash = DashboardFramework.builder()
                                  .name("Dash")
                                  .commandTemplate("jhsingle-native-proxy ...")
                                  .build();

        List<DashboardFramework> frameworks = Arrays.asList(panel, streamlit, voila, dash);

        // Setup first dashboard config
        Dashboard dashboard1 = Dashboard.builder()
                                        .name("Panel Dashboard")
                                        .description("A dashboard using Panel")
                                        .framework("Panel")
                                        .command(null)
                                        .fileSource("git")
                                        .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                        .gitRepoBranch("main")
                                        .mainFilePath("pane/panel_dashboard.ipynb")
                                        .build();

        DashboardScope dashboardSiteScope1 = DashboardScope.builder()
                                                           .scope(Site)
                                                           .enabled(true)
                                                           .ids(new HashSet<>(Collections.emptyList()))
                                                           .build();

        DashboardScope dashboardProjectScope1 = DashboardScope.builder()
                                                              .scope(Project)
                                                              .enabled(false)
                                                              .ids(new HashSet<>(new ArrayList<>(Arrays.asList("Project1", "Project2", "Project3"))))
                                                              .build();

        DashboardScope dashboardDatatypeScope1 = DashboardScope.builder()
                                                               .scope(DataType)
                                                               .enabled(false)
                                                               .ids(new HashSet<>(Arrays.asList("xnat:mrSessionData", "xnat:petSessionData", "xnat:ctSessionData", "xnat:projectData")))
                                                               .build();

        Map<Scope, DashboardScope> dashboardScopes1 = new HashMap<>();
        dashboardScopes1.put(Site, dashboardSiteScope1);
        dashboardScopes1.put(Project, dashboardProjectScope1);
        dashboardScopes1.put(DataType, dashboardDatatypeScope1);

        dashboardConfig1 = DashboardConfig.builder()
                                          .dashboard(dashboard1)
                                          .scopes(dashboardScopes1)
                                          .computeEnvironmentConfig(computeEnvironmentConfig1)
                                          .hardwareConfig(hardwareConfig1)
                                          .build();

        // Setup second dashboard config
        Dashboard dashboard2 = Dashboard.builder()
                                        .name("Voila Dashboard")
                                        .description("A dashboard using Voila")
                                        .framework("Voila")
                                        .command(null)
                                        .fileSource("git")
                                        .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                        .gitRepoBranch("main")
                                        .mainFilePath("voila/voila_dashboard.ipynb")
                                        .build();

        DashboardScope dashboardSiteScope2 = DashboardScope.builder()
                                                           .scope(Site)
                                                           .enabled(true)
                                                           .ids(new HashSet<>(Collections.emptyList()))
                                                           .build();

        DashboardScope dashboardProjectScope2 = DashboardScope.builder()
                                                              .scope(Project)
                                                              .enabled(false)
                                                              .ids(new HashSet<>(new ArrayList<>(Collections.singletonList("Project1"))))
                                                              .build();

        DashboardScope dashboardDatatypeScope2 = DashboardScope.builder()
                                                               .scope(DataType)
                                                               .enabled(false)
                                                               .ids(new HashSet<>(Collections.singletonList("xnat:projectData")))
                                                               .build();

        Map<Scope, DashboardScope> dashboardScopes2 = new HashMap<>();
        dashboardScopes2.put(Site, dashboardSiteScope2);
        dashboardScopes2.put(Project, dashboardProjectScope2);
        dashboardScopes2.put(DataType, dashboardDatatypeScope2);

        dashboardConfig2 = DashboardConfig.builder()
                                          .dashboard(dashboard2)
                                          .scopes(dashboardScopes2)
                                          .computeEnvironmentConfig(computeEnvironmentConfig3)
                                          .hardwareConfig(hardwareConfig2)
                                          .build();

        // Setup third dashboard config
        Dashboard dashboard3 = Dashboard.builder()
                                        .name("Streamlit Dashboard")
                                        .description("A dashboard using Streamlit")
                                        .framework("Streamlit")
                                        .command(null)
                                        .fileSource("git")
                                        .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                        .gitRepoBranch("main")
                                        .mainFilePath("streamlit/streamlit_dashboard.py")
                                        .build();

        DashboardScope dashboardSiteScope3 = DashboardScope.builder()
                                                           .scope(Site)
                                                           .enabled(false)
                                                           .ids(new HashSet<>(Collections.emptyList()))
                                                           .build();

        DashboardScope dashboardProjectScope3 = DashboardScope.builder()
                                                              .scope(Project)
                                                              .enabled(false)
                                                              .ids(new HashSet<>(new ArrayList<>(Collections.singletonList("Project1"))))
                                                              .build();

        DashboardScope dashboardDatatypeScope3 = DashboardScope.builder()
                                                               .scope(DataType)
                                                               .enabled(false)
                                                               .ids(new HashSet<>(Arrays.asList("xnat:projectData", "xnat:subjectData")))
                                                               .build();

        Map<Scope, DashboardScope> dashboardScopes3 = new HashMap<>();
        dashboardScopes3.put(Site, dashboardSiteScope3);
        dashboardScopes3.put(Project, dashboardProjectScope3);
        dashboardScopes3.put(DataType, dashboardDatatypeScope3);

        dashboardConfig3 = DashboardConfig.builder()
                                          .dashboard(dashboard3)
                                          .scopes(dashboardScopes3)
                                          .computeEnvironmentConfig(computeEnvironmentConfig3)
                                          .hardwareConfig(hardwareConfig3)
                                          .build();
    }

    public void commitDashboardFrameworks() {
        panel = dashboardFrameworkService.create(panel);
        streamlit = dashboardFrameworkService.create(streamlit);
        voila = dashboardFrameworkService.create(voila);
        dash = dashboardFrameworkService.create(dash);

        commitTransaction();
    }

    public void commitComputeEnvironmentAndHardwareConfigs() {
        // First create hardware configs
        hardwareConfig1 = hardwareConfigService.create(hardwareConfig1);
        hardwareConfig2 = hardwareConfigService.create(hardwareConfig2);
        hardwareConfig3 = hardwareConfigService.create(hardwareConfig3);

        commitTransaction();

        // Then create compute environment configs
        computeEnvironmentConfig1 = computeEnvironmentConfigService.create(computeEnvironmentConfig1);
        computeEnvironmentConfig2 = computeEnvironmentConfigService.create(computeEnvironmentConfig2);
        computeEnvironmentConfig3 = computeEnvironmentConfigService.create(computeEnvironmentConfig3);

        commitTransaction();

        // The dashboard configs need to be updated with the new compute environment and hardware configs (for the ids)
        dashboardConfig1.setComputeEnvironmentConfig(computeEnvironmentConfig1);
        dashboardConfig1.setHardwareConfig(hardwareConfig1);

        dashboardConfig2.setComputeEnvironmentConfig(computeEnvironmentConfig3);
        dashboardConfig2.setHardwareConfig(hardwareConfig3);

        dashboardConfig3.setComputeEnvironmentConfig(computeEnvironmentConfig3);
        dashboardConfig3.setHardwareConfig(hardwareConfig3);
    }

}