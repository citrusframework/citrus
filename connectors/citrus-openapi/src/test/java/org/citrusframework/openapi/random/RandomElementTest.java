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

package org.citrusframework.openapi.random;

import org.citrusframework.openapi.random.RandomElement.RandomValue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

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
        RandomValue value = new RandomValue("testValue");
        randomList.push(value);
        assertEquals(randomList.size(), 1);
        assertEquals(randomList.get(0), value);
    }

    @Test
    public void testRandomListPushKeyValue() {
        RandomValue value = new RandomValue("value");
        randomList.push(new RandomElement.RandomObject());
        randomList.push("key", value);
        assertEquals(((RandomElement.RandomObject) randomList.get(0)).get("key"), value);
    }

    @Test
    public void testRandomObjectPushKeyValue() {
        RandomValue value = new RandomValue("value");
        randomObject.push("key", value);
        assertEquals(randomObject.get("key"), value);
    }

    @Test
    public void testRandomObjectPushRandomObject() {
        RandomElement.RandomObject nestedObject = new RandomElement.RandomObject();
        RandomValue value = new RandomValue("nestedValue");
        nestedObject.push("nestedKey", value);
        randomObject.push(nestedObject);
        assertEquals(randomObject.size(), 1);
        assertEquals(randomObject.get("nestedKey"), value);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRandomObjectPushValueThrowsException() {
        randomObject.push(new RandomValue("value"));
    }

    @Test
    public void testRandomValuePushValue() {
        RandomValue value = new RandomValue("testValue");
        randomValue.push(value);
        assertEquals(randomValue.getValue(), value);
    }

    @Test
    public void testRandomValuePushRandomElement() {
        RandomElement.RandomObject nestedObject = new RandomElement.RandomObject();
        randomValue = new RandomElement.RandomValue(nestedObject);
        RandomValue value = new RandomValue("value");
        randomValue.push("key", value);
        assertEquals(((RandomElement.RandomObject) randomValue.getValue()).get("key"), value);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRandomValuePushKeyValueThrowsException() {
        randomValue.push("key", new RandomValue("value"));
    }
}
