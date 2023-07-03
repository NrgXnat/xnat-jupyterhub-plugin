package org.nrg.xnatx.plugins.jupyterhub.models;

import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ApiModel(value = "Jupyter Notebook Server Start Request", description = "Jupyter Notebook Server Start Request")
public class ServerStartRequest implements UserOptions {

    private String username;
    private String servername;
    private String xsiType;
    private String itemId;
    private String itemLabel;
    private String projectId;
    private String eventTrackingId;

    private Long computeEnvironmentConfigId;
    private Long hardwareConfigId;

}
