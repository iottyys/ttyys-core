package io.ttyys.core.support.springboot;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ttyys.springboot")
public class SpringBootConfigurationProperties {
    private Map<String, String> beanInjectFilterSchemas = new HashMap<>();
//    private String beanInjectNameKey = "BEAN-INJECT-NAME";
//    private String beanInjectTypeKey = "BEAN-INJECT-TYPE";
//    private String beanInjectPreEndpointsHeaderKey = "BEAN-INJECT-PRE-ENDPOINTS";
//    private String beanInjectRegexPattern = "--([a-zA-Z]+):(.*)--";

    public void setInjectFilterSchemes(Map<String, String> beanInjectFilterSchemas) {
        this.beanInjectFilterSchemas = ImmutableMap.<String, String>builder()
                .put("bean", "method")
                .putAll(beanInjectFilterSchemas)
                .build();
    }
}
