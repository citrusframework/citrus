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

import java.util.Map;
import java.util.Set;

import graphql.language.Document;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.document.GraphQlDocuments;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.json.JsonSettings;
import org.citrusframework.message.Message;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.json.JsonElementValidator;
import org.citrusframework.validation.json.JsonElementValidatorItem;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.springframework.graphql.GraphQlRequest;

/**
 * Validation of incoming GraphQL requests on the simulator side: document checks when strict
 * (parse, operation selection, schema), then the expected operation name, query and variables.
 */
final class GraphQlRequestValidation {

    private GraphQlRequestValidation() {
        // utility class
    }

    static void validate(Message message, TestContext context, GraphQlMessageValidationContext validationContext,
                         GraphQlEndpointSettings settings) {
        GraphQlRequest request;
        try {
            request = GraphQlMessages.toRequest(message);
        } catch (ValidationException e) {
            if (settings.strict() || hasExpectations(validationContext)) {
                throw e;
            }

            return;
        }

        if (settings.strict()) {
            Document document = GraphQlDocuments.parse(request.getDocument());
            GraphQlDocuments.selectOperation(document, request.getOperationName());
            if (settings.schemaLoader() != null) {
                GraphQlDocuments.validate(settings.schemaLoader().getSchema(), document);
            }
        }

        if (validationContext.getOperationName() != null) {
            ValidationUtils.validateValues(request.getOperationName(),
                    context.replaceDynamicContentInString(validationContext.getOperationName()), "operationName", context);
        }

        if (validationContext.getQuery() != null) {
            validateQuery(request.getDocument(), context.replaceDynamicContentInString(validationContext.getQuery()),
                    settings.strict(), context);
        }

        validateVariables(request.getVariables(), validationContext, context);
    }

    private static void validateQuery(String actual, String expected, boolean strict, TestContext context) {
        if (strict && !ValidationMatcherUtils.isValidationMatcherExpression(expected)) {
            String expectedNormalized = GraphQlDocuments.normalize(expected);
            String actualNormalized = GraphQlDocuments.normalize(actual);
            if (!expectedNormalized.equals(actualNormalized)) {
                throw new ValidationException("GraphQL query does not match - expected: %s but was: %s"
                        .formatted(expectedNormalized, actualNormalized));
            }

            return;
        }

        ValidationUtils.validateValues(actual, expected, "query", context);
    }

    private static void validateVariables(Map<String, Object> actual, GraphQlMessageValidationContext validationContext,
                                          TestContext context) {
        validationContext.getVariables().forEach((name, expected) -> {
            if (!actual.containsKey(name)) {
                throw new ValidationException("GraphQL variable '%s' is missing; received variables: %s".formatted(name, actual));
            }

            ValidationUtils.validateValues(actual.get(name),
                    expected instanceof String text ? context.replaceDynamicContentInString(text) : expected,
                    "variables." + name, context);
        });

        if (validationContext.getVariablesJson() != null) {
            new JsonElementValidator(JsonSettings.isStrict(), context, Set.of())
                    .validate(JsonElementValidatorItem.parseJson(JsonSettings.getPermissiveMoe(),
                            GraphQlMessages.toJson(actual),
                            context.replaceDynamicContentInString(validationContext.getVariablesJson())));
        }
    }

    private static boolean hasExpectations(GraphQlMessageValidationContext validationContext) {
        return validationContext.getOperationName() != null
                || validationContext.getQuery() != null
                || !validationContext.getVariables().isEmpty()
                || validationContext.getVariablesJson() != null;
    }
}
