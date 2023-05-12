package org.nrg.jobtemplates.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.jobtemplates.models.ComputeSpecConfig;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ComputeSpecConfigEntity extends AbstractHibernateEntity {

    private Set<String> configTypes;

    private ComputeSpecEntity computeSpec;
    private Map<Scope, ComputeSpecScopeEntity> scopes;
    private ComputeSpecHardwareOptionsEntity hardwareOptions;

    @ElementCollection
    public Set<String> getConfigTypes() {
        if (configTypes == null) {
            configTypes = new HashSet<>();
        }

        return configTypes;
    }

    public void setConfigTypes(Set<String> configTypes) {
        this.configTypes = configTypes;
    }

    @OneToOne(mappedBy = "computeSpecConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public ComputeSpecEntity getComputeSpec() {
        return computeSpec;
    }

    public void setComputeSpec(ComputeSpecEntity computeSpec) {
        computeSpec.setComputeSpecConfig(this);
        this.computeSpec = computeSpec;
    }

    @OneToMany(mappedBy = "computeSpecConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public Map<Scope, ComputeSpecScopeEntity> getScopes() {
        return scopes;
    }

    public void setScopes(Map<Scope, ComputeSpecScopeEntity> scopes) {
        this.scopes = scopes;
    }

    @OneToOne(mappedBy = "computeSpecConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public ComputeSpecHardwareOptionsEntity getHardwareOptions() {
        return hardwareOptions;
    }

    public void setHardwareOptions(ComputeSpecHardwareOptionsEntity hardwareOptions) {
        this.hardwareOptions = hardwareOptions;
    }

    /**
     * Creates a new entity from the pojo.
     * @param pojo The pojo to create the entity from
     * @return The newly created entity
     */
    public static ComputeSpecConfigEntity fromPojo(final ComputeSpecConfig pojo) {
        final ComputeSpecConfigEntity entity = new ComputeSpecConfigEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Creates a new pojo from the entity.
     * @return The pojo created from the entity
     */
    public ComputeSpecConfig toPojo() {
        return ComputeSpecConfig.builder()
                .id(getId())
                .configTypes(getConfigTypes()
                                     .stream()
                                     .map(ComputeSpecConfig.ConfigType::valueOf)
                                     .collect(Collectors.toSet()))
                .computeSpec(getComputeSpec().toPojo())
                .scopes(getScopes()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toPojo())))
                .hardwareOptions(getHardwareOptions().toPojo())
                .build();
    }

    /**
     * Updates the entity with the values from the pojo. Does not update the hardwareConfigs, since that is a
     * many-to-many relationship and needs to be handled separately.
     * @param pojo The pojo to update the entity with
     */
    public void update(final ComputeSpecConfig pojo) {
        setConfigTypes(pojo.getConfigTypes()
                              .stream()
                              .map(Enum::name)
                              .collect(Collectors.toSet()));

        if (getComputeSpec() == null) {
            // This is a new entity, so we need to create the computeSpec entity
            setComputeSpec(ComputeSpecEntity.fromPojo(pojo.getComputeSpec()));
        } else {
            // This is an existing entity, so we need to update the computeSpec entity
            getComputeSpec().update(pojo.getComputeSpec());
        }

        if (getScopes() == null) {
            // This is a new entity, so we need to create the scopes entity
            setScopes(pojo.getScopes()
                              .entrySet()
                              .stream()
                              .collect(Collectors.toMap(Map.Entry::getKey, e -> ComputeSpecScopeEntity.fromPojo(e.getValue()))));
        } else {
            // This is an existing entity, so we need to update the scopes entities
            getScopes().forEach((key, value) -> value.update(pojo.getScopes().get(key)));
        }

        // Set the computeSpecConfig on the scopes
        getScopes().values().forEach(s -> s.setComputeSpecConfig(this));

        if (getHardwareOptions() == null) {
            // This is a new entity, so we need to create the hardwareOptions entity
            ComputeSpecHardwareOptionsEntity computeSpecHardwareOptionsEntity = ComputeSpecHardwareOptionsEntity.fromPojo(pojo.getHardwareOptions());
            setHardwareOptions(computeSpecHardwareOptionsEntity);
            computeSpecHardwareOptionsEntity.setComputeSpecConfig(this);
        } else {
            // This is an existing entity, so we need to update the hardwareOptions entity
            getHardwareOptions().update(pojo.getHardwareOptions());
        }
    }

}
