package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;

import java.util.List;
import java.util.Optional;

public interface DashboardService {

    Dashboard create(Dashboard dashboard);
    Dashboard update(Dashboard dashboard) throws NotFoundException;
    Optional<Dashboard> get(Long id);
    List<Dashboard> getAll();
    void delete(Long id);

}
