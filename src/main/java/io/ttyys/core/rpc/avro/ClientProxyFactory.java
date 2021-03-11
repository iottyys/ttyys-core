package io.ttyys.core.rpc.avro;

import org.apache.avro.Protocol;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ClientProxyFactory {
    public static ClientProxy getClient() throws IOException {
//        SaslSocketTransceiver client = new SaslSocketTransceiver(new InetSocketAddress(22222));
        File avpr = ResourceUtils.getFile("classpath:avro/test.avpr");
        Path tmp = Files.createTempDirectory("test-avro");
        Files.deleteIfExists(tmp);
        Protocol protocol = Protocol.parse(avpr);
        SpecificCompiler compiler = new SpecificCompiler(protocol);
        compiler.compileToDestination(avpr, tmp.toFile());
        Files.list(tmp).forEach(System.out::println);

//        System.out.println(javaStrings);
        return null;
    }

    public class ClientProxy {

    }

    public static void main(String[] args) throws IOException {
        ClientProxyFactory.getClient();
    }
}
