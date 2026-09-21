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

package org.citrusframework.camel.actions;

import java.util.Map;

import org.citrusframework.spi.Resources;
import org.testng.annotations.Test;

import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.Assert.assertEquals;

public class CamelRunIntegrationActionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldLetInlineSystemPropertiesOverrideFile() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .withSystemProperties(Resources.create(
                        "classpath:org/citrusframework/camel/actions/system.properties"))
                .withSystemProperty("server.port", "9090")
                .withSystemProperty("greeting", "Hello inline")
                .build();

        Map<String, String> props = (Map<String, String>) getField(action, "systemProperties");

        assertEquals(props.get("server.port"), "9090");
        assertEquals(props.get("greeting"), "Hello inline");
        assertEquals(props.get("server.host"), "localhost");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldLetInlineSystemPropertiesOverrideFileRegardlessOfOrder() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .withSystemProperty("server.port", "9090")
                .withSystemProperty("greeting", "Hello inline")
                .withSystemProperties(Resources.create(
                        "classpath:org/citrusframework/camel/actions/system.properties"))
                .build();

        Map<String, String> props = (Map<String, String>) getField(action, "systemProperties");

        assertEquals(props.get("server.port"), "9090");
        assertEquals(props.get("greeting"), "Hello inline");
        assertEquals(props.get("server.host"), "localhost");
    }
}
