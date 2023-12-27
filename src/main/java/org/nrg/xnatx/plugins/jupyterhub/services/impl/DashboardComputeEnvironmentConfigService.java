package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultComputeEnvironmentConfigService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardConfigEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class DashboardComputeEnvironmentConfigService extends DefaultComputeEnvironmentConfigService {

    private final DashboardConfigEntityService dashboardConfigEntityService;

    @Autowired
    public DashboardComputeEnvironmentConfigService(final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                    final HardwareConfigEntityService hardwareConfigEntityService,
                                                    final DashboardConfigEntityService dashboardConfigEntityService) {
        super(computeEnvironmentConfigEntityService, hardwareConfigEntityService);
        this.dashboardConfigEntityService = dashboardConfigEntityService;
    }

    /**
     * Don't allow the user to delete a ComputeEnvironmentConfig if it is in use by a DashboardConfig.
     * @param id The ID of the ComputeEnvironmentConfig to delete.
     * @throws RuntimeException if the ComputeEnvironmentConfig is in use by a DashboardConfig.
     * @throws NotFoundException if the ComputeEnvironmentConfig does not exist.
     */
    @Override
    public void delete(Long id) throws NotFoundException {
        if (dashboardConfigEntityService.isComputeEnvironmentConfigInUse(id)) {
            log.error("Cannot delete ComputeEnvironmentConfig with ID {} because it is in use by a DashboardConfig.", id);
            throw new RuntimeException("Cannot delete ComputeEnvironmentConfig because it is in use by a DashboardConfig.");
        }

        super.delete(id);
    }

}
