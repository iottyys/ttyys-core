package io.ttyys.core.support.integration.routes

import io.ttyys.core.support.springboot.EnableEnhanceSupport
import io.ttyys.test.DemoInvocation
import io.ttyys.test.TestInput
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan

@SpringBootTest()
@ComponentScan("io.ttyys.test")
@EnableEnhanceSupport
class InvocationProcessorTest {
    @Autowired
    DemoInvocation invocation

    @Test
    void test() {
        TestInput input = TestInput.builder().id("testId").name("testName").build();
        println invocation.exec(input)
    }
}
