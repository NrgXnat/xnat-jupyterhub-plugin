package org.nrg.xnat.compute.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ComputeEnvironmentScope {

    private Scope scope;
    private boolean enabled; // For project and user scopes, this is whether the scope is enabled for all projects or users
    private Set<String> ids; // For project and user scopes, this is the set of project or user IDs that are enabled for this scope (if isEnabled is false)

}
