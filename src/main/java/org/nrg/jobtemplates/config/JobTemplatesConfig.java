package org.nrg.jobtemplates.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.jobtemplates.services.impl",
                "org.nrg.jobtemplates.services",
                "org.nrg.jobtemplates.rest",
                "org.nrg.jobtemplates.repositories"})
public class JobTemplatesConfig {
}
