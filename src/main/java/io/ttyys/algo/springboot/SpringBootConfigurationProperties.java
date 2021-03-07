package io.ttyys.algo.springboot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ttyys.springboot.data")
public class SpringBootConfigurationProperties {
}
