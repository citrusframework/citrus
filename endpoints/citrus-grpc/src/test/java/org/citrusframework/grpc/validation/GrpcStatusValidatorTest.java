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

import java.util.List;

import org.citrusframework.base.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrpcStatusValidatorTest {

    private static final String METHOD = "helloworld.Greeter/GetUser";

    private final GrpcStatusValidator validator = new GrpcStatusValidator();
    private TestContext context;

    @BeforeMethod
    public void setUp() {
        context = new TestContext();
        context.setReferenceResolver(new SimpleReferenceResolver());
        context.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());
    }

    @Test
    public void shouldPassWhenStatusCodeMatches() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status().build();

        validator.validateMessage(message(GrpcMessageHeaders.KIND_RESPONSE, "OK", null),
                null, context, contexts(validationContext));

        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldFailWhenStatusCodeDiffers() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status().build();

        assertThatThrownBy(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user"),
                null, context, contexts(validationContext)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("gRPC call 'helloworld.Greeter/GetUser' ended with status NOT_FOUND "
                        + "(\"unknown user\") but OK was expected");
        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.FAILED);
    }

    @Test
    public void shouldMatchDescriptionWithValidationMatcher() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("NOT_FOUND")
                .statusDescription("@contains('unknown')@")
                .build();

        assertThatCode(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user"),
                null, context, contexts(validationContext)))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldFailWhenDescriptionDiffers() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("NOT_FOUND")
                .statusDescription("known user")
                .build();

        assertThatThrownBy(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user"),
                null, context, contexts(validationContext)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("status description");
    }

    @Test
    public void shouldMatchTrailers() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("NOT_FOUND")
                .trailer("x-request-id", "42")
                .build();

        assertThatCode(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user")
                        .trailer("x-request-id", "42"),
                null, context, contexts(validationContext)))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldMatchTrailersWithValidationMatcher() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("NOT_FOUND")
                .trailer("x-request-id", "@startsWith('4')@")
                .build();

        assertThatCode(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user")
                        .trailer("x-request-id", "42"),
                null, context, contexts(validationContext)))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldFailWhenTrailerDiffers() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("NOT_FOUND")
                .trailer("x-request-id", "43")
                .build();

        assertThatThrownBy(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user")
                        .trailer("x-request-id", "42"),
                null, context, contexts(validationContext)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("x-request-id");
    }

    @Test
    public void shouldValidateStreamCompletionStatus() {
        GrpcStatusValidationContext validationContext = GrpcStatusValidationContext.Builder.status()
                .statusCode("UNAVAILABLE")
                .build();

        validator.validateMessage(message(GrpcMessageHeaders.KIND_STREAM_END, "UNAVAILABLE", null),
                null, context, contexts(validationContext));

        assertThat(validationContext.getStatus()).isEqualTo(ValidationStatus.PASSED);
    }

    @Test
    public void shouldOnlySupportStatusMessages() {
        assertThat(validator.supportsMessageType("JSON",
                message(GrpcMessageHeaders.KIND_RESPONSE, "OK", null))).isTrue();
        assertThat(validator.supportsMessageType("JSON",
                message(GrpcMessageHeaders.KIND_STREAM_END, "OK", null))).isTrue();
        assertThat(validator.supportsMessageType("JSON",
                message(GrpcMessageHeaders.KIND_CALL, "OK", null))).isFalse();
        assertThat(validator.supportsMessageType("JSON",
                message(GrpcMessageHeaders.KIND_STREAM_MESSAGE, "OK", null))).isFalse();
        assertThat(validator.supportsMessageType("JSON", null)).isFalse();
    }

    @Test
    public void shouldIgnoreMessagesWithoutStatusContext() {
        assertThatCode(() -> validator.validateMessage(
                message(GrpcMessageHeaders.KIND_RESPONSE, "NOT_FOUND", "unknown user"),
                null, context, List.of()))
                .doesNotThrowAnyException();
    }

    private static List<ValidationContext> contexts(GrpcStatusValidationContext validationContext) {
        return List.of(validationContext);
    }

    private static GrpcMessage message(String kind, String statusCode, String statusDescription) {
        GrpcMessage message = new GrpcMessage("{}")
                .method(METHOD)
                .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                .kind(kind)
                .statusCode(statusCode);

        if (statusDescription != null) {
            message.statusDescription(statusDescription);
        }

        return message;
    }
}
