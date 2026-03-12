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

package org.citrusframework.jbang.util;

import java.io.IOException;
import java.util.Arrays;

import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class YamlCodeAnalyzerTest {

    @Test
    public void shouldListComponents() throws IOException {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();
        CodeAnalyzer.ScanResult scanResult = analyzer.scan(Resources.create("sample.citrus.it.yaml"));

        Assert.assertEquals(scanResult.name(), "sample.citrus.it.yaml");

        Assert.assertEquals(scanResult.modules().length, 3L);
        String[] foundModules = Arrays.stream(scanResult.modules()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundModules[0], "citrus-base");
        Assert.assertEquals(foundModules[1], "citrus-camel");
        Assert.assertEquals(foundModules[2], "citrus-testcontainers");

        Assert.assertEquals(scanResult.dependencies().length, 1L);
        String[] foundDeps = Arrays.stream(scanResult.dependencies()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundDeps[0], "org.apache.camel:camel-aws2-s3:4.18.0");

        Assert.assertEquals(scanResult.actions().length, 3L);
        String[] foundActions = Arrays.stream(scanResult.actions()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundActions[0], "print");
        Assert.assertEquals(foundActions[1], "receive");
        Assert.assertEquals(foundActions[2], "send");

        Assert.assertEquals(scanResult.containers().length, 1L);
        Assert.assertEquals(scanResult.containers()[0], "iterate");

        Assert.assertEquals(scanResult.endpoints().length, 3L);
        String[] foundEndpoints = Arrays.stream(scanResult.endpoints()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundEndpoints[0], "http");
        Assert.assertEquals(foundEndpoints[1], "jms");
        Assert.assertEquals(foundEndpoints[2], "kafka");

        Assert.assertEquals(scanResult.functions().length, 1L);
        Assert.assertEquals(scanResult.functions()[0], "citrus:randomNumber");

        Assert.assertEquals(scanResult.validationMatcher().length, 2L);
        String[] foundValidationMatcher = Arrays.stream(scanResult.validationMatcher()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundValidationMatcher[0], "ignore");
        Assert.assertEquals(foundValidationMatcher[1], "isNumber");
    }

}
