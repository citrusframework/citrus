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
package org.citrusframework.graphql.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.graphql.message.MapGraphQlResponse;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.json.JsonPathUtils;
import org.citrusframework.json.JsonSettings;
import org.citrusframework.message.Message;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.json.JsonElementValidator;
import org.citrusframework.validation.json.JsonElementValidatorItem;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.springframework.graphql.ResponseError;

/**
 * Validates messages received through GraphQL receive actions against a
 * {@link GraphQlMessageValidationContext}. Selected only for messages that carry the
 * {@link GraphQlMessageHeaders#KIND} header, so other messages keep their usual validators.
 */
public class GraphQlMessageValidator extends AbstractMessageValidator<GraphQlMessageValidationContext> {

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context,
                                GraphQlMessageValidationContext validationContext) {
        GraphQlEndpointSettings settings = validationContext.getSettings(context);
        if (validationContext.getKind() == GraphQlMessageValidationContext.Kind.RESPONSE) {
            validateResponse(receivedMessage, context, validationContext, settings);
        } else {
            GraphQlRequestValidation.validate(receivedMessage, context, validationContext, settings);
        }
    }

    @Override
    protected Class<GraphQlMessageValidationContext> getRequiredValidationContextType() {
        return GraphQlMessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return message != null && message.getHeader(GraphQlMessageHeaders.KIND) != null;
    }

    private static void validateResponse(Message message, TestContext context,
                                         GraphQlMessageValidationContext validationContext, GraphQlEndpointSettings settings) {
        String shapeViolation = GraphQlMessages.shapeViolation(message.getPayload(String.class));
        if (shapeViolation != null) {
            boolean hasExpectations = validationContext.isErrorsExpected()
                    || !validationContext.getDataExpressions().isEmpty()
                    || validationContext.getData() != null;
            boolean unreadable = shapeViolation.equals(GraphQlMessages.NOT_A_JSON_OBJECT);
            if (settings.strict() || (unreadable && hasExpectations)) {
                throw new ValidationException("Expected GraphQL response but got content type '%s' with status %s: %s"
                        .formatted(Objects.requireNonNullElse(GraphQlMessages.header(message, HttpMessageHeaders.HTTP_CONTENT_TYPE), "unknown"),
                                statusCode(message), shapeViolation));
            }

            if (unreadable) {
                return;
            }
        }

        MapGraphQlResponse response = (MapGraphQlResponse) GraphQlMessages.toResponse(message);
        List<ResponseError> errors = response.getErrors();

        if (!validationContext.isErrorsExpected() && !errors.isEmpty()) {
            throw new ValidationException("GraphQL response contains %d unexpected %s: %s - use expectError(...) or expectErrors() if errors are intended"
                    .formatted(errors.size(), errors.size() == 1 ? "error" : "errors", describe(errors)));
        }

        if (validationContext.isErrorsExpected() && errors.isEmpty()) {
            throw new ValidationException("GraphQL errors expected but none were returned");
        }

        for (GraphQlError expected : validationContext.getExpectedErrors()) {
            if (errors.stream().noneMatch(error -> matches(expected, error, context))) {
                throw new ValidationException("No GraphQL error matched %s; received: %s".formatted(expected, describe(errors)));
            }
        }

        validateData(message, response, validationContext, context);
    }

    private static void validateData(Message message, MapGraphQlResponse response,
                                     GraphQlMessageValidationContext validationContext, TestContext context) {
        if (validationContext.getDataExpressions().isEmpty() && validationContext.getData() == null) {
            return;
        }

        Map<String, Object> responseMap = response.toMap();
        if (responseMap.get("data") == null) {
            throw new ValidationException("GraphQL data is %s; errors: %s"
                    .formatted(responseMap.containsKey("data") ? "null" : "absent", describe(response.getErrors())));
        }

        String payload = message.getPayload(String.class);
        validationContext.getDataExpressions().forEach((path, expected) -> {
            String expression = dataPath(path);
            Object actual;
            try {
                actual = JsonPathUtils.evaluate(payload, expression);
            } catch (CitrusRuntimeException e) {
                throw new ValidationException("Failed to evaluate GraphQL data path '%s': %s".formatted(expression, e.getMessage()), e);
            }

            ValidationUtils.validateValues(actual, resolve(expected, context), expression, context);
        });

        if (validationContext.getData() != null) {
            new JsonElementValidator(JsonSettings.isStrict(), context, Set.of())
                    .validate(JsonElementValidatorItem.parseJson(JsonSettings.getPermissiveMoe(),
                            GraphQlMessages.toJson(responseMap.get("data")),
                            context.replaceDynamicContentInString(validationContext.getData())));
        }
    }

    private static boolean matches(GraphQlError expected, ResponseError actual, TestContext context) {
        return fieldMatches("message", expected.getMessage(), actual.getMessage(), context)
                && fieldMatches("extensions.code", expected.getCode(), code(actual), context)
                && (expected.getPath().isEmpty()
                    || fieldMatches("path", GraphQlError.formatPath(expected.getPath()), actual.getPath(), context));
    }

    private static boolean fieldMatches(String field, String expected, String actual, TestContext context) {
        if (expected == null) {
            return true;
        }

        String resolved = context.replaceDynamicContentInString(expected);
        if (ValidationMatcherUtils.isValidationMatcherExpression(resolved)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher(field, actual, resolved, context);
                return true;
            } catch (ValidationException e) {
                return false;
            }
        }

        return Objects.equals(resolved, actual);
    }

    private static String code(ResponseError error) {
        Object code = error.getExtensions().get("code");
        return code == null ? null : code.toString();
    }

    static String describe(List<ResponseError> errors) {
        return errors.stream()
                .map(error -> GraphQlError.error()
                        .message(error.getMessage())
                        .path(error.getParsedPath().toArray())
                        .code(code(error))
                        .toString())
                .collect(Collectors.joining("; "));
    }

    private static String dataPath(String path) {
        if (path.startsWith("$.")) {
            return "$.data." + path.substring(2);
        }

        if (path.startsWith("$[") || "$".equals(path)) {
            return "$.data" + path.substring(1);
        }

        return "$.data." + path;
    }

    private static Object resolve(Object expected, TestContext context) {
        return expected instanceof String text ? context.replaceDynamicContentInString(text) : expected;
    }

    private static String statusCode(Message message) {
        Object status = message.getHeader(HttpMessageHeaders.HTTP_STATUS_CODE);
        return status == null ? "unknown" : status.toString();
    }
}
