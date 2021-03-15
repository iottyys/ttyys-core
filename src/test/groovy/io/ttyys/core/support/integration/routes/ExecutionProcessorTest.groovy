package io.ttyys.core.support.integration.routes

import io.ttyys.core.support.springboot.EnableEnhanceSupport
import io.ttyys.core.support.springboot.SpringBootAutoConfiguration
import io.ttyys.test.DemoServiceExecuted
import io.ttyys.test.TestInput
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan

@SpringBootTest(classes = SpringBootAutoConfiguration.class)
@ComponentScan("io.ttyys.test")
@EnableEnhanceSupport(servicePackages = "io.ttyys.test")
class ExecutionProcessorTest {
    @Autowired
    DemoServiceExecuted executed

    @Test
    void test() {
        TestInput input = TestInput.builder().id("testId").name("testName").build();
        println executed.exec(input)
    }
}
