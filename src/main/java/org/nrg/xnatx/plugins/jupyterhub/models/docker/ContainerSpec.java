package org.nrg.xnatx.plugins.jupyterhub.models.docker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(ALWAYS)
public class ContainerSpec {

    private String image;
    private Map<String, String> env;
    private Map<String, String> labels;
    private List<Mount> mounts;

}
