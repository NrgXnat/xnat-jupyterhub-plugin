package org.nrg.xnatx.plugins.jupyterhub.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class DockerImage {

    private String image;
    private boolean enabled;

}
