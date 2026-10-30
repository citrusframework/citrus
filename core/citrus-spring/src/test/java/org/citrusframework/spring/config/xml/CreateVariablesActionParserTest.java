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

import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateVariablesActionParserTest extends AbstractActionParserTest<CreateVariablesAction> {

    @Test
    public void testCreateVariablesActionParser() {
        assertActionCount(1);
        assertActionClassAndName(CreateVariablesAction.class, "create-variables");

        CreateVariablesAction action = getNextTestActionFromTest();
        Assertions.assertEquals(3, action.getVariables().size());
        Assertions.assertEquals("Hello", action.getVariables().get("text"));
        Assertions.assertEquals("script:<groovy>(1+2+3+4+5)", action.getVariables().get("sum"));
        Assertions.assertEquals("<embeddedXml>This is an embedded Xml variable value</embeddedXml>", action.getVariables().get("embeddedXml"));
    }
}
