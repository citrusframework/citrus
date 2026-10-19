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
package org.citrusframework.grpc.actions;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.spi.ReferenceResolver;

/**
 * Cancels the current gRPC call on a client endpoint.
 */
public class GrpcCancelAction extends AbstractTestAction {

    private final Endpoint endpoint;
    private final String endpointUri;

    public GrpcCancelAction(Builder builder) {
        super("grpc:cancel", builder);
        this.endpoint = builder.endpoint;
        this.endpointUri = builder.endpointUri;
    }

    @Override
    public void doExecute(TestContext context) {
        Endpoint target = endpoint;
        if (target == null) {
            if (context.getReferenceResolver() == null) {
                throw new CitrusRuntimeException("Missing gRPC client endpoint to cancel the call");
            }
            target = context.getReferenceResolver().resolve(endpointUri, Endpoint.class);
        }

        if (!(target instanceof GrpcClient client)) {
            throw new CitrusRuntimeException(String.format(
                    "Endpoint '%s' is not a gRPC client - cannot cancel a call", target.getName()));
        }

        client.cancel(context);
    }

    public static class Builder extends AbstractTestActionBuilder<GrpcCancelAction, Builder>
            implements org.citrusframework.api.actions.grpc.GrpcCancelActionBuilder<GrpcCancelAction, Builder> {

        private Endpoint endpoint;
        private String endpointUri;
        private ReferenceResolver referenceResolver;

        public Builder endpoint(Endpoint endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder endpoint(String endpointUri) {
            this.endpointUri = endpointUri;
            return this;
        }

        @Override
        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public GrpcCancelAction build() {
            return new GrpcCancelAction(this);
        }
    }
}
