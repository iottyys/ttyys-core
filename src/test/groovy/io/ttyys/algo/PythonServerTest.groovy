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
    void corpusTest() {
        String resp = AlgorithmFactory.ALGORITHM.invoker().send(Message.newBuilder().setFolder('/Volumes/works/tmp/text').setStop('/Volumes/works/tmp/text/.similarity/stop_word.txt').setDict('/Volumes/works/tmp/text/.similarity/user_dict.txt').setResult('/Volumes/works/tmp/text/.similarity/log.txt').build())
        println resp
    }
}
