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

package org.citrusframework.jbang.cli.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.jbang.cli.CitrusJBangMain.Settings.CAMEL_VERSION_DEFAULT;

public class XmlCodeAnalyzerTest {

    @Test
    public void shouldAnalyzeCode() throws IOException {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();
        CodeAnalyzer.ScanResult scanResult = analyzer.scan(Resources.create("sample.citrus.it.xml"));

        Assert.assertEquals(scanResult.name(), "sample.citrus.it.xml");

        Assert.assertEquals(scanResult.modules().length, 7L);
        String[] foundModules = Arrays.stream(scanResult.modules()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundModules[0], "citrus-base");
        Assert.assertEquals(foundModules[1], "citrus-camel");
        Assert.assertEquals(foundModules[2], "citrus-http");
        Assert.assertEquals(foundModules[3], "citrus-jbang-connector");
        Assert.assertEquals(foundModules[4], "citrus-jms");
        Assert.assertEquals(foundModules[5], "citrus-kafka");
        Assert.assertEquals(foundModules[6], "citrus-testcontainers");

        Assert.assertEquals(scanResult.dependencies().length, 4L);
        String[] foundDeps = Arrays.stream(scanResult.dependencies()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundDeps[0], "org.apache.camel:camel-aws2-s3:${camel.version}");
        Assert.assertEquals(foundDeps[1], "org.apache.camel:camel-paho-mqtt5:" + CAMEL_VERSION_DEFAULT);
        Assert.assertEquals(foundDeps[2], "org.apache.camel:camel-test-infra-aws-v2:" + CAMEL_VERSION_DEFAULT);
        Assert.assertEquals(foundDeps[3], "org.apache.camel:camel-test-infra-kafka:" + CAMEL_VERSION_DEFAULT);

        Assert.assertEquals(scanResult.actions().length, 5L);
        String[] foundActions = Arrays.stream(scanResult.actions()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundActions[0], "camel-cli-run");
        Assert.assertEquals(foundActions[1], "camel-infra-run");
        Assert.assertEquals(foundActions[2], "print");
        Assert.assertEquals(foundActions[3], "receive");
        Assert.assertEquals(foundActions[4], "send");

        Assert.assertEquals(scanResult.containers().length, 1L);
        Assert.assertEquals(scanResult.containers()[0], "iterate");

        Assert.assertEquals(scanResult.endpoints().length, 5L);
        String[] foundEndpoints = Arrays.stream(scanResult.endpoints()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundEndpoints[0], "camel");
        Assert.assertEquals(foundEndpoints[1], "direct");
        Assert.assertEquals(foundEndpoints[2], "http");
        Assert.assertEquals(foundEndpoints[3], "jms");
        Assert.assertEquals(foundEndpoints[4], "kafka");

        Assert.assertEquals(scanResult.functions().length, 1L);
        Assert.assertEquals(scanResult.functions()[0], "citrus:randomNumber");

        Assert.assertEquals(scanResult.validationMatcher().length, 2L);
        String[] foundValidationMatcher = Arrays.stream(scanResult.validationMatcher()).sorted().toArray(String[]::new);
        Assert.assertEquals(foundValidationMatcher[0], "ignore");
        Assert.assertEquals(foundValidationMatcher[1], "isNumber");
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithTypeAttribute() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint type="kafka" auto-close="true">
                    <properties>
                      <property name="topic" value="my-topic"/>
                    </properties>
                  </create-endpoint>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assert.assertTrue(endpoints.contains("kafka"));
        Assert.assertTrue(modules.contains("citrus-kafka"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithChildElement() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint name="myHttpEndpoint">
                    <http>
                      <client requestUrl="http://localhost:8080"/>
                    </http>
                  </create-endpoint>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assert.assertTrue(endpoints.contains("http"));
        Assert.assertTrue(modules.contains("citrus-http"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithUri() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint name="myJmsEndpoint" uri="jms:my-queue"/>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assert.assertTrue(endpoints.contains("jms"));
        Assert.assertTrue(modules.contains("citrus-jms"));
    }

}
