package org.nrg.xnatx.plugins.jupyterhub.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;

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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "servername"})})
@Slf4j
public class UserOptionsEntity extends AbstractHibernateEntity {

    private Integer userId;
    private String servername;
    private String xsiType;
    private String itemId;
    private String projectId;
    private String eventTrackingId;

    private TaskTemplate taskTemplate;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public TaskTemplate getTaskTemplate() {
        return taskTemplate;
    }

    public void setTaskTemplate(TaskTemplate taskTemplate) {
        this.taskTemplate = taskTemplate;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Column(columnDefinition = "TEXT")
    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    @Column(columnDefinition = "TEXT")
    public String getXsiType() {
        return xsiType;
    }

    public void setXsiType(String xsiType) {
        this.xsiType = xsiType;
    }

    @Column(columnDefinition = "TEXT")
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    @Column(columnDefinition = "TEXT")
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(columnDefinition = "TEXT")
    public String getEventTrackingId() {
        return eventTrackingId;
    }

    public void setEventTrackingId(String trackingId) {
        this.eventTrackingId = trackingId;
    }

    public XnatUserOptions toPojo() {
        return XnatUserOptions.builder()
                .userId(userId)
                .servername(servername)
                .xsiType(xsiType)
                .itemId(itemId)
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .taskTemplate(taskTemplate)
                .build();
    }

    public void update(final UserOptionsEntity update) {
        this.setUserId(update.getUserId());
        this.setServername(update.getServername());
        this.setXsiType(update.getXsiType());
        this.setItemId(update.getItemId());
        this.setProjectId(update.getProjectId());
        this.setEventTrackingId(update.getEventTrackingId());
        this.setTaskTemplate(update.getTaskTemplate());
    }
}
