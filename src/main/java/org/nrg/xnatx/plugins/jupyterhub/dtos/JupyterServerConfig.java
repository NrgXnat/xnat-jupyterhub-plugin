package org.nrg.xnatx.plugins.jupyterhub.dtos;

import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "Jupyter Server Configuration", description = "Jupyter Server Configuration")
public class JupyterServerConfig {

    private String userWorkspacePath;
    // id -> file path of projects/experiments
    private Map<String, String> xnatResourcePaths;

}
