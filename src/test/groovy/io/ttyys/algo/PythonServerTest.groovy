package io.ttyys.algo

import io.ttyys.algo.springboot.EnableDataSupport
import io.ttyys.algo.springboot.SpringBootAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = SpringBootAutoConfiguration)
@EnableDataSupport
class PythonServerTest {

    @Test
    void baseTest() {}
}
