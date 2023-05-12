package org.nrg.jobtemplates.services;

import org.nrg.jobtemplates.models.JobTemplate;

public interface JobTemplateService {

    boolean isAvailable(String user, String project, Long computeSpecConfigId, Long hardwareConfigId);
    JobTemplate resolve(String user, String project, Long computeSpecConfigId, Long hardwareConfigId);

}
