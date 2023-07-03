package org.nrg.xnatx.plugins.jupyterhub.models.docker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

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
public class Resources {

    @JsonProperty("cpu_limit") private Double cpuLimit;
    @JsonProperty("cpu_reservation") private Double cpuReservation;
    @JsonProperty("mem_limit") private String memLimit;
    @JsonProperty("mem_reservation") private String memReservation;
    @JsonProperty("generic_resources") private Map<String, String> genericResources;

}
