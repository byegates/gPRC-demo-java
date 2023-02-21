package calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static calculator.service.CalculatorServer.PORT;


public class CalculatorClient {

    public static final String tag = "[Client]";

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
            case "sum" -> doSum(channel);
            case "primes" -> doPrimes(channel);
            case "avg" -> doAvg(channel);
            case "max" -> doMax(channel);
            case "sqrt" -> {
                doSqrt(channel, 25);
                doSqrt(channel, 78);
                doSqrt(channel, -1);
            }
            default -> System.out.printf("%s Keyword(Function Name) Invalid: %s\n", tag, args[0]);
        }

        System.out.printf("%s [Shutting Down]\n", tag);
        channel.shutdown();
        System.out.printf("%s [Down]\n\n", tag);
    }

    private static void doMax(ManagedChannel channel) throws InterruptedException {
        String tag0 = tag + " [Max]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = CalculatorServiceGrpc.newStub(channel);
        var latch = new CountDownLatch(1);
        var stream = stub.max(new StreamObserver<>() {
            @Override
            public void onNext(MaxResponse resp) {
                System.out.printf("%s [resp] Max: %d\n", tag0, resp.getMax());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        List.of(1,5,3,6,2,8,7,9,4,10).forEach(x -> stream.onNext(MaxRequest.newBuilder().setX(x).build()));

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doAvg(ManagedChannel channel) throws InterruptedException {
        String tag0 = tag + " [Avg]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = CalculatorServiceGrpc.newStub(channel);
        var latch = new CountDownLatch(1);
        var stream = stub.avg(new StreamObserver<>() {
            @Override
            public void onNext(AvgResponse resp) {
                System.out.printf("%s [resp] Avg: %f\n", tag0, resp.getAvg());
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        List.of(1,2,3,4,5,6,7,8,9,10).forEach(x -> stream.onNext(AvgRequest.newBuilder().setX(x).build()));

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doPrimes(ManagedChannel channel) {
        String tag0 = tag + " [Primes]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = CalculatorServiceGrpc.newBlockingStub(channel);
        System.out.printf("%s [resp] ", tag0);
        stub.primes(PrimeRequest.newBuilder().setX(567890).build()).forEachRemaining(resp -> {
            System.out.printf("%d, ", resp.getP());
        });
        System.out.print("\n\n");
    }

    private static void doSum(ManagedChannel channel) {
        String tag0 = tag + " [Sum]";
        System.out.printf("%s [Invoked]\n", tag0);

        int a = 3, b = 10;
        var stub = CalculatorServiceGrpc.newBlockingStub(channel);
        var resp = stub.sum(SumRequest.newBuilder().setA(a).setB(b).build());
        System.out.printf("%s [resp] %2d + %2d = %2d\n", tag0, a, b, resp.getSum());
    }

    private static void doSqrt(ManagedChannel channel, int x) {
        String tag0 = tag + " [Sqrt]";
        System.out.printf("%s [Invoked]\n", tag0);

        var stub = CalculatorServiceGrpc.newBlockingStub(channel);
        try {
            var resp = stub.sqrt(SqrtRequest.newBuilder().setX(x).build());
            System.out.printf("%s [resp] Sqrt of %2d = %f\n", tag0, x, resp.getSqrt());
        } catch (RuntimeException e) {
            System.out.printf("%s [Error] Got Exception.\n", tag0);
            e.printStackTrace();
        }
    }
}
