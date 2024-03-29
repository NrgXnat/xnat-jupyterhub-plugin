package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.jupyterhub.client.models.UserOptions;
import org.nrg.xnatx.plugins.jupyterhub.models.docker.TaskTemplate;

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
    private String itemLabel;
    private String projectId;
    private String eventTrackingId;
    private Long dashboardConfigId;

    @JsonProperty("task_template") private TaskTemplate taskTemplate;

}
