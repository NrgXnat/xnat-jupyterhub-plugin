package org.nrg.xnatx.plugins.jupyterhub.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.ComputeEnvironmentConfig;
import org.nrg.xnat.compute.models.HardwareConfig;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardConfig {

    @ApiModelProperty(position = 0) private Long id;
    @ApiModelProperty(position = 1) private Dashboard dashboard;
    @ApiModelProperty(position = 2) private Map<Scope, DashboardScope> scopes;
    @ApiModelProperty(position = 3) private ComputeEnvironmentConfig computeEnvironmentConfig;
    @ApiModelProperty(position = 4) private HardwareConfig hardwareConfig;

}
