package org.nrg.xnat.compute.services;

import org.nrg.xnat.compute.models.JobTemplate;

public interface JobTemplateService {

    boolean isAvailable(String user, String project, Long computeSpecConfigId, Long hardwareConfigId);
    JobTemplate resolve(String user, String project, Long computeSpecConfigId, Long hardwareConfigId);

}