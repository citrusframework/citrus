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

package org.citrusframework.integration;

import java.util.stream.Stream;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.citrusframework.junit.jupiter.CitrusTestFactory;
import org.citrusframework.junit.jupiter.CitrusTestFactorySupport;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;

/**
 * Runs a YAML file holding several tests through the JUnit Jupiter host runners.
 */
@CitrusSupport
public class YamlMultiTestLoaderIT {

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = { "classpath:org/citrusframework/integration/multi/orders.citrus.it.yaml" })
    void multiTestFile_IT() {
    }

    @Test
    @CitrusTestSource(type = TestLoader.YAML, sources = {
            "classpath:org/citrusframework/yaml/actions/echo.citrus.it.yaml",
            "classpath:org/citrusframework/integration/multi/orders.citrus.it.yaml" })
    void severalSourcesWithMultiTestFile_IT() {
    }

    @CitrusTestFactory
    public Stream<DynamicNode> packageScanNodes_IT() {
        return CitrusTestFactorySupport.yaml().packageScanNodes("org.citrusframework.integration.multi");
    }

    @CitrusTestFactory
    public Stream<DynamicNode> dynamicNodes_IT() {
        return CitrusTestFactorySupport.yaml().dynamicNodes("org.citrusframework.integration.multi", "orders.citrus.it");
    }
}
