package org.nrg.xnat.compute.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xnat.compute.config.HibernateHardwareConfigEntityServiceTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = HibernateHardwareConfigEntityServiceTestConfig.class)
public class HibernateHardwareConfigEntityServiceTest {

    @Autowired private HibernateHardwareConfigEntityService hibernateHardwareConfigEntityService;

    @Test
    public void test() {
        assertNotNull(hibernateHardwareConfigEntityService);
    }

}