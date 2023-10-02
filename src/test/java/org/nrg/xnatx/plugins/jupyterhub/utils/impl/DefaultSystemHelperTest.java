package org.nrg.xnatx.plugins.jupyterhub.utils.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultSystemHelperTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultSystemHelperTestConfig.class)
public class DefaultSystemHelperTest {

    @Autowired private DefaultSystemHelper systemHelper;

    @Test
    public void testNotNull() {
        assertNotNull(systemHelper);
    }

    @Test
    public void test() {
        systemHelper.getEnv("PATH");
    }

}