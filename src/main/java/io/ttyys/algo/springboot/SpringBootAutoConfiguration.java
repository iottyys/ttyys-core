package io.ttyys.algo.springboot;

import org.apache.avro.Protocol;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@SuppressWarnings("SpringFacetCodeInspection")
@Configuration
@ComponentScan("io.ttyys.algo")
@Import(PythonServerRunner.class)
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {
}
