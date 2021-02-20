package io.ttyys.core.support.springboot;

import io.ttyys.core.support.camel.ThreadLocalRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.camel.support.DefaultRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("io.ttyys.core")
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {

    @Bean
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                if (camelContext instanceof SpringBootCamelContext) {
                    SpringBootCamelContext context = (SpringBootCamelContext) camelContext;
                    if (context.getRegistry() instanceof DefaultRegistry) {
                        ((DefaultRegistry) context.getRegistry()).setFallbackRegistry(new ThreadLocalRegistry());
                    }
                }
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {

            }
        };
    }
}
