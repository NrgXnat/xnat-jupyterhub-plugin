package org.nrg.jobtemplates.models;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeSpec {

    @ApiModelProperty(position = 0) private String name;
    @ApiModelProperty(position = 1) private String image;
    @ApiModelProperty(position = 2) private String command;
    @ApiModelProperty(position = 3) private List<EnvironmentVariable> environmentVariables;
    @ApiModelProperty(position = 4) private List<Mount> mounts;

}
