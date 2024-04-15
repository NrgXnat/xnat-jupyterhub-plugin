package org.nrg.xnat.compute.entities;

import lombok.*;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.compute.models.HardwareConfig;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class HardwareConfigEntity extends AbstractHibernateEntity {

    private HardwareEntity hardware;
    private Map<Scope, HardwareScopeEntity> scopes;

    @ToString.Exclude @EqualsAndHashCode.Exclude private List<ComputeEnvironmentHardwareOptionsEntity> computeEnvironmentHardwareOptions;

    @OneToOne(mappedBy = "hardwareConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public HardwareEntity getHardware() {
        return hardware;
    }

    public void setHardware(HardwareEntity hardware) {
        hardware.setHardwareConfig(this);
        this.hardware = hardware;
    }

    @OneToMany(mappedBy = "hardwareConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    public Map<Scope, HardwareScopeEntity> getScopes() {
        return scopes;
    }

    public void setScopes(Map<Scope, HardwareScopeEntity> scopes) {
        this.scopes = scopes;
    }

    @ManyToMany(mappedBy = "hardwareConfigs")
    public List<ComputeEnvironmentHardwareOptionsEntity> getComputeEnvironmentHardwareOptions() {
        return computeEnvironmentHardwareOptions;
    }

    public void setComputeEnvironmentHardwareOptions(List<ComputeEnvironmentHardwareOptionsEntity> computeEnvironmentHardwareOptions) {
        this.computeEnvironmentHardwareOptions = computeEnvironmentHardwareOptions;
    }

    /**
     * Creates a new entity from the given pojo.
     * @param pojo The pojo from which to create the entity.
     * @return The newly created entity.
     */
    public static HardwareConfigEntity fromPojo(final HardwareConfig pojo) {
        final HardwareConfigEntity entity = new HardwareConfigEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Creates a new pojo from the given entity.
     * @return The newly created pojo.
     */
    public HardwareConfig toPojo() {
        return HardwareConfig.builder()
                .id(getId())
                .hardware(getHardware().toPojo())
                .scopes(getScopes()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toPojo())))
                .build();
    }

    /**
     * Updates the entity with the values from the given pojo.
     * @param pojo The pojo from which to update the entity.
     */
    public void update(final HardwareConfig pojo) {
        if (getHardware() == null) {
            // This is a new entity, so we need to create the hardware entity
            setHardware(HardwareEntity.fromPojo(pojo.getHardware()));
        } else {
            // This is an existing entity, so we need to update the hardware entity
            getHardware().update(pojo.getHardware());
        }

        // Set the hardware config on the hardware entity
        getHardware().setHardwareConfig(this);

        if (getScopes() == null) {
            // This is a new entity, so we need to create the scope entities
            setScopes(pojo.getScopes()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> HardwareScopeEntity.fromPojo(e.getValue()))));
        } else {
            // This is an existing entity, so we need to update the scope entities
            getScopes().forEach((key, value) -> value.update(pojo.getScopes().get(key)));
        }

        // Set the hardware config on the scope entities
        getScopes().forEach((key, value) -> value.setHardwareConfig(this));
    }

}
