package io.ttyys.core.support.springboot;

import io.ttyys.core.support.camel.ThreadLocalRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanComponent;
import org.apache.camel.component.bean.springboot.BeanComponentConfiguration;
import org.apache.camel.spi.ComponentCustomizer;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.spring.boot.SpringBootCamelContext;
import org.apache.camel.spring.boot.util.CamelPropertiesHelper;
import org.apache.camel.spring.boot.util.ConditionalOnCamelContextAndAutoConfigurationBeans;
import org.apache.camel.spring.boot.util.HierarchicalPropertiesEvaluator;
import org.apache.camel.support.DefaultRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.util.Map;

@Configuration
@ComponentScan("io.ttyys.core")
@EnableConfigurationProperties({SpringBootConfigurationProperties.class})
public class SpringBootAutoConfiguration {

    @Bean
    public CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
//                if (camelContext instanceof SpringBootCamelContext) {
//                    SpringBootCamelContext context = (SpringBootCamelContext) camelContext;
//                    if (context.getRegistry() instanceof DefaultRegistry) {
//                        ((DefaultRegistry) context.getRegistry()).setFallbackRegistry(new ThreadLocalRegistry());
//                    }
//                }
//                camelContext.addComponent("domain", new BeanComponent() {
//                    @Override
//                    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
//                        return super.createEndpoint(uri, remaining, parameters);
//                    }
//                });
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {

            }
        };
    }

//    @Lazy
//    @Bean
//    public ComponentCustomizer configureDomainComponent() {
//        return new ComponentCustomizer() {
//            @Override
//            public void configure(String name, Component target) {
//                CamelPropertiesHelper.copyProperties(applicationContext.getBean(CamelContext.class), applicationContext.getBean(BeanComponentConfiguration.class), target);
//            }
//            @Override
//            public boolean isEnabled(String name, Component target) {
//                return HierarchicalPropertiesEvaluator.evaluate(
//                        applicationContext,
//                        "camel.component.customizer",
//                        "camel.component.bean.customizer")
//                        && target instanceof BeanComponent;
//            }
//        };
//    }
}
