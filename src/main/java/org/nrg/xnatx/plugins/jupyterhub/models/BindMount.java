package org.nrg.xnatx.plugins.jupyterhub.models;

import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value       = "Bind Mount",
          description = "Describes the bind mounts used by the single-user Jupyter notebook server container.")
public class BindMount {

    private String name;
    private boolean writable;
    private String containerHostPath;
    private String xnatHostPath;
    private String jupyterHostPath;

}
