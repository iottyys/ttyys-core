package io.ttyys.test

import groovy.transform.builder.Builder
import io.ttyys.core.support.architecture.EnhanceService
import io.ttyys.core.support.integration.Execution
import io.ttyys.core.support.integration.InterfaceType
import io.ttyys.core.support.integration.Invoke
import org.springframework.stereotype.Component

import javax.validation.constraints.NotNull

@EnhanceService
interface DemoServiceExecuted {
    @Execution(serviceEndpoints = "direct:domain,direct:integration",
        input = InterfaceType.JavaObj,
        output = InterfaceType.JavaObj)
    def exec(input)
}

@Component
class DemoInvocation {
    @Invoke(serviceInterface = "io.ttyys.test.DemoServiceExecuted", serviceMethod = "exec", convertFile = "convertor.user.account.xml")
    def exec(input) {
    }
}

@Builder
class TestInput {
    @NotNull
    String id
    String name
}

@Builder
class TestOutput {
    int code
    String message
}
