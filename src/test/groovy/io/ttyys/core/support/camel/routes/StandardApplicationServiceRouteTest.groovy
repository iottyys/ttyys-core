package io.ttyys.core.support.camel.routes

import org.apache.camel.EndpointInject
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.mock.MockEndpoint
import org.apache.camel.test.spring.junit5.CamelSpringBootTest
import org.apache.camel.test.spring.junit5.MockEndpoints
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication

@CamelSpringBootTest
@SpringBootApplication(scanBasePackages = "io.ttyys.core")
@MockEndpoints()
class StandardApplicationServiceRouteTest {

    @Autowired
    ProducerTemplate template
    @EndpointInject("mock:log:direct:io.ttyys.core.support.StandardApplicationService")
    MockEndpoint mock

    @Test
    void test() {
        mock.expectedBodiesReceived("Hello");
        template.sendBody("direct:io.ttyys.core.support.StandardApplicationService", "Hello");
        mock.assertIsSatisfied();

    }
}
