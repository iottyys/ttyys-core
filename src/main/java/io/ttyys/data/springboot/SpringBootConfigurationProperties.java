package io.ttyys.data.springboot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ttyys.springboot.data")
public class SpringBootConfigurationProperties {
}
