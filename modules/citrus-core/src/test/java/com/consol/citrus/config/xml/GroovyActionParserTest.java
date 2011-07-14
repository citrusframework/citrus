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
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testFailActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 4);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), GroovyAction.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "groovy");
        
        Assert.assertNull(((GroovyAction)getTestCase().getActions().get(0)).getFileResource());
        Assert.assertEquals(((GroovyAction)getTestCase().getActions().get(0)).getScriptTemplateResource().getFilename(), "script-template.groovy");
        Assert.assertEquals(((GroovyAction)getTestCase().getActions().get(0)).getScript().trim(), "println 'Hello Citrus'");
        
        Assert.assertNull(((GroovyAction)getTestCase().getActions().get(1)).getFileResource());
        Assert.assertNotNull(((GroovyAction)getTestCase().getActions().get(1)).getScript());
        Assert.assertEquals(((GroovyAction)getTestCase().getActions().get(1)).isUseScriptTemplate(), false);
        
        Assert.assertNull(((GroovyAction)getTestCase().getActions().get(2)).getFileResource());
        Assert.assertEquals(((GroovyAction)getTestCase().getActions().get(2)).getScriptTemplateResource().getFilename(), "custom-script-template.groovy");
        Assert.assertNotNull(((GroovyAction)getTestCase().getActions().get(2)).getScript());
        
        Assert.assertNotNull(((GroovyAction)getTestCase().getActions().get(3)).getFileResource());
        Assert.assertEquals(((GroovyAction)getTestCase().getActions().get(3)).getFileResource().getFilename(), "example.groovy");
        Assert.assertNull(((GroovyAction)getTestCase().getActions().get(3)).getScript());
    }
}
