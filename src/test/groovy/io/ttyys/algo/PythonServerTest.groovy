package io.ttyys.algo

import algo.text.Message
import io.ttyys.algo.springboot.EnableAlgoSupport
import io.ttyys.algo.springboot.SpringBootAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// @SpringBootTest(classes = SpringBootAutoConfiguration)
@EnableAlgoSupport
class PythonServerTest {

    @Test
    void corpusTest() {
        String resp = AlgorithmFactory.ALGORITHM.invoker().send(
                Message.newBuilder()
                        .setFolderPath('/Volumes/works/tmp/text')
                        .setStopWordFile('/Volumes/works/tmp/text/.similarity/stop_word')
                        .setUserDict('/Volumes/works/tmp/text/.similarity/user_dict')
                        .setCosResultFile('/Volumes/works/tmp/text/.similarity/cos_result')
                        .setSimResultFile('/Volumes/works/tmp/text/.similarity/sim_result')
                        .build())
        println resp
    }
}
