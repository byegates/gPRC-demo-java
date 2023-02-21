package calculator.service;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import static greeting.service.GreetingServer.tag;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        String tag0 = tag + " [Greet]";
        System.out.printf("%s [Invoked]\n\n", tag0);
        responseObserver.onNext(SumResponse.newBuilder().setSum(request.getA()+request.getB()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void sqrt(SqrtRequest request, StreamObserver<SqrtResponse> responseObserver) {
        String tag0 = tag + " [Sqrt]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        int x = request.getX();

        if (x < 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                            .withDescription("Negative Numbers are invalid")
                            .augmentDescription("Number Received: " + x)
                    .asRuntimeException());
        }

        responseObserver.onNext(SqrtResponse.newBuilder().setSqrt(Math.sqrt(x)).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<MaxRequest> max(StreamObserver<MaxResponse> responseObserver) {
        String tag0 = tag + " [Avg]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        return new StreamObserver<>() {
            int max = 0;

            @Override
            public void onNext(MaxRequest req) {
                if (req.getX() > max) {
                    max = req.getX();
                    responseObserver.onNext(MaxResponse.newBuilder().setMax(max).build());
                }
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

    @Override
    public StreamObserver<AvgRequest> avg(StreamObserver<AvgResponse> responseObserver) {
        String tag0 = tag + " [Avg]";
        System.out.printf("%s [Invoked]\n\n", tag0);

        return new StreamObserver<>() {
            int sum, cnt;

            @Override
            public void onNext(AvgRequest req) {
                cnt++;
                sum += req.getX();
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(AvgResponse.newBuilder().setAvg((double) sum/cnt).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void primes(PrimeRequest request, StreamObserver<PrimeResponse> responseObserver) {
        String tag0 = tag + " [Primes]";
        System.out.printf("%s [Invoked]\n\n", tag0);
        int divisor = 2, x = request.getX();
        while (x > 1) {
            if (x % divisor == 0) {
                x /= divisor;
                responseObserver.onNext(PrimeResponse.newBuilder().setP(divisor).build());
            } else divisor++;
        }

        responseObserver.onCompleted();
    }
}
