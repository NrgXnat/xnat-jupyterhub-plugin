package org.nrg.xnatx.plugins.jupyterhub.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class ResourceSpec {

    @JsonProperty("cpu_limit") private Double cpuLimit;
    @JsonProperty("cpu_reservation") private Double cpuReservation;
    @JsonProperty("mem_limit") private String memLimit;
    @JsonProperty("mem_reservation") private String memReservation;

}
