package greeting.service;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

import static greeting.service.GreetingServer.tag;

public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    @Override
    public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        String tag0 = tag + " [Greet]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        responseObserver.onNext(GreetingResponse.newBuilder().setResult(
                createGreet(request, false)
        ).build());
        responseObserver.onCompleted();
    }

    @Override
    public void greetWithDL(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        String tag0 = tag + " [DL]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var ctx = Context.current();

        try {
            for (int i = 0; i < 3; i++) {
                if (ctx.isCancelled()) {
                    return;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }

        responseObserver.onNext(GreetingResponse.newBuilder().setResult(
                createGreet(request, false)
        ).build());
        responseObserver.onCompleted();
    }

    @Override
    public void greetSStream(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
        String tag0 = tag + " [SS]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var resp = GreetingResponse.newBuilder()
                .setResult(createGreet(request, false))
                .build();

        for (int i = 0; i < 5; i++)
            responseObserver.onNext(resp);

        responseObserver.onCompleted();
    }

    private static String createGreet(GreetingRequest request, boolean nl) {
        return String.format("Hello, %s! \uD83C\uDCCF%s", request.getFirstName(), nl ? "\n" : "");
    }

    @Override
    public StreamObserver<GreetingRequest> greetCStream(StreamObserver<GreetingResponse> responseObserver) {
        String tag0 = tag + " [CS]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        var sb = new StringBuilder();

        return new StreamObserver<>() {

            @Override
            public void onNext(GreetingRequest req) {
                sb.append(createGreet(req, true));
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult(sb.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetingRequest> greetBStream(StreamObserver<GreetingResponse> responseObserver) {
        String tag0 = tag + " [BS]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        return new StreamObserver<>() {

            @Override
            public void onNext(GreetingRequest req) {
                responseObserver.onNext(GreetingResponse.newBuilder().setResult(createGreet(req, false)).build());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
