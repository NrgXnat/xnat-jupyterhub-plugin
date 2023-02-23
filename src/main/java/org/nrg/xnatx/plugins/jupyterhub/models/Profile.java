package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(ALWAYS)
public class Profile {
    public static final String SWARM_SPAWNER = "dockerspawner.SwarmSpawner";
    public static final String KUBE_SPAWNER = "NOT_IMPLEMENTED";

    private Long id;
    private String name;
    private String description;
    private String spawner;
    private Boolean enabled;
    @JsonProperty("task_template") private TaskTemplate taskTemplate;

}