/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class CreateVariablesActionParserTest extends AbstractActionParserTest<CreateVariablesAction> {

    @Test
    public void testCreateVariablesActionParser() {
        assertActionCount(1);
        assertActionClassAndName(CreateVariablesAction.class, "create-variables");
        
        CreateVariablesAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariables().size(), 3);
        Assert.assertEquals(action.getVariables().get("text"), "Hello");
        Assert.assertEquals(action.getVariables().get("sum"), "script:<groovy>(1+2+3+4+5)");
        Assert.assertEquals(action.getVariables().get("embeddedXml"), "<embeddedXml>This is an embedded Xml variable value</embeddedXml>");
    }
    
}
