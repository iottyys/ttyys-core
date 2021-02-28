package io.ttyys.core.support.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
@ComponentScan("io.ttyys.core")
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {

    @Bean
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {

            }
        };
    }
}
