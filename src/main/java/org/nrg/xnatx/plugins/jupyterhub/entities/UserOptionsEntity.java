package org.nrg.xnatx.plugins.jupyterhub.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xdat.XDAT;
import org.nrg.xnatx.plugins.jupyterhub.models.BindMount;
import org.nrg.xnatx.plugins.jupyterhub.models.XnatUserOptions;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    private String dockerImage;
    private Map<String, String> environmentVariables;
    private String bindMountsJson; // List<BindMounts> serialized to json string

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

    @Column(columnDefinition = "TEXT")
    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @Column(columnDefinition = "TEXT")
    public String getBindMountsJson() {
        return bindMountsJson;
    }

    public void setBindMountsJson(String bindMountsJson) {
        this.bindMountsJson = bindMountsJson;
    }

    public static String bindMountPojo(List<BindMount> bindMounts) {
        try {
            return XDAT.getSerializerService().getObjectMapper().writeValueAsString(bindMounts);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize bind mounts.", e);
            throw new RuntimeException(e);
        }
    }

    public List<BindMount> bindMountPojo() {
        try {
            CollectionType javaType = XDAT.getSerializerService().getObjectMapper().getTypeFactory()
                    .constructCollectionType(List.class, BindMount.class);

            return XDAT.getSerializerService().getObjectMapper().readValue(bindMountsJson, javaType);
        } catch (IOException e) {
            log.error("Unable to deserialize bind mounts.", e);
            throw new RuntimeException(e);
        }
    }

    public XnatUserOptions toPojo() {
        return XnatUserOptions.builder()
                .userId(userId)
                .servername(servername)
                .xsiType(xsiType)
                .itemId(itemId)
                .projectId(projectId)
                .eventTrackingId(eventTrackingId)
                .dockerImage(dockerImage)
                .environmentVariables(environmentVariables)
                .bindMounts(bindMountPojo())
                .build();
    }

    public void update(final UserOptionsEntity update) {
        this.setUserId(update.getUserId());
        this.setServername(update.getServername());
        this.setXsiType(update.getXsiType());
        this.setItemId(update.getItemId());
        this.setProjectId(update.getProjectId());
        this.setEventTrackingId(update.getEventTrackingId());
        this.setDockerImage(update.getDockerImage());
        this.setEnvironmentVariables(update.getEnvironmentVariables());
        this.setBindMountsJson(update.getBindMountsJson());
    }
}
