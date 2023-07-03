package org.nrg.xnatx.plugins.jupyterhub.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.*;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.nrg.xnat.initialization.tasks.InitializingTaskException;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnatx.plugins.jupyterhub.utils.XFTManagerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Initialization task for creating the default JupyterHub ComputeEnvironment and Hardware configurations.
 */
@Component
@Slf4j
public class JupyterHubEnvironmentsAndHardwareInitializer extends AbstractInitializingTask {

    private final XFTManagerHelper xftManagerHelper;
    private final XnatAppInfo appInfo;
    private final ComputeEnvironmentConfigService computeEnvironmentConfigService;
    private final HardwareConfigService hardwareConfigService;

    @Autowired
    public JupyterHubEnvironmentsAndHardwareInitializer(final XFTManagerHelper xftManagerHelper,
                                                        final XnatAppInfo appInfo,
                                                        final ComputeEnvironmentConfigService computeEnvironmentConfigService,
                                                        final HardwareConfigService hardwareConfigService) {
        this.xftManagerHelper = xftManagerHelper;
        this.appInfo = appInfo;
        this.computeEnvironmentConfigService = computeEnvironmentConfigService;
        this.hardwareConfigService = hardwareConfigService;
    }

    @Override
    public String getTaskName() {
        return "JupyterHubJobTemplateInitializer";
    }

    /**
     * Builds the default ComputeEnvironment and Hardware configurations for JupyterHub.
     * @throws InitializingTaskException if the XFT or XNAT services are not initialized.
     */
    @Override
    protected void callImpl() throws InitializingTaskException {
        log.debug("Initializing ComputeEnvironments and Hardware for JupyterHub.");

        // Not sure if I need to check both of these or just one.
        if (!xftManagerHelper.isInitialized()) {
            log.debug("XFT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (!appInfo.isInitialized()) {
            log.debug("XNAT not initialized, deferring execution.");
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }

        if (computeEnvironmentConfigService.getByType(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB).size() > 0) {
            log.debug("ComputeEnvironments already exist. Skipping initialization.");
            return;
        }

        if (hardwareConfigService.retrieveAll().size() > 0) {
            log.debug("Hardware already exists. Skipping initialization.");
            return;
        }

        ComputeEnvironmentConfig computeEnvironmentConfig = buildDefaultComputeEnvironmentConfig();

        try {
            computeEnvironmentConfigService.create(computeEnvironmentConfig);
            log.info("Created default ComputeEnvironment for JupyterHub.");
        } catch (Exception e) {
            log.error("Error creating default ComputeEnvironment for JupyterHub.", e);
        }

        HardwareConfig smallHardwareConfig = buildSmallHardwareConfig();
        HardwareConfig mediumHardwareConfig = buildMediumHardwareConfig();
        HardwareConfig largeHardwareConfig = buildLargeHardwareConfig();
        HardwareConfig xlargeHardwareConfig = buildXLargeHardwareConfig();

        try {
            hardwareConfigService.create(smallHardwareConfig);
            hardwareConfigService.create(mediumHardwareConfig);
            hardwareConfigService.create(largeHardwareConfig);
            hardwareConfigService.create(xlargeHardwareConfig);
            log.info("Created default Hardware for JupyterHub.");
        } catch (Exception e) {
            log.error("Error creating default Hardware for JupyterHub.", e);
        }
    }

    /**
     * Builds the default ComputeEnvironment for JupyterHub.
     * @return the default ComputeEnvironment for JupyterHub.
     */
    private ComputeEnvironmentConfig buildDefaultComputeEnvironmentConfig() {
        // Initialize the ComputeEnvironmentConfig
        ComputeEnvironment computeEnvironment = new ComputeEnvironment();
        Map<Scope, ComputeEnvironmentScope> scopes = new HashMap<>();
        ComputeEnvironmentHardwareOptions hardwareOptions = new ComputeEnvironmentHardwareOptions();
        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder()
                .id(null)
                .configTypes(new HashSet<>(Collections.singletonList(ComputeEnvironmentConfig.ConfigType.JUPYTERHUB)))
                .computeEnvironment(computeEnvironment)
                .scopes(scopes)
                .hardwareOptions(hardwareOptions)
                .build();

        // Set the ComputeEnvironment values
        computeEnvironment.setName("XNAT Datascience Notebook");
        computeEnvironment.setImage("xnat/datascience-notebook:latest");
        computeEnvironment.setEnvironmentVariables(new ArrayList<>());
        computeEnvironment.setMounts(new ArrayList<>());

        // Set the ComputeEnvironmentHardwareOptions values
        hardwareOptions.setAllowAllHardware(true);
        hardwareOptions.setHardwareConfigs(new HashSet<>());

        // Set the ComputeEnvironmentScope values
        scopes.put(Scope.Site, new ComputeEnvironmentScope(Scope.Site, true, Collections.emptySet()));
        scopes.put(Scope.Project, new ComputeEnvironmentScope(Scope.Project, true, Collections.emptySet()));
        scopes.put(Scope.User, new ComputeEnvironmentScope(Scope.User, true, Collections.emptySet()));

        return computeEnvironmentConfig;
    }

    /**
     * Builds the small HardwareConfig for JupyterHub.
     * @return the small HardwareConfig for JupyterHub.
     */
    private HardwareConfig buildSmallHardwareConfig() {
        // Initialize the HardwareConfig
        Hardware hardware = new Hardware();
        Map<Scope, HardwareScope> scopes = new HashMap<>();
        HardwareConfig hardwareConfig = HardwareConfig.builder()
                .id(null)
                .hardware(hardware)
                .scopes(scopes)
                .build();

        // Set the Hardware values
        hardware.setName("Small");
        hardware.setCpuReservation(2.0);
        hardware.setCpuLimit(2.0);
        hardware.setMemoryReservation("2G");
        hardware.setMemoryLimit("2G");

        // Set the HardwareScope values
        scopes.put(Scope.Site, new HardwareScope(Scope.Site, true, Collections.emptySet()));
        scopes.put(Scope.Project, new HardwareScope(Scope.Project, true, Collections.emptySet()));
        scopes.put(Scope.User, new HardwareScope(Scope.User, true, Collections.emptySet()));

        return hardwareConfig;
    }

    /**
     * Builds the large HardwareConfig for JupyterHub.
     * @return the large HardwareConfig for JupyterHub.
     */
    private HardwareConfig buildMediumHardwareConfig() {
        // Initialize the HardwareConfig
        Hardware hardware = new Hardware();
        Map<Scope, HardwareScope> scopes = new HashMap<>();
        HardwareConfig hardwareConfig = HardwareConfig.builder()
                .id(null)
                .hardware(hardware)
                .scopes(scopes)
                .build();

        // Set the Hardware values
        hardware.setName("Medium");
        hardware.setCpuReservation(2.0);
        hardware.setCpuLimit(2.0);
        hardware.setMemoryReservation("4G");
        hardware.setMemoryLimit("4G");

        // Set the HardwareScope values
        scopes.put(Scope.Site, new HardwareScope(Scope.Site, true, Collections.emptySet()));
        scopes.put(Scope.Project, new HardwareScope(Scope.Project, true, Collections.emptySet()));
        scopes.put(Scope.User, new HardwareScope(Scope.User, true, Collections.emptySet()));

        return hardwareConfig;
    }

    /**
     * Builds the large HardwareConfig for JupyterHub.
     * @return the large HardwareConfig for JupyterHub.
     */
    private HardwareConfig buildLargeHardwareConfig() {
        // Initialize the HardwareConfig
        Hardware hardware = new Hardware();
        Map<Scope, HardwareScope> scopes = new HashMap<>();
        HardwareConfig hardwareConfig = HardwareConfig.builder()
                .id(null)
                .hardware(hardware)
                .scopes(scopes)
                .build();

        // Set the Hardware values
        hardware.setName("Large");
        hardware.setCpuReservation(2.0);
        hardware.setCpuLimit(2.0);
        hardware.setMemoryReservation("8G");
        hardware.setMemoryLimit("8G");

        // Set the HardwareScope values
        scopes.put(Scope.Site, new HardwareScope(Scope.Site, true, Collections.emptySet()));
        scopes.put(Scope.Project, new HardwareScope(Scope.Project, true, Collections.emptySet()));
        scopes.put(Scope.User, new HardwareScope(Scope.User, true, Collections.emptySet()));

        return hardwareConfig;
    }

    /**
     * Builds the xlarge HardwareConfig for JupyterHub.
     * @return the xlarge HardwareConfig for JupyterHub.
     */
    private HardwareConfig buildXLargeHardwareConfig() {
        // Initialize the HardwareConfig
        Hardware hardware = new Hardware();
        Map<Scope, HardwareScope> scopes = new HashMap<>();
        HardwareConfig hardwareConfig = HardwareConfig.builder()
                .id(null)
                .hardware(hardware)
                .scopes(scopes)
                .build();

        // Set the Hardware values
        hardware.setName("XLarge");
        hardware.setCpuReservation(4.0);
        hardware.setCpuLimit(4.0);
        hardware.setMemoryReservation("16G");
        hardware.setMemoryLimit("16G");

        // Set the HardwareScope values
        scopes.put(Scope.Site, new HardwareScope(Scope.Site, true, Collections.emptySet()));
        scopes.put(Scope.Project, new HardwareScope(Scope.Project, true, Collections.emptySet()));
        scopes.put(Scope.User, new HardwareScope(Scope.User, true, Collections.emptySet()));

        return hardwareConfig;
    }
}
