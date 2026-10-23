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

package org.citrusframework.nosql.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.citrusframework.context.TestContext;
import org.citrusframework.nosql.actions.NoSqlExtraction;
import org.citrusframework.nosql.actions.NoSqlValidation;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.model.NativeResult;
import org.citrusframework.nosql.model.NoSqlResult;
import org.citrusframework.nosql.model.NoSqlValue;
import org.citrusframework.nosql.model.QueryResult;
import org.citrusframework.nosql.model.ScalarResult;
import org.citrusframework.nosql.model.WriteResult;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

/**
 * Validation and extraction bridge for normalized NoSQL results.
 *
 * <p>JSON paths are evaluated against the canonical result model, never against raw provider JSON.
 * A path that does not exist is a failure: it differs from a path that exists with a null value.
 * The canonical model exposes the following shape:</p>
 *
 * <pre>
 * query  -&gt; { "documents": [ ... ], "totalCount": 42 }
 * write  -&gt; { "matched": 1, "modified": 1, "generatedIds": [ ... ] }
 * scalar -&gt; { "value": ... }
 * native -&gt; { "providerId": "...", "value": ..., "metadata": { ... } }
 * </pre>
 */
public final class NoSqlResultPaths {

    private NoSqlResultPaths() {
    }

    /**
     * Evaluate a JSON path against the canonical result model.
     *
     * <p>The path runs over the JSON-compatible projection of the canonical result, so provider,
     * temporal and binary leaf values evaluate to their text representations.</p>
     */
    public static NoSqlValue evaluate(NoSqlResult result, String path) {
        return read(result, path, NoSqlErrorClass.VALIDATION);
    }

    /**
     * Convert a result to a JSON compatible object model.
     */
    public static Map<String, Object> toJsonCompatible(NoSqlResult result) {
        Map<String, Object> model = new LinkedHashMap<>();

        if (result instanceof QueryResult(List<NoSqlValue> documents1, java.util.OptionalLong count)) {
            List<Object> documents = new ArrayList<>(documents1.size());
            documents1.forEach(document -> documents.add(document.toJsonCompatible()));
            model.put("documents", documents);
            count.ifPresent(totalCount -> model.put("totalCount", totalCount));
        } else if (result instanceof WriteResult(
                java.util.OptionalLong matched1, java.util.OptionalLong modified1, java.util.OptionalLong inserted1,
                java.util.OptionalLong deleted1, List<NoSqlValue> ids
        )) {
            matched1.ifPresent(matched -> model.put("matched", matched));
            modified1.ifPresent(modified -> model.put("modified", modified));
            inserted1.ifPresent(inserted -> model.put("inserted", inserted));
            deleted1.ifPresent(deleted -> model.put("deleted", deleted));
            List<Object> generatedIds = new ArrayList<>(ids.size());
            ids.forEach(generatedId -> generatedIds.add(generatedId.toJsonCompatible()));
            model.put("generatedIds", generatedIds);
        } else if (result instanceof ScalarResult(NoSqlValue value2)) {
            model.put("value", value2.toJsonCompatible());
        } else if (result instanceof NativeResult(
                String providerId, NoSqlValue value1, Map<String, NoSqlValue> metadata1
        )) {
            model.put("providerId", providerId);
            model.put("value", value1.toJsonCompatible());
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata1.forEach((key, value) -> metadata.put(key, value.toJsonCompatible()));
            model.put("metadata", metadata);
        } else {
            throw new NoSqlException(NoSqlErrorClass.EXECUTION,
                    String.format("Unsupported NoSQL result type '%s'", result.getClass().getName()));
        }

        return model;
    }

    /**
     * Validate all entries against the result. Missing paths and mismatches fail with
     * {@link NoSqlErrorClass#VALIDATION}.
     */
    public static void validate(NoSqlResult result, List<NoSqlValidation> validations, TestContext context) {
        for (NoSqlValidation validation : validations) {
            NoSqlValue actual = read(result, validation.path(), NoSqlErrorClass.VALIDATION);
            if (actual.isMissing()) {
                throw new NoSqlException(NoSqlErrorClass.VALIDATION,
                        String.format("No NoSQL result value at path '%s'", validation.path()));
            }

            String actualValue = actual.render();
            if (ValidationMatcherUtils.isValidationMatcherExpression(validation.expected())) {
                try {
                    ValidationMatcherUtils.resolveValidationMatcher(validation.path(), actualValue,
                            validation.expected(), context);
                } catch (RuntimeException e) {
                    throw new NoSqlException(NoSqlErrorClass.VALIDATION,
                            String.format("NoSQL result validation failed at path '%s': %s",
                                    validation.path(), e.getMessage()), e);
                }
            } else if (!validation.expected().equals(actualValue)) {
                throw new NoSqlException(NoSqlErrorClass.VALIDATION,
                        String.format("NoSQL result validation failed at path '%s': expected '%s' but was '%s'",
                                validation.path(), validation.expected(), actualValue));
            }
        }
    }

    /**
     * Extract all entries from the result into test variables. Missing paths fail with
     * {@link NoSqlErrorClass#EXTRACTION}.
     */
    public static void extract(NoSqlResult result, List<NoSqlExtraction> extractions, TestContext context) {
        for (NoSqlExtraction extraction : extractions) {
            NoSqlValue actual = read(result, extraction.path(), NoSqlErrorClass.EXTRACTION);
            if (actual.isMissing()) {
                throw new NoSqlException(NoSqlErrorClass.EXTRACTION,
                        String.format("No NoSQL result value at path '%s'", extraction.path()));
            }

            context.setVariable(extraction.variable(), actual.render());
        }
    }

    private static NoSqlValue read(NoSqlResult result, String path, NoSqlErrorClass errorClass) {
        try {
            JsonPath compiled = JsonPath.compile(path);
            Object value = compiled.read(toJsonCompatible(result));
            if (!compiled.isDefinite() && value instanceof List<?> list && list.isEmpty()) {
                return NoSqlValue.missing();
            }
            return NoSqlValue.of(value);
        } catch (PathNotFoundException e) {
            return NoSqlValue.missing();
        } catch (NoSqlException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new NoSqlException(errorClass,
                    String.format("Failed to evaluate NoSQL result path '%s': %s", path, e.getMessage()), e);
        }
    }
}
