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

import com.consol.citrus.actions.JavaAction;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;
import com.consol.citrus.util.InvocationDummy;

/**
 * @author Christoph Deppisch
 */
public class JavaActionParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testJavaActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 3);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), JavaAction.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "java");
        
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getClassName(), "com.consol.citrus.util.InvocationDummy");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getMethodName(), "invoke");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getConstructorArgs().size(), 1);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getConstructorArgs().get(0), "Test Invocation");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getMethodArgs().size(), 1);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(0)).getMethodArgs().get(0), new String[] {"1", "2"});
        
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getClassName(), "com.consol.citrus.util.InvocationDummy");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getMethodName(), "invoke");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getConstructorArgs().size(), 0);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getMethodArgs().size(), 3);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getMethodArgs().get(0), 4);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getMethodArgs().get(1), "Test");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(1)).getMethodArgs().get(2), true);
        
        Assert.assertNull(((JavaAction)getTestCase().getActions().get(2)).getClassName());
        Assert.assertNotNull(((JavaAction)getTestCase().getActions().get(2)).getInstance());
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(2)).getInstance().getClass(), InvocationDummy.class);
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(2)).getMethodName(), "invoke");
        Assert.assertEquals(((JavaAction)getTestCase().getActions().get(2)).getConstructorArgs().size(), 0);
    }
}
