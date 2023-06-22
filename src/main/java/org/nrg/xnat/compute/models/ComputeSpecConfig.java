package org.nrg.xnat.compute.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;

import java.util.Map;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeSpecConfig {

    @ApiModelProperty(position = 0) private Long id;
    @ApiModelProperty(position = 1) private Set<ConfigType> configTypes;
    @ApiModelProperty(position = 2) private ComputeSpec computeSpec;
    @ApiModelProperty(position = 3) private Map<Scope, ComputeSpecScope> scopes;
    @ApiModelProperty(position = 4) private ComputeSpecHardwareOptions hardwareOptions;

    public enum ConfigType {
        JUPYTERHUB,
        CONTAINER_SERVICE,
        GENERAL
    }
}
