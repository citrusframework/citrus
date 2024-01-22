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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonElementValidatorItemTest {

    @DataProvider
    public static Object[][] getPathPairs() {
        return new Object[][]{
                {"$['propertyA']", new JsonElementValidatorItem<>("propertyA", "", "")},
                {"$['propertyA']", new JsonElementValidatorItem<>("propertyA", "", "").parent(
                        new JsonElementValidatorItem<>(null, "", "")
                )},
                {"$['propertyA']['propertyB']", new JsonElementValidatorItem<>("propertyB", "", "").parent(
                        new JsonElementValidatorItem<>("propertyA", "", "").parent(
                                new JsonElementValidatorItem<>(null, "", "")
                        )
                )},
                {"$['propertyA'][1]", new JsonElementValidatorItem<>(1, "", "").parent(
                        new JsonElementValidatorItem<>("propertyA", "", "").parent(
                                new JsonElementValidatorItem<>(null, "", "")
                        )
                )},
                {"$[1]", new JsonElementValidatorItem<>(1, "", "").parent(
                        new JsonElementValidatorItem<>(null, "", "")
                )}
        };
    }

    @Test(dataProvider = "getPathPairs")
    void shouldGetJsonPath(String expectedPath, JsonElementValidatorItem<?> fixture) {
        assertThat(fixture.getJsonPath()).isEqualTo(expectedPath);
    }


    @DataProvider
    public static Object[][] getNamePairs() {
        return new Object[][]{
                {"$", new JsonElementValidatorItem<>(null, "", "")},
                {"propertyA", new JsonElementValidatorItem<>("propertyA", "", "")},
                {"[2]", new JsonElementValidatorItem<>(2, "", "")}
        };
    }

    @Test(dataProvider = "getNamePairs")
    void shouldGetName(String expectedPath, JsonElementValidatorItem<?> fixture) {
        assertThat(fixture.getName()).isEqualTo(expectedPath);
    }
}