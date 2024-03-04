package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.config.HibernateDashboardFrameworkEntityServiceTestConfig;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardFrameworkEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;
import org.nrg.xnatx.plugins.jupyterhub.repositories.DashboardFrameworkEntityDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.nrg.xnatx.plugins.jupyterhub.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateDashboardFrameworkEntityServiceTestConfig.class)
public class HibernateDashboardFrameworkEntityServiceTest {

    @Autowired private HibernateDashboardFrameworkEntityService entityService;
    @Qualifier("dashboardFrameworkEntityDaoImpl") @Autowired private DashboardFrameworkEntityDao dao;

    private DashboardFramework panel;
    private DashboardFramework streamlit;

    @Before
    public void setup() {
        entityService.setDao(dao);

        panel = DashboardFramework.builder()
                .name("Panel")
                .commandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug")
                .build();
        streamlit = DashboardFramework.builder()
                .name("Streamlit")
                .commandTemplate("jhsingle-native-proxy --destport {port} --authtype none --user {username} --group {group} --debug")
                .build();
    }

    @After
    public void after() {
        Mockito.reset();
    }

    @Test
    public void test() {
        assertNotNull(entityService);
        assertNotNull(dao);
    }

    @Test
    @DirtiesContext
    public void testCreate() throws NotFoundException {
        // Setup
        DashboardFrameworkEntity panelEntity = DashboardFrameworkEntity.fromPojo(panel);
        DashboardFrameworkEntity streamlitEntity = DashboardFrameworkEntity.fromPojo(streamlit);

        // Execute
        DashboardFrameworkEntity createdPanel = entityService.create(panelEntity);
        DashboardFrameworkEntity createdStreamlit = entityService.create(streamlitEntity);

        commitTransaction();

        DashboardFrameworkEntity retrievedPanel = entityService.get(createdPanel.getId());
        DashboardFrameworkEntity retrievedStreamlit = entityService.get(createdStreamlit.getId());

        // Verify
        assertEquals(2, entityService.getAll().size());
        assertThat(retrievedPanel.toPojo(), is(createdPanel.toPojo()));
        assertThat(retrievedStreamlit.toPojo(), is(createdStreamlit.toPojo()));
    }

    @Test
    @DirtiesContext
    public void testFindFrameworkByName() {
        // Setup
        DashboardFrameworkEntity panelEntity = DashboardFrameworkEntity.fromPojo(panel);
        DashboardFrameworkEntity streamlitEntity = DashboardFrameworkEntity.fromPojo(streamlit);

        // Execute
        DashboardFrameworkEntity createdPanel = entityService.create(panelEntity);
        DashboardFrameworkEntity createdStreamlit = entityService.create(streamlitEntity);

        // Update pojo IDs
        panel.setId(createdPanel.getId());
        streamlit.setId(createdStreamlit.getId());

        commitTransaction();

        Optional<DashboardFrameworkEntity> retrievedPanel = entityService.findFrameworkByName(panel.getName());
        Optional<DashboardFrameworkEntity> retrievedStreamlit = entityService.findFrameworkByName(streamlit.getName());

        // Verify
        assertTrue(retrievedPanel.isPresent());
        assertTrue(retrievedStreamlit.isPresent());
        assertThat(retrievedPanel.get().toPojo(), is(panel));
        assertThat(retrievedStreamlit.get().toPojo(), is(streamlit));
    }

}