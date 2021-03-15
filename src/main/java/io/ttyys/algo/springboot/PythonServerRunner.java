package io.ttyys.algo.springboot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.File;

public class PythonServerRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("test");
        File f = new File(Thread.currentThread().getContextClassLoader().getResource("bin/server").toURI());
        System.out.println(f.exists());
        System.out.println(f.getAbsolutePath());
//        new PythonSocketServer().start();
    }
}
