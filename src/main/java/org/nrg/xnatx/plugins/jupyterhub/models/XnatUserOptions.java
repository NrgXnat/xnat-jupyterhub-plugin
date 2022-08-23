package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "Jupyter Notebook Server User Options", description = "Jupyter Notebook Server User Options")
public class XnatUserOptions implements UserOptions {

    private Integer userId;
    private String servername;
    private String xsiType;
    private String itemId;
    private String projectId;
    private String eventTrackingId;

    @JsonProperty("docker-image") private String dockerImage; // The image to use for single-user servers
    @JsonProperty("environment-variables") private Map<String, String> environmentVariables; // Extra environment variables to set for the single-user server’s process.
    @JsonProperty("mounts") private List<BindMount> bindMounts;

}
