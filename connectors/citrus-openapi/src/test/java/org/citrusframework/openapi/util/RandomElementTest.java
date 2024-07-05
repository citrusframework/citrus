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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RandomElementTest {

    private RandomElement.RandomList randomList;
    private RandomElement.RandomObject randomObject;
    private RandomElement.RandomValue randomValue;

    @BeforeMethod
    public void setUp() {
        randomList = new RandomElement.RandomList();
        randomObject = new RandomElement.RandomObject();
        randomValue = new RandomElement.RandomValue();
    }

    @Test
    public void testRandomListPushValue() {
        randomList.push("testValue");
        assertEquals(randomList.size(), 1);
        assertEquals(randomList.get(0), "testValue");
    }

    @Test
    public void testRandomListPushKeyValue() {
        randomList.push(new RandomElement.RandomObject());
        randomList.push("key", "value");
        assertEquals(((RandomElement.RandomObject) randomList.get(0)).get("key"), "value");
    }

    @Test
    public void testRandomObjectPushKeyValue() {
        randomObject.push("key", "value");
        assertEquals(randomObject.get("key"), "value");
    }

    @Test
    public void testRandomObjectPushRandomObject() {
        RandomElement.RandomObject nestedObject = new RandomElement.RandomObject();
        nestedObject.push("nestedKey", "nestedValue");
        randomObject.push(nestedObject);
        assertEquals(randomObject.size(), 1);
        assertEquals(randomObject.get("nestedKey"), "nestedValue");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRandomObjectPushValueThrowsException() {
        randomObject.push("value");
    }

    @Test
    public void testRandomValuePushValue() {
        randomValue.push("testValue");
        assertEquals(randomValue.getValue(), "testValue");
    }

    @Test
    public void testRandomValuePushRandomElement() {
        RandomElement.RandomObject nestedObject = new RandomElement.RandomObject();
        randomValue = new RandomElement.RandomValue(nestedObject);
        randomValue.push("key", "value");
        assertEquals(((RandomElement.RandomObject) randomValue.getValue()).get("key"), "value");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRandomValuePushKeyValueThrowsException() {
        randomValue.push("key", "value");
    }
}
