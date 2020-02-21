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

package com.consol.citrus.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionParserTest extends AbstractActionParserTest<GroovyAction> {

    @Test
    public void testActionParser() {
        assertActionCount(4);
        assertActionClassAndName(GroovyAction.class, "groovy");
        
        GroovyAction action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:com/consol/citrus/script/script-template.groovy");
        Assert.assertEquals(action.getScript().trim(), "println 'Hello Citrus'");
        
        action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertNotNull(action.getScript());
        Assert.assertEquals(action.isUseScriptTemplate(), false);
        
        action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:com/consol/citrus/script/custom-script-template.groovy");
        Assert.assertNotNull(action.getScript());
        
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptResourcePath(), "classpath:com/consol/citrus/script/example.groovy");
        Assert.assertNull(action.getScript());
    }
}
