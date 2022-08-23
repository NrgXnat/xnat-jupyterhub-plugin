package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnatx.plugins.jupyterhub.rest.JupyterHubApi;
import org.nrg.xnatx.plugins.jupyterhub.services.JupyterHubService;
import org.nrg.xnatx.plugins.jupyterhub.services.UserOptionsService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@Import({MockConfig.class, RestApiTestConfig.class})
public class JupyterHubApiConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public JupyterHubApi jupyterHubApi(final UserManagementServiceI mockUserManagementService,
                                       final RoleHolder mockRoleHolder,
                                       final JupyterHubService mockJupyterHubService,
                                       final UserOptionsService mockUserOptionsService) {
        return new JupyterHubApi(mockUserManagementService,
                                 mockRoleHolder,
                                 mockJupyterHubService,
                                 mockUserOptionsService);
    }

    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new TestingAuthenticationProvider());
    }
}
