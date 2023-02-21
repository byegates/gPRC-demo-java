package greeting.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServerTls {

    public static final String tag = "[Server]";
    public static final int PORT = 50064;
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(PORT)
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
                .addService(new GreetingServerImpl()).build().start();

        System.out.printf("%s [Started] listening on port: %d\n", tag, PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("%s [Recv] Shutdown request\n", tag);
            server.shutdown();
            System.out.printf("%s [Stopped]\n", tag);
        }));

        server.awaitTermination();
    }
}
