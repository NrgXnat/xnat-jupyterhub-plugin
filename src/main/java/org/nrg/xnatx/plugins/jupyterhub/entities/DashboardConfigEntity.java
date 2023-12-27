package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.entities.ComputeEnvironmentConfigEntity;
import org.nrg.xnat.compute.entities.HardwareConfigEntity;
import org.nrg.xnat.compute.models.ComputeEnvironment;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.models.Hardware;
import org.nrg.xnat.compute.models.HardwareConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardConfig;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardScope;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "xhbm_dashboard_config_entity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class DashboardConfigEntity extends AbstractHibernateEntity {

    private DashboardEntity dashboard;
    private Map<Scope, DashboardScopeEntity> scopes;
    private ComputeEnvironmentConfigEntity computeEnvironmentConfig;
    private HardwareConfigEntity hardwareConfig;

    @OneToOne(mappedBy = "dashboardConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public DashboardEntity getDashboard() {
        return dashboard;
    }

    public void setDashboard(final DashboardEntity dashboard) {
        this.dashboard = dashboard;
    }

    @OneToMany(mappedBy = "dashboardConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public Map<Scope, DashboardScopeEntity> getScopes() {
        return scopes;
    }

    public void setScopes(final Map<Scope, DashboardScopeEntity> scopes) {
        this.scopes = scopes;
    }

    @OneToOne
    @JoinColumn(name = "compute_environment_config_id", referencedColumnName = "id")
    public ComputeEnvironmentConfigEntity getComputeEnvironmentConfig() {
        return computeEnvironmentConfig;
    }

    public void setComputeEnvironmentConfig(final ComputeEnvironmentConfigEntity computeEnvironmentConfig) {
        this.computeEnvironmentConfig = computeEnvironmentConfig;
    }

    @OneToOne
    @JoinColumn(name = "hardware_config_id", referencedColumnName = "id")
    public HardwareConfigEntity getHardwareConfig() {
        return hardwareConfig;
    }

    public void setHardwareConfig(final HardwareConfigEntity hardwareConfig) {
        this.hardwareConfig = hardwareConfig;
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo from which to create the entity.
     * @return The newly created entity.
     */
    public static DashboardConfigEntity fromPojo(final DashboardConfig pojo) {
        final DashboardConfigEntity entity = new DashboardConfigEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Converts the entity to a pojo. The compute environment config and hardware config are converted to pojos
     * with only the id and name set.
     * @return The pojo representation of the entity.
     */
    public DashboardConfig toPojo() {
        // Only the id and name is needed for the compute environment config and hardware config
        ComputeEnvironmentConfig computeEnvironmentConfig = ComputeEnvironmentConfig.builder()
                                                                                    .id(getComputeEnvironmentConfig().getId())
                                                                                    .computeEnvironment(ComputeEnvironment.builder()
                                                                                                                          .name(getComputeEnvironmentConfig().getComputeEnvironment().getName())
                                                                                                                          .build())
                                                                                    .build();

        HardwareConfig hardwareConfig = HardwareConfig.builder()
                                                      .id(getHardwareConfig().getId())
                                                      .hardware(Hardware.builder()
                                                                        .name(getHardwareConfig().getHardware().getName())
                                                                        .build())
                                                      .build();

        return DashboardConfig.builder()
                .id(getId())
                .dashboard(getDashboard().toPojo())
                .scopes(getScopes().entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toPojo())))
                .computeEnvironmentConfig(computeEnvironmentConfig)
                .hardwareConfig(hardwareConfig)
                .build();
    }

    /**
     * Updates the entity with the values from the given pojo. Updating the compute environment config and hardware
     * config is not supported. Updating the dashboard config -> dashboard -> dashboard framework relationship is not
     * supported and must be done externally.
     * @param pojo The pojo from which to update the entity.
     */
    public void update(final DashboardConfig pojo) {
        if (getDashboard() == null) {
            setDashboard(DashboardEntity.fromPojo(pojo.getDashboard()));
        } else {
            getDashboard().update(pojo.getDashboard());
        }

        getDashboard().setDashboardConfig(this);

        if (getScopes() == null) {
            // Create the entity scopes from the pojo scopes
            setScopes(pojo.getScopes()
                          .entrySet().stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, e -> DashboardScopeEntity.fromPojo(e.getValue()))));
        } else {
            // Update the existing entity scopes with the pojo scopes
            // Collect the existing entity scopes and the updated pojo scopes
            Map<Scope, DashboardScopeEntity> existingEntityScopes = getScopes();
            Map<Scope, DashboardScope> updatedPojoScopes = pojo.getScopes();

            // Collect and remove scopes that are no longer in the updated pojo but are in the existing entity
            Set<Scope> scopesToRemove = existingEntityScopes.keySet().stream()
                                                            .filter(scope -> !updatedPojoScopes.containsKey(scope))
                                                            .collect(Collectors.toSet());

            scopesToRemove.forEach(scope -> {
                DashboardScopeEntity scopeToRemove = existingEntityScopes.remove(scope);
                scopeToRemove.setDashboardConfig(null);
            });

            // Update existing and add new scopes
            updatedPojoScopes.forEach((scope, dashboardScope) -> {
                DashboardScopeEntity existingScope = existingEntityScopes.get(scope);
                if (existingScope != null) {
                    // Update the existing scope
                    existingScope.update(dashboardScope);
                } else {
                    // Add new scope
                    existingEntityScopes.put(scope, DashboardScopeEntity.fromPojo(dashboardScope));
                }
            });
        }

        // Set the dashboard config on the scope entities
        getScopes().values().forEach(s -> s.setDashboardConfig(this));

        // Updating the compute environment config and hardware config is not supported
    }

}
