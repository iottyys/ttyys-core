package io.ttyys.algo.springboot;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("io.ttyys.algo")
@Import(PythonServerRunner.class)
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {
}
