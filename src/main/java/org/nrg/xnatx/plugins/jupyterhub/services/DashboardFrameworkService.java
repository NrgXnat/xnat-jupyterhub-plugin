package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;

import java.util.List;
import java.util.Optional;

public interface DashboardFrameworkService {

    DashboardFramework create(DashboardFramework framework);
    DashboardFramework update(DashboardFramework framework) throws NotFoundException;
    Optional<DashboardFramework> get(Long id);
    Optional<DashboardFramework> get(String name);
    List<DashboardFramework> getAll();
    void delete(Long id);
    void delete(String name);
    String resolveCommand(Dashboard dashboard);

}
