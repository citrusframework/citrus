/*
 * Copyright 2024 the original author or authors.
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

package org.citrusframework.validation.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;

import java.util.List;
import java.util.stream.Stream;

import static com.jayway.jsonpath.Option.AS_PATH_LIST;
import static org.citrusframework.validation.ValidationUtils.buildValueMismatchErrorMessage;

/**
 * Wraps all needed data to validate an actual json with an expected json-template.
 * @see JsonElementValidator for usage
 *
 * @param <T> the type of the actual and expected json
 */
public class JsonElementValidatorItem<T> {
    private final String name;
    private final Integer index;
    public final T actual;
    public final T expected;
    private JsonElementValidatorItem<?> parent;

    /**
     * Parses and wraps the given json's.
     *
     * @param permissiveMode see {@code JSONParser#MODE_*} or {@link JSONParser#DEFAULT_PERMISSIVE_MODE}
     * @param actualJson as string
     * @param expectedJson as string
     * @return the two json's wrapped in a {@link JsonElementValidatorItem<Object>}
     */
    public static JsonElementValidatorItem<Object> parseJson(int permissiveMode, String actualJson, String expectedJson) {
        JSONParser parser = new JSONParser(permissiveMode);
        try {
            return new JsonElementValidatorItem<>(null, parser.parse(actualJson), parser.parse(expectedJson));
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to parse JSON text", e);
        }
    }

    /**
     * For array-items.
     *
     * @param index of the item in the {@link JsonElementValidatorItem#parent}-element
     */
    public JsonElementValidatorItem(int index, T actual, T expected) {
        this.name = null;
        this.index = index;
        this.actual = actual;
        this.expected = expected;
    }

    /**
     * For array-items.
     *
     * @param name/key of the json value in the {@link JsonElementValidatorItem#parent}-element
     */
    public JsonElementValidatorItem(String name, T actual, T expected) {
        this.name = name;
        this.index = null;
        this.actual = actual;
        this.expected = expected;
    }

    /**
     * @return {@link JsonElementValidatorItem#actual} as string or null
     */
    public String actualAsStringOrNull() {
        return actual == null ? null : actual.toString();
    }

    /**
     * @return {@link JsonElementValidatorItem#expected} as string or null
     */
    public String expectedAsStringOrNull() {
        return expected == null ? null : expected.toString();
    }

    /**
     * @throws ValidationException if either {@link JsonElementValidatorItem#expected}
     *         or {@link JsonElementValidatorItem#expected} is not of the given {@code type}
     * @param type to cast the values to
     * @return {@link this} as {@link JsonElementValidatorItem<O>}
     */
    public <O> JsonElementValidatorItem<O> ensureType(Class<O> type) {
        JsonElementValidatorItem<?> self = this;
        if (((actual != null) && !type.isInstance(actual)) || ((expected != null) && !type.isInstance(expected))) {
            throw new ValidationException(buildValueMismatchErrorMessage(
                    "Type mismatch for JSON entry '" + name + "'",
                    type.getSimpleName(),
                    actual == null ? null : actual.getClass().getSimpleName()
            ));
        }
        return (JsonElementValidatorItem<O>) self;
    }

    /**
     * Set the parent of this json-element.
     */
    public JsonElementValidatorItem<T> parent(JsonElementValidatorItem<?> parent) {
        this.parent = parent;
        return this;
    }

    /**
     * The json path as string from the root to this item, i.e. $['books'][1]['name']
     */
    public String getJsonPath() {
        String parentPath = parent == null ? "$" : parent.getJsonPath();
        if (index != null) {
            return parentPath + "[%s]".formatted(index);
        }
        if (name != null) {
            return parentPath + "['%s']".formatted(name);
        }
        return parentPath;
    }

    /**
     * The identifier of a json element
     * <ul>
     *     <li>null on root</li>
     *     <li>{@link JsonElementValidatorItem#name} for an entry in a json map</li>
     *     <li>{@link JsonElementValidatorItem#index} in square brackets for an item in a json array, i.e. {@code "[2]"}</li>
     * </ul>
     *
     * @return
     */
    public String getName() {
        if (index != null) return "[%s]".formatted(index);
        if (name != null) return name;
        return "$";
    }

    public JsonElementValidatorItem<?> getRoot() {
        return parent == null ? this : parent.getRoot();
    }

    public boolean isPathIgnoredBy(String jsonPathExpression) {
        String currentPath = getJsonPath();
        return Stream.concat(
                getAllMatchedPathsInJson(jsonPathExpression, getRoot().expected),
                getAllMatchedPathsInJson(jsonPathExpression, getRoot().actual)
        ).anyMatch(currentPath::equals);
    }

    private Stream<String> getAllMatchedPathsInJson(String jsonPathExpression, Object json) {
        Configuration config = Configuration.builder().options(AS_PATH_LIST).build();
        List<String> foundJsonPaths;
        try {
            foundJsonPaths = JsonPath.using(config).parse(json).read(jsonPathExpression);
        } catch (JsonPathException e) {
            return Stream.of();
        }
        return foundJsonPaths.stream();
    }
}
