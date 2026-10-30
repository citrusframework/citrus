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

package org.citrusframework.spring.config.xml;

import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.4
 */
public class TestCaseParserTest extends AbstractActionParserTest<EchoAction> {

    @Test
    public void testVariablesParser() {
        assertActionCount(1);
        assertActionClassAndName(EchoAction.class, "echo");

        TestCase test = getTestCase();
        Map<String, Object> variables = test.getVariableDefinitions();
        Assertions.assertEquals(3, variables.size());
        Assertions.assertEquals("Hello", variables.get("text"));
        Assertions.assertEquals("15", variables.get("sum"));
        Assertions.assertEquals("<embeddedXml>works!</embeddedXml>", variables.get("embeddedXml"));
    }
}
