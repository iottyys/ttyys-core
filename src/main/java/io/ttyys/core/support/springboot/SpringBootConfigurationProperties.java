package io.ttyys.core.support.springboot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ttyys.springboot")
public class SpringBootConfigurationProperties {
//    private Map<String, String> beanInjectFilterSchemas = new HashMap<>();
//    private String beanInjectNameKey = "BEAN-INJECT-NAME";
//    private String beanInjectTypeKey = "BEAN-INJECT-TYPE";
//    private String beanInjectPreEndpointsHeaderKey = "BEAN-INJECT-PRE-ENDPOINTS";
//    private String beanInjectRegexPattern = "--([a-zA-Z]+):(.*)--";

//    public void setInjectFilterSchemes(Map<String, String> beanInjectFilterSchemas) {
//        this.beanInjectFilterSchemas = ImmutableMap.<String, String>builder()
//                .put("bean", "method")
//                .putAll(beanInjectFilterSchemas)
//                .build();
//    }
}
