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

package org.citrusframework.testcontainers.integration.yaml;

import org.citrusframework.testcontainers.yaml.Testcontainers;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestcontainersTest {

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(YamlTestActionBuilder.lookup("testcontainers").isPresent());
        Assert.assertEquals(YamlTestActionBuilder.lookup("testcontainers").get().getClass(), Testcontainers.class);
    }
}
