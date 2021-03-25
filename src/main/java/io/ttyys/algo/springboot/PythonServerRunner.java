package io.ttyys.algo.springboot;

import io.ttyys.core.rpc.PythonSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public class PythonServerRunner implements ApplicationRunner {
    @Value("${spring.application.name:springboot_default_server}")
    private String serverName;

    @Autowired
    private SpringBootConfigurationProperties configuration;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new PythonSocketServer(serverName, configuration.getDelay()).start();
    }
}
