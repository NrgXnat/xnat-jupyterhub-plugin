package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultDashboardFrameworkServiceTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardFrameworkEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.nrg.xnatx.plugins.jupyterhub.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = DefaultDashboardFrameworkServiceTestConfig.class)
public class DefaultDashboardFrameworkServiceTest {

    @Autowired private DefaultDashboardFrameworkService service;
    @Autowired private DashboardFrameworkEntityService entityService;

    private DashboardFramework panel;
    private DashboardFramework streamlit;
    private List<DashboardFramework> frameworks;

    @Before
    public void setup() {
        panel = DashboardFramework.builder()
                                  .name("Panel")
                                  .commandTemplate("jhsingle-native-proxy --destport {port} --repo {repo} --repobranch {repobranch} --repofolder /home/jovyan/work  bokeh_root_cmd.main /home/jovyan/work/{mainFilePath}")
                                  .build();
        streamlit = DashboardFramework.builder()
                                      .name("Streamlit")
                                      .commandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug")
                                      .build();

        frameworks = Arrays.asList(panel, streamlit);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void test() {
        assertNotNull(service);
        assertNotNull(entityService);
    }

    @Test
    @DirtiesContext
    public void testCreate() {
        // Setup and execute
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Verify
        assertThat(created.size(), is(frameworks.size()));
        for (DashboardFramework framework : created) {
            assertThat(frameworks.contains(framework), is(true));
        }
    }

    @Test
    @DirtiesContext
    public void testUpdate() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        List<DashboardFramework> updated = new ArrayList<>();
        for (DashboardFramework framework : created) {
            framework.setCommandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug");
            updated.add(service.update(framework));
        }

        commitTransaction();

        // Verify
        assertThat(updated.size(), is(created.size()));
        for (DashboardFramework framework : updated) {
            assertThat(created.contains(framework), is(true));
        }
    }

    @Test
    @DirtiesContext
    public void testGet() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        List<DashboardFramework> retrieved = new ArrayList<>();
        for (DashboardFramework framework : created) {
            retrieved.add(service.get(framework.getId()).get());
        }

        // Verify
        assertThat(retrieved.size(), is(created.size()));
        for (DashboardFramework framework : retrieved) {
            assertThat(created.contains(framework), is(true));
        }
    }

    @Test
    @DirtiesContext
    public void testGetByName() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        List<DashboardFramework> retrieved = new ArrayList<>();
        for (DashboardFramework framework : created) {
            retrieved.add(service.get(framework.getName()).get());
        }

        // Verify
        assertThat(retrieved.size(), is(created.size()));
        for (DashboardFramework framework : retrieved) {
            assertThat(created.contains(framework), is(true));
        }
    }

    @Test
    @DirtiesContext
    public void testGetAll() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        List<DashboardFramework> retrieved = service.getAll();

        // Verify
        assertThat(retrieved.size(), is(created.size()));
        for (DashboardFramework framework : retrieved) {
            assertThat(created.contains(framework), is(true));
        }
    }

    @Test
    @DirtiesContext
    public void testDelete() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        for (DashboardFramework framework : created) {
            service.delete(framework.getId());
        }

        commitTransaction();

        // Verify
        assertThat(service.getAll().size(), is(0));
    }

    @Test
    @DirtiesContext
    public void testDeleteByName() throws Exception {
        // Setup
        List<DashboardFramework> created = commitDashboardFrameworks();

        // Execute
        for (DashboardFramework framework : created) {
            service.delete(framework.getName());
        }

        commitTransaction();

        // Verify
        assertThat(service.getAll().size(), is(0));
    }

    @Test
    @DirtiesContext
    public void test_resolve() {
        // Setup
        Dashboard dashboard = Dashboard.builder()
                                       .name("Test Dashboard")
                                       .description("Test dashboard description")
                                       .framework("Panel")
                                       .command("")
                                       .fileSource("git")
                                       .gitRepoUrl("https://github.com/andylassiter/dashboard-testing.git")
                                       .gitRepoBranch("main")
                                       .mainFilePath("panel/subject-demographics.py")
                                       .build();

        commitDashboardFrameworks();

        // Execute
        String command = service.resolveCommand(dashboard);

        // Verify
        assertThat(command, containsString(dashboard.getGitRepoUrl()));
        assertThat(command, containsString(dashboard.getGitRepoBranch()));
        assertThat(command, containsString(dashboard.getMainFilePath()));
    }

    @Test
    public void test_removeExtraSpaces() {
        // Setup
        String command = "jhsingle-native-proxy       --destport         {port} --repo {repo} --repobranch {repobranch} --repofolder /home/jovyan/work   bokeh_root_cmd.main       /home/jovyan/work/{mainFilePath}";
        String expected = "jhsingle-native-proxy --destport {port} --repo {repo} --repobranch {repobranch} --repofolder /home/jovyan/work bokeh_root_cmd.main /home/jovyan/work/{mainFilePath}";

        // Execute
        String result = service.removeExtraSpaces(command);

        // Verify
        assertThat(result, is(expected));
    }

    public List<DashboardFramework> commitDashboardFrameworks() {
        List<DashboardFramework> created = new ArrayList<>();
        for (DashboardFramework framework : frameworks) {
            DashboardFramework temp = service.create(framework);
            framework.setId(temp.getId());
            created.add(temp);
        }

        commitTransaction();

        return created;
    }

}