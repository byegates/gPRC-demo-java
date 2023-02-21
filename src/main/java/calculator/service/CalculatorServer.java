package calculator.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class CalculatorServer {

    public static final String tag = "[Server]";
    public static final int PORT = 50062;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(PORT)
                .addService(new CalculatorServiceImpl())
                .addService(ProtoReflectionService.newInstance())
                .build().start();

        System.out.printf("%s [Started] listening on port: %d\n", tag, PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("%s [Recv] Shutdown request\n", tag);
            server.shutdown();
            System.out.printf("%s [Stopped]\n", tag);
        }));

        server.awaitTermination();
    }
}
