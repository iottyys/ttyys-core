package io.ttyys.algo.springboot;

import io.ttyys.core.rpc.PythonSocketServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class PythonServerRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new PythonSocketServer().start();
    }
}
