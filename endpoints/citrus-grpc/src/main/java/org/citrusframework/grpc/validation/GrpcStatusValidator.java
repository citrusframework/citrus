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

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;

/**
 * Validates the final gRPC status of a received response or stream end against a
 * {@link GrpcStatusValidationContext}. Only messages that carry a gRPC kind of
 * {@code response} or {@code stream_end} are supported.
 */
public class GrpcStatusValidator extends AbstractMessageValidator<GrpcStatusValidationContext> {

    @Override
    protected Class<GrpcStatusValidationContext> getRequiredValidationContextType() {
        return GrpcStatusValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        if (message == null) {
            return false;
        }

        Object kind = message.getHeader(GrpcMessageHeaders.KIND);
        return GrpcMessageHeaders.KIND_RESPONSE.equals(kind) || GrpcMessageHeaders.KIND_STREAM_END.equals(kind);
    }

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context,
                                GrpcStatusValidationContext validationContext) {
        String method = header(receivedMessage, GrpcMessageHeaders.METHOD);
        String actualCode = header(receivedMessage, GrpcMessageHeaders.STATUS_CODE);
        String actualDescription = header(receivedMessage, GrpcMessageHeaders.STATUS_DESCRIPTION);
        String expectedCode = validationContext.getStatusCode();

        if (!expectedCode.equals(actualCode)) {
            throw new ValidationException(failureMessage(method, actualCode, actualDescription, expectedCode));
        }

        if (validationContext.getStatusDescription() != null) {
            ValidationUtils.validateValues(actualDescription, validationContext.getStatusDescription(),
                    "status description", context);
        }

        validationContext.getTrailers().forEach((name, expected) -> {
            Object actual = receivedMessage.getHeader(GrpcMessageHeaders.TRAILER_PREFIX + name);
            ValidationUtils.validateValues(actual, expected, "trailer '" + name + "'", context);
        });
    }

    private static String header(Message message, String name) {
        Object value = message.getHeader(name);
        return value == null ? null : value.toString();
    }

    private static String failureMessage(String method, String actualCode, String actualDescription, String expectedCode) {
        String description = actualDescription == null || actualDescription.isBlank()
                ? ""
                : " (\"%s\")".formatted(actualDescription);

        return "gRPC call '%s' ended with status %s%s but %s was expected"
                .formatted(method, actualCode, description, expectedCode);
    }
}
