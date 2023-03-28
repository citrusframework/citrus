/*
 *  Copyright 2006-2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.config.xml;

import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class TestCaseParserTest extends AbstractActionParserTest<EchoAction> {

    @Test
    public void testVariablesParser() {
        assertActionCount(1);
        assertActionClassAndName(EchoAction.class, "echo");

        TestCase test = getTestCase();
        Map<String, Object> variables = test.getVariableDefinitions();
        Assert.assertEquals(variables.size(), 3);
        Assert.assertEquals(variables.get("text"), "Hello");
        Assert.assertEquals(variables.get("sum"), "15");
        Assert.assertEquals(variables.get("embeddedXml"), "<embeddedXml>works!</embeddedXml>");
    }
}
