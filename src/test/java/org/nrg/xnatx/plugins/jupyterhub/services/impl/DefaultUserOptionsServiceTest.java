package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.jupyterhub.config.DefaultUserOptionsServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DefaultUserOptionsServiceConfig.class)
public class DefaultUserOptionsServiceTest {

    @Autowired private DefaultUserOptionsService userOptionsService;

    private UserI user = mock(UserI.class);

    private static final List<String> projectIds = new ArrayList<>();
    static {
        projectIds.add("ProjectA");
        projectIds.add("ProjectB");
    }

    @Before
    public void before() {
    }

    // TODO Write tests

    @Test @Ignore
    public void testProjectPaths() {
        // How to test XnatProjectdata static methods and getRootrchivePath and getCurrentArc?
    }

    @Test @Ignore public void testSubjectPaths() {}
    @Test @Ignore public void testExperimentPaths() {}
    @Test @Ignore public void testStoredSearchPaths() {}



}