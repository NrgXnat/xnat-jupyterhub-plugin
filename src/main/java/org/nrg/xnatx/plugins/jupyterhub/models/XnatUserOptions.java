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
@ApiModel(value = "Jupyter Notebook Server User Options", description = "Jupyter Notebook Server User Options")
public class XnatUserOptions implements UserOptions {

    private String servername;
    private String xsiType;
    private String id;

}
