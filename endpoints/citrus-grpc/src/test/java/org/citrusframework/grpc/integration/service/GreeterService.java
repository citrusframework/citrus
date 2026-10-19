/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.grpc.integration.service;

import java.util.concurrent.atomic.AtomicReference;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.citrusframework.grpc.test.proto.helloworld.GetUserRequest;
import org.citrusframework.grpc.test.proto.helloworld.GreeterGrpc;
import org.citrusframework.grpc.test.proto.helloworld.HelloReply;
import org.citrusframework.grpc.test.proto.helloworld.HelloRequest;
import org.citrusframework.grpc.test.proto.helloworld.UserInfo;

/**
 * Real grpc-java service used as the system under test by the client integration tests.
 */
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    public static final Metadata.Key<String> REQUEST_ID =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> TRAILER =
            Metadata.Key.of("x-trailer", Metadata.ASCII_STRING_MARSHALLER);

    public static final AtomicReference<Metadata> LAST_REQUEST_METADATA = new AtomicReference<>();

    @Override
    public void sayHello(HelloRequest request, io.grpc.stub.StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder()
                .setMessage("Hello " + request.getName())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(GetUserRequest request, io.grpc.stub.StreamObserver<UserInfo> responseObserver) {
        if ("unknown".equals(request.getId())) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("unknown user")
                    .asRuntimeException());
            return;
        }

        if ("slow".equals(request.getId())) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        responseObserver.onNext(UserInfo.newBuilder().setName("Ada").build());
        responseObserver.onCompleted();
    }

    /**
     * Captures the request metadata and adds a response trailer to every call.
     */
    public static class MetadataInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                     Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            LAST_REQUEST_METADATA.set(headers);

            ServerCall<ReqT, RespT> wrapped = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                @Override
                public void close(Status status, Metadata trailers) {
                    trailers.put(TRAILER, "trailer-value");
                    super.close(status, trailers);
                }
            };

            return next.startCall(wrapped, headers);
        }
    }
}
