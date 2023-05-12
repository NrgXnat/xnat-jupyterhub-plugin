package org.nrg.jobtemplates.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobTemplate {

    private ComputeSpec computeSpec;
    private Hardware hardware;
    private List<Constraint> constraints;

}