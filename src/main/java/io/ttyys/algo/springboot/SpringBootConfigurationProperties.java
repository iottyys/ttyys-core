package io.ttyys.algo.springboot;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ttyys.springboot.data")
public class SpringBootConfigurationProperties {
    @Getter
    @Setter
    private int delay = 5;
}
