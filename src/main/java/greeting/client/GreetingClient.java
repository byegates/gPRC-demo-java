package greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static greeting.service.GreetingServer.PORT;

public class GreetingClient {

    public static final String tag = "[Client]";
    private static final List<String> names = List.of("邱晨", "颜怡颜悦", "鸟鸟", "肖骁", "艾力");

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.printf("%s Please provide the function you'd like to call as an argument.", tag);
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", PORT)
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "greet" -> doGreet(channel, 0);
            case "DL" -> {
                doGreet(channel, 5000);
                doGreet(channel, 300);
            }
            case "SS" -> doSStream(channel);
            case "CS" -> doCStream(channel);
            case "BS" -> doBStream(channel);
            default -> System.out.printf("%s Keyword(Function Name) Invalid: %s\n", tag, args[0]);
        }

        System.out.printf("%s [Shutting Down]\n", tag);
        channel.shutdown();
        System.out.printf("%s [Down]\n", tag);
    }

    private static void doBStream(ManagedChannel channel) throws InterruptedException {
        String tag0 = tag + " [BS]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = GreetingServiceGrpc.newStub(channel);
        var latch = new CountDownLatch(1);

        var stream = stub.greetBStream(new StreamObserver<>() {
            @Override
            public void onNext(GreetingResponse resp) {
                System.out.printf("%s [RESP] %s\n", tag0, resp.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        names.forEach(name -> stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build()));

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doCStream(ManagedChannel channel) throws InterruptedException {
        String tag0 = tag + " [CS]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = GreetingServiceGrpc.newStub(channel);
        var latch = new CountDownLatch(1);

        var stream = stub.greetCStream(new StreamObserver<>() {
            @Override
            public void onNext(GreetingResponse resp) {
                System.out.printf("%s [RESP]\n%s", tag0, resp.getResult());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        names.forEach(name -> stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build()));

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doSStream(ManagedChannel channel) {
        String tag0 = tag + " [SS]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = GreetingServiceGrpc.newBlockingStub(channel);
        stub.greetSStream(GreetingRequest.newBuilder().setFirstName("邱晨").build()).forEachRemaining(resp -> {
            System.out.printf("%s [Recv] %s\n", tag0, resp.getResult());
        });
    }

    private static void doGreet(ManagedChannel channel, int dl) {
        String tag0 = tag + " [Greet]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = GreetingServiceGrpc.newBlockingStub(channel);
        var req = GreetingRequest.newBuilder().setFirstName("邱晨").build();

        try {
            var response = dl ==  0 ? stub.greet(req) :
                    stub.withDeadline(Deadline.after(dl, TimeUnit.MILLISECONDS)).greetWithDL(req);

            System.out.printf("%s [Recv] %s\n", tag0, response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.printf("%s [Error] Deadline Exceeded\n", tag0);
            }
            System.out.printf("%s [Error] Got an exception\n", tag0);
            e.printStackTrace();
        }
    }
}
