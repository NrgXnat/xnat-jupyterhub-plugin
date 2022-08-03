package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value       = "Jupyter Notebook Server Container Configuration",
          description = "The single user Jupyter notebook server configuration.")
public class Container {

    @JsonProperty("docker-image") private String image; // The image to use for single-user servers
    @JsonProperty("environment-variables") private Map<String, String> environment; // Extra environment variables to set for the single-user server’s process.
    @JsonProperty("mounts") private List<ContainerMount> mounts; // id -> file path of projects/experiments

}
