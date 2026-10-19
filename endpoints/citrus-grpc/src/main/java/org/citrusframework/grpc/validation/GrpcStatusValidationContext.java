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

package org.citrusframework.grpc.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.validation.context.DefaultValidationContext;

/**
 * Validation context holding the expected gRPC status of a call: the status code (default {@code OK}),
 * an optional description and optional trailers. Values may use Citrus validation matchers.
 */
public class GrpcStatusValidationContext extends DefaultValidationContext {

    private String statusCode = "OK";
    private String statusDescription;

    private final Map<String, Object> trailers = new LinkedHashMap<>();

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Map<String, Object> getTrailers() {
        return trailers;
    }

    public void trailer(String name, Object value) {
        trailers.put(name, value);
    }

    @Override
    public Optional<String> getCorrespondingValidationModule() {
        return Optional.of("org.citrusframework:citrus-grpc");
    }

    /**
     * Fluent builder for a status validation context.
     */
    public static class Builder {

        private final GrpcStatusValidationContext context = new GrpcStatusValidationContext();

        public static Builder status() {
            return new Builder();
        }

        public Builder statusCode(String statusCode) {
            context.setStatusCode(statusCode);
            return this;
        }

        public Builder statusDescription(String statusDescription) {
            context.setStatusDescription(statusDescription);
            return this;
        }

        public Builder trailer(String name, Object value) {
            context.trailer(name, value);
            return this;
        }

        public GrpcStatusValidationContext build() {
            return context;
        }
    }
}
