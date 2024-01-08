package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.DashboardFramework;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "xhbm_dashboard_framework_entity", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@Slf4j
public class DashboardFrameworkEntity extends AbstractHibernateEntity {

    private String name;
    private String commandTemplate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 4096)
    public String getCommandTemplate() {
        return commandTemplate;
    }

    public void setCommandTemplate(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }

    /**
     * Creates a new entity from the pojo representation.
     * @param pojo The pojo representation of the entity.
     * @return The entity representation of the pojo.
     */
    public static DashboardFrameworkEntity fromPojo(DashboardFramework pojo) {
        final DashboardFrameworkEntity entity = new DashboardFrameworkEntity();
        entity.update(pojo);
        return entity;
    }

    /**
     * Updates the entity with the pojo representation. Does not update the ID.
     * @param pojo The pojo representation of the entity.
     */
    public void update(DashboardFramework pojo) {
        // Don't update the ID, that's immutable
        this.setName(pojo.getName());
        this.setCommandTemplate(pojo.getCommandTemplate());
    }

    /**
     * Converts the entity to a pojo representation.
     * @return The pojo representation of the entity.
     */
    public DashboardFramework toPojo() {
        return DashboardFramework.builder()
                .id(this.getId())
                .name(this.getName())
                .commandTemplate(this.getCommandTemplate())
                .build();
    }

}
