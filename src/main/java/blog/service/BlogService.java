package blog.service;

import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogService {
    public static final String tag = "[Server]";
    public static final int PORT = 50063;
    public static void main(String[] args) throws IOException, InterruptedException {
        var client = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder.forPort(PORT).addService(new BlogServiceImpl(client)).build().start();

        System.out.printf("%s [Started] listening on port: %d\n", tag, PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("%s [Recv] Shutdown request\n", tag);
            server.shutdown();
            client.close();
            System.out.printf("%s [Stopped]\n", tag);
        }));

        server.awaitTermination();
    }

}
