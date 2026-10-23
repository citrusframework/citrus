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

package org.citrusframework.nosql.actions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

import org.citrusframework.context.TestContext;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.citrusframework.nosql.spi.NoSqlPayload;

/**
 * Immutable model of a NoSQL test action. The specification is the only place that holds action
 * input; substitution produces a new instance and never mutates the original, so a specification
 * can be executed in parallel by multiple tests.
 *
 * <p>Dynamic content substitution applies to the payload and to expected validation values only.
 * Targets, operation names and variable names are used verbatim.</p>
 */
public record NoSqlActionSpec(String client,
                              NoSqlOperationKind kind,
                              String nativeCommand,
                              String target,
                              NoSqlPayload payload,
                              List<NoSqlValidation> validations,
                              List<NoSqlExtraction> extractions,
                              OptionalInt maxResults,
                              NoSqlRetryPolicy retryPolicy) {

    public NoSqlActionSpec {
        Objects.requireNonNull(kind, "Operation kind must not be null");
        Objects.requireNonNull(payload, "Payload must not be null");
        Objects.requireNonNull(validations, "Validations must not be null");
        Objects.requireNonNull(extractions, "Extractions must not be null");
        Objects.requireNonNull(maxResults, "Max results must not be null");
        Objects.requireNonNull(retryPolicy, "Retry policy must not be null");

        if (client == null || client.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL action requires a database configuration name");
        }
        if (target == null || target.isBlank()) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL action requires a non-blank target");
        }
        if (maxResults.isPresent() && maxResults.getAsInt() < 1) {
            throw new NoSqlException(NoSqlErrorClass.CONFIGURATION,
                    "NoSQL max results must be greater than zero");
        }

        validations = List.copyOf(validations);
        extractions = List.copyOf(extractions);
    }

    /**
     * Replace dynamic content in the designated expression fields and return a new specification.
     *
     * <p>A substitution failure is reported as {@link NoSqlErrorClass#INVALID_PAYLOAD} for the
     * payload and {@link NoSqlErrorClass#VALIDATION} for an expected validation value.</p>
     */
    public NoSqlActionSpec substitute(TestContext context) {
        NoSqlPayload substitutedPayload;
        try {
            substitutedPayload = new NoSqlPayload(substituteValue(payload.value(), context));
        } catch (NoSqlException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                    String.format("Failed to substitute NoSQL payload of client '%s': %s",
                            client, e.getMessage()), e);
        }

        List<NoSqlValidation> substitutedValidations = new ArrayList<>(validations.size());
        for (NoSqlValidation validation : validations) {
            String expected;
            try {
                expected = context.replaceDynamicContentInString(validation.expected());
            } catch (RuntimeException e) {
                throw new NoSqlException(NoSqlErrorClass.VALIDATION,
                        String.format("Failed to substitute expected value at path '%s': %s",
                                validation.path(), e.getMessage()), e);
            }

            substitutedValidations.add(new NoSqlValidation(validation.path(), expected));
        }

        return new NoSqlActionSpec(client, kind, nativeCommand, target, substitutedPayload,
                substitutedValidations, extractions, maxResults, retryPolicy);
    }

    /**
     * Substitute dynamic content in a canonical value tree. Structured values are traversed
     * recursively, all other values stay untouched.
     */
    private NoSqlValue substituteValue(NoSqlValue value, TestContext context) {
        if (value instanceof NoSqlValue.StringValue stringValue) {
            return NoSqlValue.string(context.replaceDynamicContentInString(stringValue.value()));
        } else if (value instanceof NoSqlValue.ObjectValue objectValue) {
            Map<String, NoSqlValue> substituted = new LinkedHashMap<>();
            objectValue.values().forEach((key, entryValue) -> substituted.put(key, substituteValue(entryValue, context)));
            return NoSqlValue.object(substituted);
        } else if (value instanceof NoSqlValue.ArrayValue arrayValue) {
            List<NoSqlValue> substituted = new ArrayList<>(arrayValue.values().size());
            arrayValue.values().forEach(entryValue -> substituted.add(substituteValue(entryValue, context)));
            return NoSqlValue.array(substituted);
        }

        return value;
    }

    /**
     * Payload is mandatory for writes and native operations. Queries and deletes accept an empty
     * payload, which means "match all" for document stores.
     */
    public boolean requiresPayload() {
        return kind == NoSqlOperationKind.INSERT
                || kind == NoSqlOperationKind.UPDATE
                || kind == NoSqlOperationKind.NATIVE;
    }

    @Override
    public String toString() {
        return String.format("NoSqlActionSpec[client=%s, kind=%s, target=%s, nativeCommand=%s]",
                client, kind, target, nativeCommand);
    }
}
