package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultHardwareConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class DashboardHardwareConfigService extends DefaultHardwareConfigService {

    private final DashboardConfigEntityService dashboardConfigEntityService;

    public DashboardHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                          final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                          final DashboardConfigEntityService dashboardConfigEntityService) {
        super(hardwareConfigEntityService, computeEnvironmentConfigEntityService);
        this.dashboardConfigEntityService = dashboardConfigEntityService;
    }

    /**
     * Don't allow the user to delete a HardwareConfig if it is in use by a DashboardConfig.
     * @param id The ID of the HardwareConfig to delete.
     * @throws RuntimeException if the HardwareConfig is in use by a DashboardConfig.
     * @throws NotFoundException if the HardwareConfig does not exist.
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (dashboardConfigEntityService.isHardwareConfigInUse(id)) {
            log.error("Cannot delete HardwareConfig with ID {} because it is in use by a DashboardConfig.", id);
            throw new RuntimeException("Cannot delete HardwareConfig because it is in use by a DashboardConfig.");
        }

        super.delete(id);
    }
}
