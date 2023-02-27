package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnatx.plugins.jupyterhub.models.Profile;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.ContainerSpec;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.Placement;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.Resources;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;
import org.nrg.xnatx.plugins.jupyterhub.services.ProfileService;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class JupyterHubProfileInitializer extends AbstractInitializingTask {

    private final ProfileService profileService;
    private final XFTManagerHelper xfmManagerHelper;

    @Autowired
    public JupyterHubProfileInitializer(final ProfileService profileService,
                                        final XFTManagerHelper xfmManagerHelper) {
        this.profileService = profileService;
        this.xfmManagerHelper = xfmManagerHelper;
    }

    @Override
    public String getTaskName() {
        return "JupyterHubProfileInitializer";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing JupyterHub profile.");

        if (!xfmManagerHelper.isInitialized()) {
            log.debug("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (profileService.getAll().size() > 0) {
            log.debug("JupyterHub profile already exists. Skipping initialization.");
            return;
        }

        Profile profile = buildDefaultProfile();

        try {
            profileService.create(profile);
            log.info("Created default JupyterHub profile.");
        } catch (DataFormatException e) {
            log.error("Error creating default JupyterHub profile.", e);
        }
    }

    private Profile buildDefaultProfile() {
        ContainerSpec containerSpec = ContainerSpec.builder()
                .image("jupyter/datascience-notebook:hub-3.0.0")
                .mounts(Collections.emptyList())
                .env(Collections.emptyMap())
                .labels(Collections.emptyMap())
                .build();

        Placement placement = Placement.builder()
                .constraints(Collections.emptyList())
                .build();

        Resources resources = Resources.builder()
                .cpuLimit(null)
                .cpuReservation(null)
                .memLimit(null)
                .memReservation(null)
                .genericResources(Collections.emptyMap())
                .build();

        TaskTemplate taskTemplate = TaskTemplate.builder()
                .containerSpec(containerSpec)
                .placement(placement)
                .resources(resources)
                .build();

        return Profile.builder()
                .name("Default")
                .description("Default JupyterHub profile.")
                .taskTemplate(taskTemplate)
                .build();
    }

}
