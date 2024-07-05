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

import java.util.ArrayDeque;
import java.util.Deque;
import org.citrusframework.openapi.util.RandomElement.RandomList;
import org.citrusframework.openapi.util.RandomElement.RandomObject;
import org.citrusframework.openapi.util.RandomElement.RandomValue;

/**
 * RandomModelBuilder is a class for building random JSON models. It supports adding
 * simple values, objects, properties, and arrays to the JSON structure. The final
 * model can be converted to a JSON string using the `writeToJson` method. I
 * <p>
 * The builder is able to build nested structures and can also handle native string,
 * number, and boolean elements, represented as functions for later dynamic string
 * conversion by Citrus.
 * <p>
 * Example usage:
 * <pre>
 * RandomModelBuilder builder = new RandomModelBuilder();
 * builder.object(() -> {
 *     builder.property("key1", () -> builder.appendSimple("value1"));
 *     builder.property("key2", () -> builder.array(() -> {
 *         builder.appendSimple("value2");
 *         builder.appendSimple("value3");
 *     }));
 * });
 * String json = builder.writeToJson();
 * </pre>
 */
public class RandomModelBuilder {

    final Deque<RandomElement> deque = new ArrayDeque<>();

    public RandomModelBuilder() {
        deque.push(new RandomValue());
    }

    public String toString() {
        return RandomModelWriter.toString(this);
    }

    public void appendSimple(String nativeValue) {
        if (deque.isEmpty()) {
            deque.push(new RandomValue(nativeValue));
        } else {
            deque.peek().push(nativeValue);
        }
    }

    public void object(Runnable objectBuilder) {
        if (deque.isEmpty()) {
            throwIllegalState();
        }

        RandomObject randomObject = new RandomObject();
        deque.peek().push(randomObject);
        objectBuilder.run();
    }

    private static void throwIllegalState() {
        throw new IllegalStateException("Encountered empty stack!");
    }

    public void property(String key, Runnable valueBuilder) {
        if (deque.isEmpty()) {
            throwIllegalState();
        }

        RandomValue randomValue = new RandomValue();
        deque.peek().push(key, randomValue);

        deque.push(randomValue);
        valueBuilder.run();
        deque.pop();
    }

    public void array(Runnable arrayBuilder) {
        if (deque.isEmpty()) {
            throwIllegalState();
        }
        RandomList randomList = new RandomList();
        deque.peek().push(randomList);

        // For a list, we need to push the list to the queue. This is because when the builder adds elements
        // to the list, and we are dealing with nested lists, we can otherwise not distinguish whether to put
        // an element into the list or into the nested list.
        deque.push(randomList);
        arrayBuilder.run();
        deque.pop();
    }

}
