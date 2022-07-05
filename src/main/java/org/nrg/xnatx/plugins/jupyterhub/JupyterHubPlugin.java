package org.nrg.xnatx.plugins.jupyterhub;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xnatx.plugins.jupyterhub.client.DefaultJupyterHupClient;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(value = "JupyterHubPlugin",
            name  = "Jupyter Hub Plugin",
            logConfigurationFile = "jupyterhub-logback.xml")
@ComponentScan({"org.nrg.xnatx.plugins.jupyterhub.preferences",
                "org.nrg.xnatx.plugins.jupyterhub.client",
                "org.nrg.xnatx.plugins.jupyterhub.rest",
                "org.nrg.xnatx.plugins.jupyterhub.services",
                "org.nrg.xnatx.plugins.jupyterhub.services.impl"})
@Slf4j
public class JupyterHubPlugin {

    private final JupyterHubPreferences jupyterHubPreferences;

    @Autowired
    public JupyterHubPlugin(final JupyterHubPreferences jupyterHubPreferences) {
        this.jupyterHubPreferences = jupyterHubPreferences;
    }

    @Bean
    public SearchHelperServiceI getSearchHelperService() {
        return UserHelper.getSearchHelperService();
    }

    @Bean
    public JupyterHubClient getJupyterHubClient() {
        return new DefaultJupyterHupClient(jupyterHubPreferences.getJupyterHubToken(),
                                           jupyterHubPreferences.getJupyterHubApiUrl());
    }

}
