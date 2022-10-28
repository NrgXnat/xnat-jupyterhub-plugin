package org.nrg.xnatx.plugins.jupyterhub.models;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class PlacementSpec {

    private List<String> constraints;

}
