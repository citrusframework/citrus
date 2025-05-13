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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlNodeValidatorItemTest {

    @DataProvider
    public static Object[][] getPathPairs() {
        return new Object[][]{
                {"propertyA", new YamlNodeValidatorItem<>("propertyA", "", "")},
                {"$.propertyA", new YamlNodeValidatorItem<>("propertyA", "", "").parent(
                        new YamlNodeValidatorItem<>("$", "", "")
                )},
                {"$.propertyA.propertyB", new YamlNodeValidatorItem<>("propertyB", "", "").parent(
                        new YamlNodeValidatorItem<>("propertyA", "", "").parent(
                                new YamlNodeValidatorItem<>("$", "", "")
                        )
                )},
                {"$.propertyA[1]", new YamlNodeValidatorItem<>(1, "", "").parent(
                        new YamlNodeValidatorItem<>("propertyA", "", "").parent(
                                new YamlNodeValidatorItem<>("$", "", "")
                        )
                )},
                {"$[1]", new YamlNodeValidatorItem<>(1, "", "").parent(
                        new YamlNodeValidatorItem<>("$", "", "")
                )}
        };
    }

    @Test(dataProvider = "getPathPairs")
    void shouldGetNodePath(String expectedPath, YamlNodeValidatorItem<?> fixture) {
        assertThat(fixture.getNodePath()).isEqualTo(expectedPath);
    }

    @DataProvider
    public static Object[][] getNamePairs() {
        return new Object[][]{
                {"$", new YamlNodeValidatorItem<>(null, "", "")},
                {"propertyA", new YamlNodeValidatorItem<>("propertyA", "", "")},
                {"[2]", new YamlNodeValidatorItem<>(2, "", "")}
        };
    }

    @Test(dataProvider = "getNamePairs")
    void shouldGetName(String expectedPath, YamlNodeValidatorItem<?> fixture) {
        assertThat(fixture.getName()).isEqualTo(expectedPath);
    }
}
