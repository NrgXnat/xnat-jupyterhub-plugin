package org.nrg.xnatx.plugins.jupyterhub.config;

import org.nrg.xnatx.plugins.jupyterhub.utils.impl.DefaultSystemHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultSystemHelperTestConfig {

    @Bean
    public DefaultSystemHelper defaultSystemHelper() {
        return new DefaultSystemHelper();
    }

}
