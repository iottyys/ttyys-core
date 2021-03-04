package io.ttyys.data.springboot;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
@ComponentScan("io.ttyys.data")
@Import(PythonServerRunner.class)
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {
}
