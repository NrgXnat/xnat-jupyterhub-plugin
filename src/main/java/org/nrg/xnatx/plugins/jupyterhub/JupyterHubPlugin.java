package org.nrg.xnatx.plugins.jupyterhub;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xnatx.plugins.jupyterhub.client.DefaultJupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.client.JupyterHubClient;
import org.nrg.xnatx.plugins.jupyterhub.preferences.JupyterHubPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@XnatPlugin(value = "JupyterHubPlugin",
            name  = "Jupyter Hub Plugin",
            logConfigurationFile = "jupyterhub-logback.xml",
            entityPackages = {"org.nrg.xnatx.plugins.jupyterhub.entities"})
@ComponentScan({"org.nrg.xnatx.plugins.jupyterhub.preferences",
                "org.nrg.xnatx.plugins.jupyterhub.client",
                "org.nrg.xnatx.plugins.jupyterhub.rest",
                "org.nrg.xnatx.plugins.jupyterhub.services",
                "org.nrg.xnatx.plugins.jupyterhub.services.impl",
                "org.nrg.xnatx.plugins.jupyterhub.events",
                "org.nrg.xnatx.plugins.jupyterhub.listeners",
                "org.nrg.xnatx.plugins.jupyterhub.repositories",
                "org.nrg.xnatx.plugins.jupyterhub.utils",
                "org.nrg.xnatx.plugins.jupyterhub.initialize"})
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
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JupyterHubClient getJupyterHubClient() {
        return new DefaultJupyterHubClient(jupyterHubPreferences.getJupyterHubToken(),
                                           jupyterHubPreferences.getJupyterHubUrl());
    }

}
