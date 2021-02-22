package io.ttyys.core.support.camel.routes

import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpoints
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.ResourceLoader

@CamelSpringBootTest
@SpringBootApplication(scanBasePackages = "io.ttyys.core")
@MockEndpoints()
class StandardApplicationServiceRouteTest {

    @Autowired
    ResourceLoader loader

    @Autowired
    ProducerTemplate template
    @EndpointInject("mock:log:direct:io.ttyys.core.support.StandardApplicationService")
    MockEndpoint mock

    @EndpointInject("direct:io.ttyys.core.support.StandardApplicationService")
    private ProducerTemplate producer;

    @Test
    void test() {
        mock.expectedBodiesReceived("Hello");
        template.sendBody("direct:io.ttyys.core.support.StandardApplicationService", "Hello");
        mock.assertIsSatisfied();
    }

    @Test
    void testJava() {
        producer.requestBodyAndHeaders(new Object(),
            [
                serviceUris: 'direct:start1,direct:start2',
                isJsonInput: false, isJavaInput: true, inputSchema: 'java.lang.Object',
                isJsonOutput: false, isJavaOutput: true, outputSchema: 'java.lang.Object'
            ])
    }

    @Test
    void testJson() {
        producer.requestBodyAndHeaders("{}",
            [
                serviceUris: 'direct:start1,direct:start2',
                isJsonInput: true, isJavaInput: false, inputSchema: 'classpath:io/ttyys/core/test.input.schema.json',
                isJsonOutput: true, isJavaOutput: false, outputSchema: 'classpath:io/ttyys/core/test.output.schema.json'
            ])
    }
}
