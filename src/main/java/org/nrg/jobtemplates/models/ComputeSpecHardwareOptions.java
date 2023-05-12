package org.nrg.jobtemplates.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeSpecHardwareOptions {

    private boolean allowAllHardware;
    private Set<HardwareConfig> hardwareConfigs;

}
