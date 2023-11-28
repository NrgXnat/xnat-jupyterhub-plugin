package org.nrg.xnatx.plugins.jupyterhub.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnatx.plugins.jupyterhub.entities.DashboardEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.Dashboard;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardEntityService;
import org.nrg.xnatx.plugins.jupyterhub.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DefaultDashboardService implements DashboardService {

    private final DashboardEntityService dashboardEntityService;

    @Autowired
    public DefaultDashboardService(final DashboardEntityService dashboardEntityService) {
        super();
        this.dashboardEntityService = dashboardEntityService;
    }

    @Override
    public Dashboard create(Dashboard dashboard) {
        DashboardEntity entity = dashboardEntityService.create(DashboardEntity.fromPojo(dashboard));
        return entity.toPojo();
    }

    @Override
    public Dashboard update(Dashboard dashboard) throws NotFoundException {
        Optional<DashboardEntity> existing = Optional.ofNullable(dashboardEntityService.retrieve(dashboard.getId()));

        if (existing.isPresent()) {
            existing.get().update(dashboard);
            dashboardEntityService.update(existing.get());
        } else {
            throw new NotFoundException("Dashboard not found");
        }

        return dashboardEntityService.retrieve(dashboard.getId()).toPojo();
    }

    @Override
    public Optional<Dashboard> get(Long id) {
        return Optional.ofNullable(dashboardEntityService.retrieve(id))
                       .map(DashboardEntity::toPojo);
    }

    @Override
    public List<Dashboard> getAll() {
        return dashboardEntityService.getAll().stream()
                                     .map(DashboardEntity::toPojo)
                                     .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        dashboardEntityService.delete(id);
    }

}
