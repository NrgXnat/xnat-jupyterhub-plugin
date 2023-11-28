package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.JobTemplate;
import org.nrg.xnat.compute.services.JobTemplateService;

import java.util.Map;

public interface DashboardJobTemplateService extends JobTemplateService {

    boolean isAvailable(Long dashboardConfigId, Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope);
    JobTemplate resolve(Long dashboardConfigId, Long computeEnvironmentConfigId, Long hardwareConfigId, Map<Scope, String> executionScope);

}
