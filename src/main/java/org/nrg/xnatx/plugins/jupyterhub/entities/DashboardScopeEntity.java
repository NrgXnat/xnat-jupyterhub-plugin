package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardScope;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "xhbm_dashboard_scope_entity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DashboardScopeEntity {

    private long id;
    private String scope;
    private boolean enabled;
    private Set<String> ids;

    @ToString.Exclude @EqualsAndHashCode.Exclude private DashboardConfigEntity dashboardConfig;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ElementCollection
    public Set<String> getIds() {
        if (ids == null) {
            ids = new HashSet<>();
        }

        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    @ManyToOne
    public DashboardConfigEntity getDashboardConfig() {
        return dashboardConfig;
    }

    public void setDashboardConfig(final DashboardConfigEntity dashboardConfig) {
        this.dashboardConfig = dashboardConfig;
    }

    /**
     * Creates a new DashboardScopeEntity from the given pojo.
     * @param pojo The pojo to create the entity from
     * @return The newly created entity
     */
    public static DashboardScopeEntity fromPojo(final DashboardScope pojo) {
        final DashboardScopeEntity entity = new DashboardScopeEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Updates this entity with the values from the given pojo.
     * @param pojo The pojo to update from
     */
    public void update(final DashboardScope pojo) {
        this.setScope(pojo.getScope().toString());
        this.setEnabled(pojo.isEnabled());

        // clear ids then add new ids
        this.getIds().clear();

        if (pojo.getIds() != null && !pojo.getIds().isEmpty()) {
            this.getIds().addAll(pojo.getIds());
        }
    }

    /**
     * Converts this entity to a pojo.
     * @return The pojo
     */
    public DashboardScope toPojo() {
        return DashboardScope.builder()
                             .scope(Scope.valueOf(this.getScope()))
                             .enabled(this.isEnabled())
                             .ids(new HashSet<>(this.getIds()))
                             .build();
    }

}
