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

package org.citrusframework.openapi.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Interface representing a random element in a JSON structure. This interface provides default
 * methods to push values into the element, which can be overridden by implementing classes.
 */
public interface RandomElement {

    default void push(Object value) {
        throw new UnsupportedOperationException();
    }

    default void push(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * A random element representing an array. Array elements can be of type String (native
     * attribute) or {@link RandomElement}.
     */
    class RandomList extends ArrayList<Object> implements RandomElement {

        @Override
        public void push(Object value) {
            add(value);
        }

        @Override
        public void push(String key, Object value) {
            if (!isEmpty()) {
                Object lastElement = get(size() - 1);
                if (lastElement instanceof RandomElement randomElement) {
                    randomElement.push(key, value);
                }
            }
        }
    }

    /**
     * A random object representing a JSON object, with attributes stored as key-value pairs. Values
     * are of type String (simple attributes) or {@link RandomElement}.
     */
    class RandomObject extends LinkedHashMap<String, Object> implements RandomElement {

        @Override
        public void push(String key, Object value) {
            put(key, value);
        }

        @Override
        public void push(Object value) {
            if (value instanceof RandomObject randomObject) {
                this.putAll(randomObject);
                return;
            }
            RandomElement.super.push(value);
        }
    }

    /**
     * A random value that either holds a String (simple property) or a random element.
     */
    class RandomValue implements RandomElement {

        private Object value;

        public RandomValue() {
        }

        public RandomValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public void push(Object pushedValue) {
            if (value instanceof RandomElement randomElement) {
                randomElement.push(pushedValue);
            } else {
                this.value = pushedValue;
            }
        }

        @Override
        public void push(String key, Object pushedValue) {
            if (value instanceof RandomElement randomElement) {
                randomElement.push(key, pushedValue);
            } else {
                throw new IllegalStateException("Cannot push key/value to value: " + value);
            }
        }

    }
}