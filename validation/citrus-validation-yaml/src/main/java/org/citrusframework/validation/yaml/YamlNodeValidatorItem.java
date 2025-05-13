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

package org.citrusframework.validation.yaml;

import org.citrusframework.exceptions.ValidationException;

import static org.citrusframework.validation.ValidationUtils.buildValueMismatchErrorMessage;

/**
 * Wraps all needed data to validate an actual Yaml with an expected Yaml template.
 *
 * @param <T> the type of the actual and expected yaml
 * @see YamlNodeValidator for usage
 */
public class YamlNodeValidatorItem<T> {
    private final String name;
    private final Integer index;
    public final T actual;
    public final T expected;
    private YamlNodeValidatorItem<?> parent;

    /**
     * For array-items.
     *
     * @param index of the item in the {@link YamlNodeValidatorItem#parent}-element
     */
    public YamlNodeValidatorItem(int index, T actual, T expected) {
        this.name = null;
        this.index = index;
        this.actual = actual;
        this.expected = expected;
    }

    /**
     * For object-items.
     *
     * @param name/key of the Yaml value in the {@link YamlNodeValidatorItem#parent}-element
     */
    public YamlNodeValidatorItem(String name, T actual, T expected) {
        this.name = name;
        this.index = null;
        this.actual = actual;
        this.expected = expected;
    }

    /**
     * @return {@link YamlNodeValidatorItem#actual} as string or null
     */
    public String actualAsStringOrNull() {
        return actual == null ? null : actual.toString();
    }

    /**
     * @return {@link YamlNodeValidatorItem#expected} as string or null
     */
    public String expectedAsStringOrNull() {
        return expected == null ? null : expected.toString();
    }

    /**
     * @param type to cast the values to
     * @return {@link this} as {@link YamlNodeValidatorItem<O>}
     * @throws ValidationException if either {@link YamlNodeValidatorItem#expected}
     *                             or {@link YamlNodeValidatorItem#expected} is not of the given {@code type}
     */
    public <O> YamlNodeValidatorItem<O> ensureType(Class<O> type) {
        if (((actual != null) && !type.isInstance(actual)) || ((expected != null) && !type.isInstance(expected))) {
            throw new ValidationException(buildValueMismatchErrorMessage(
                    "Type mismatch for JSON entry '" + name + "'",
                    type.getSimpleName(),
                    actual == null ? null : actual.getClass().getSimpleName()
            ));
        }
        return (YamlNodeValidatorItem<O>) this;
    }

    /**
     * Set the parent of this Yaml element.
     */
    public YamlNodeValidatorItem<T> parent(YamlNodeValidatorItem<?> parent) {
        this.parent = parent;
        return this;
    }

    /**
     * The node path as string from the root to this item, i.e. $['books'][1]['name']
     */
    public String getNodePath() {
        String parentPath = parent == null ? "" : parent.getNodePath();
        if (index != null) {
            return parentPath + "[%s]".formatted(index);
        }
        if (name != null) {
            return parentPath.isEmpty() ? name :  parentPath + ".%s".formatted(name);
        }
        return parentPath;
    }

    /**
     * The identifier of a Yaml element
     * <ul>
     *     <li>null on root</li>
     *     <li>{@link YamlNodeValidatorItem#name} for an entry in a yaml map</li>
     *     <li>{@link YamlNodeValidatorItem#index} in square brackets for an item in a yaml array, i.e. {@code "[2]"}</li>
     * </ul>
     */
    public String getName() {
        if (index != null) return "[%s]".formatted(index);
        if (name != null) return name;
        return "$";
    }

    public boolean isPathIgnoredBy(String pathExpression) {
        return pathExpression.equals(getNodePath());
    }

    public YamlNodeValidatorItem<Object> child(int expectedIndex, Object actual, Object expected) {
        return new YamlNodeValidatorItem<>(
                expectedIndex,
                actual,
                expected
        );
    }
}
