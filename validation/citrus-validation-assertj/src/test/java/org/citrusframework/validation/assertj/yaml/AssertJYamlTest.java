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

package org.citrusframework.validation.assertj.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertJYamlTest extends AbstractYamlActionTest {

    @Test
    public void shouldValidateWithAssertJInYaml() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/validation/assertj/yaml/assertj-validation.citrus.it.yaml");

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        assertThat(result.getName()).isEqualTo("AssertJValidationTest");
        assertThat(result.getActionCount()).isEqualTo(5L);
        assertThat(context.getVariable("i")).isEqualTo("2");
    }
}
