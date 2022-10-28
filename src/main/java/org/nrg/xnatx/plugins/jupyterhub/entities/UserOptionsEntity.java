package org.nrg.xnatx.plugins.jupyterhub.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xdat.XDAT;
import org.nrg.xnatx.plugins.jupyterhub.models.*;

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

    private Map<String, String> environmentVariables;
    private String bindMountsJson; // List<BindMounts> serialized to json string
    private String containerSpecJson; // container spec serialized to json string
    private String placementSpecJson; // container spec serialized to json string
    private String resourceSpecJson; // container spec serialized to json string

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

    @Column(columnDefinition = "TEXT")
    public String getContainerSpecJson() {
        return containerSpecJson;
    }

    public void setContainerSpecJson(String containerSpecJson) {
        this.containerSpecJson = containerSpecJson;
    }

    @Column(columnDefinition = "TEXT")
    public String getPlacementSpecJson() {
        return placementSpecJson;
    }

    public void setPlacementSpecJson(String placementSpecJson) {
        this.placementSpecJson = placementSpecJson;
    }

    @Column(columnDefinition = "TEXT")
    public String getResourceSpecJson() {
        return resourceSpecJson;
    }

    public void setResourceSpecJson(String resourceSpecJson) {
        this.resourceSpecJson = resourceSpecJson;
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

    public static String containerSpecPojo(ContainerSpec containerSpec) {
        try {
            return XDAT.getSerializerService().getObjectMapper().writeValueAsString(containerSpec);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize containerSpec.", e);
            throw new RuntimeException(e);
        }
    }

    public ContainerSpec containerSpecPojo() {
        try {
            return XDAT.getSerializerService().getObjectMapper().readValue(containerSpecJson, ContainerSpec.class);
        } catch (IOException e) {
            log.error("Unable to deserialize containerSpec.", e);
            throw new RuntimeException(e);
        }
    }

    public PlacementSpec placementSpecPojo() {
        try {
            return XDAT.getSerializerService().getObjectMapper().readValue(placementSpecJson, PlacementSpec.class);
        } catch (IOException e) {
            log.error("Unable to deserialize containerSpec.", e);
            throw new RuntimeException(e);
        }
    }


    public static String placementSpecPojo(PlacementSpec placementSpec) {
        try {
            return XDAT.getSerializerService().getObjectMapper().writeValueAsString(placementSpec);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize placementSpec.", e);
            throw new RuntimeException(e);
        }
    }


    public ResourceSpec resourceSpecPojo() {
        try {
            return XDAT.getSerializerService().getObjectMapper().readValue(resourceSpecJson, ResourceSpec.class);
        } catch (IOException e) {
            log.error("Unable to deserialize resourceSpecJson.", e);
            throw new RuntimeException(e);
        }
    }


    public static String resourceSpecPojo(ResourceSpec resourceSpec) {
        try {
            return XDAT.getSerializerService().getObjectMapper().writeValueAsString(resourceSpec);
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize resourceSpecJson.", e);
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
                .environmentVariables(environmentVariables)
                .bindMounts(bindMountPojo())
                .containerSpec(containerSpecPojo())
                .placementSpec(placementSpecPojo())
                .resourceSpec(resourceSpecPojo())
                .build();
    }

    public void update(final UserOptionsEntity update) {
        this.setUserId(update.getUserId());
        this.setServername(update.getServername());
        this.setXsiType(update.getXsiType());
        this.setItemId(update.getItemId());
        this.setProjectId(update.getProjectId());
        this.setEventTrackingId(update.getEventTrackingId());
        this.setEnvironmentVariables(update.getEnvironmentVariables());
        this.setBindMountsJson(update.getBindMountsJson());
        this.setContainerSpecJson(update.getContainerSpecJson());
        this.setPlacementSpecJson(update.getPlacementSpecJson());
        this.setResourceSpecJson(update.getResourceSpecJson());
    }
}
