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

import io.grpc.stub.StreamObserver;
import org.citrusframework.grpc.test.proto.common.Money;
import org.citrusframework.grpc.test.proto.market.Price;
import org.citrusframework.grpc.test.proto.market.PriceSubscription;
import org.citrusframework.grpc.test.proto.market.PricesGrpc;

/**
 * Real grpc-java server-streaming service used as the system under test.
 */
public class PricesService extends PricesGrpc.PricesImplBase {

    @Override
    public void subscribe(PriceSubscription request, StreamObserver<Price> responseObserver) {
        for (int i = 0; i < 3; i++) {
            responseObserver.onNext(price(request.getSymbol(), i));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void watch(PriceSubscription request, StreamObserver<Price> responseObserver) {
        responseObserver.onNext(price(request.getSymbol(), 0));
        // never completes - the client is expected to cancel the subscription
    }

    private static Price price(String symbol, long amount) {
        return Price.newBuilder()
                .setSymbol(symbol)
                .setAmount(Money.newBuilder().setCurrency("EUR").setAmount(amount))
                .build();
    }
}
