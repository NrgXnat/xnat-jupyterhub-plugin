package org.nrg.xnat.compute.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.config.HibernateConstraintConfigEntityServiceTestConfig;
import org.nrg.xnat.compute.entities.ConstraintConfigEntity;
import org.nrg.xnat.compute.models.Constraint;
import org.nrg.xnat.compute.models.ConstraintConfig;
import org.nrg.xnat.compute.models.ConstraintScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nrg.xnat.compute.utils.TestingUtils.commitTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = HibernateConstraintConfigEntityServiceTestConfig.class)
public class HibernateConstraintConfigEntityServiceTest {

    @Autowired private HibernateConstraintConfigEntityService hibernateConstraintConfigEntityService;

    @Test
    public void test() {
        assertNotNull(hibernateConstraintConfigEntityService);
    }

    @Test
    @DirtiesContext
    public void testCreateConstraintConfig() {
        assertNotNull(hibernateConstraintConfigEntityService);

        // Create a constraint config
        Constraint constraint1 = Constraint.builder()
                .key("node.role")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("worker")))
                .build();

        ConstraintScope constraintScopeSite1 = ConstraintScope.builder()
                .scope(Scope.Site)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ConstraintScope constraintScopeProject1 = ConstraintScope.builder()
                .scope(Scope.Project)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        ConstraintScope constraintScopeUser1 = ConstraintScope.builder()
                .scope(Scope.User)
                .enabled(true)
                .ids(new HashSet<>())
                .build();

        Map<Scope, ConstraintScope> scopes1 = new HashMap<>();
        scopes1.put(Scope.Site, constraintScopeSite1);
        scopes1.put(Scope.Project, constraintScopeProject1);
        scopes1.put(Scope.User, constraintScopeUser1);

        ConstraintConfig constraintConfig = ConstraintConfig.builder()
                .constraint(constraint1)
                .scopes(scopes1)
                .build();

        // Save the constraint config
        ConstraintConfigEntity created = hibernateConstraintConfigEntityService.create(ConstraintConfigEntity.fromPojo(constraintConfig));

        commitTransaction();

        // Retrieve the constraint config
        ConstraintConfigEntity retrieved = hibernateConstraintConfigEntityService.retrieve(created.getId());

        // Check that the retrieved constraint config matches the original
        assertEquals(created, retrieved);
    }

}