package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.ComputeSpecScope;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ComputeSpecScopeEntity {

    private long id;
    private String scope;
    private boolean enabled;
    private Set<String> ids;

    @ToString.Exclude @EqualsAndHashCode.Exclude private ComputeSpecConfigEntity computeSpecConfig;

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
    public ComputeSpecConfigEntity getComputeSpecConfig() {
        return computeSpecConfig;
    }

    public void setComputeSpecConfig(ComputeSpecConfigEntity computeSpecConfig) {
        this.computeSpecConfig = computeSpecConfig;
    }

    /**
     * Updates this entity with the values from the given pojo.
     * @param pojo The pojo to update from
     */
    public void update(ComputeSpecScope pojo) {
        setScope(pojo.getScope().name());
        setEnabled(pojo.isEnabled());

        getIds().clear();

        // add new ids
        if (pojo.getIds() != null && !pojo.getIds().isEmpty()) {
            getIds().addAll(pojo.getIds());
        }
    }

    /**
     * Converts this entity to a pojo.
     * @return The pojo
     */
    public ComputeSpecScope toPojo() {
        return ComputeSpecScope.builder()
                .scope(Scope.valueOf(getScope()))
                .enabled(isEnabled())
                .ids(getIds())
                .build();
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo to create from
     * @return The new entity
     */
    public static ComputeSpecScopeEntity fromPojo(ComputeSpecScope pojo) {
        final ComputeSpecScopeEntity entity = new ComputeSpecScopeEntity();
        entity.update(pojo);
        return entity;
    }

}
