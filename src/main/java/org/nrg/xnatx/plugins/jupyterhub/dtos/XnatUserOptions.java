package org.nrg.xnatx.plugins.jupyterhub.dtos;

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
@ApiModel(value = "Jupyter Server User Options", description = "Jupyter Server User Options")
public class XnatUserOptions implements UserOptions {

//    private String username;
    private String servername;
    private String xsiType;
    private String id;

}
