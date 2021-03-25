package io.ttyys.algo

import algo.text.Message
import io.ttyys.algo.springboot.EnableAlgoSupport
import io.ttyys.algo.springboot.SpringBootAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = SpringBootAutoConfiguration)
@EnableAlgoSupport
class PythonServerTest {

    @Test
    void baseTest() {
        String resp = AlgorithmFactory.SIMILARITY.invoker().send(Message.newBuilder().setBody('a').setFrom('b').setTo('c').build())
        println resp
    }
}
