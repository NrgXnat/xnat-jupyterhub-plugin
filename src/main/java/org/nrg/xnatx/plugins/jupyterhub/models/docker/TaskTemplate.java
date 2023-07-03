package org.nrg.xnatx.plugins.jupyterhub.models.docker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(ALWAYS)
public class TaskTemplate {

    @JsonProperty("container_spec") private ContainerSpec containerSpec;
    private Resources resources;
    private Placement placement;

}
