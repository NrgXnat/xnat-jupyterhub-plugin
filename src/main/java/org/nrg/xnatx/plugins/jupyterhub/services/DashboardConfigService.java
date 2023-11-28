package org.nrg.xnatx.plugins.jupyterhub.services;

import org.nrg.framework.constants.Scope;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DashboardConfigService {

    boolean exists(Long id);
    Optional<DashboardConfig> retrieve(Long id);
    List<DashboardConfig> getAll();
    DashboardConfig create(DashboardConfig dashboardConfig);
    DashboardConfig update(DashboardConfig dashboardConfig) throws NotFoundException;
    void delete(Long id) throws NotFoundException;
    boolean isAvailable(Long id, Map<Scope, String> executionScope);
    List<DashboardConfig> getAvailable(Map<Scope, String> executionScope);
    boolean isValid(DashboardConfig dashboardConfig);

    void enableForSite(Long id) throws NotFoundException;
    void disableForSite(Long id) throws NotFoundException;
    void enableForProject(Long id, String projectId) throws NotFoundException;
    void disableForProject(Long id, String projectId) throws NotFoundException;

}
