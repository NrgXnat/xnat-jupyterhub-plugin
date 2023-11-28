package org.nrg.xnatx.plugins.jupyterhub.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;
import org.nrg.xnat.compute.models.AccessScope;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardScope implements AccessScope {

    private Scope scope;
    private boolean enabled; // Whether the scope is enabled for all ids or only for the ids in the set
    private Set<String> ids; // The set of ids that are enabled for this scope (if isEnabled is false)

}
