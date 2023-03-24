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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.JavaAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.citrusframework.util.InvocationDummy;

/**
 * @author Christoph Deppisch
 */
public class JavaActionParserTest extends AbstractActionParserTest<JavaAction> {

    @Test
    public void testJavaActionParser() {
        assertActionCount(3);
        assertActionClassAndName(JavaAction.class, "java");
        
        JavaAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getClassName(), "org.citrusframework.util.InvocationDummy");
        Assert.assertEquals(action.getMethodName(), "invoke");
        Assert.assertEquals(action.getConstructorArgs().size(), 1);
        Assert.assertEquals(action.getConstructorArgs().get(0), "Test Invocation");
        Assert.assertEquals(action.getMethodArgs().size(), 1);
        Assert.assertEquals(action.getMethodArgs().get(0), new String[] {"1", "2"});
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getClassName(), "org.citrusframework.util.InvocationDummy");
        Assert.assertEquals(action.getMethodName(), "invoke");
        Assert.assertEquals(action.getConstructorArgs().size(), 0);
        Assert.assertEquals(action.getMethodArgs().size(), 3);
        Assert.assertEquals(action.getMethodArgs().get(0), 4);
        Assert.assertEquals(action.getMethodArgs().get(1), "Test");
        Assert.assertEquals(action.getMethodArgs().get(2), true);
        
        action = getNextTestActionFromTest();
        Assert.assertNull(action.getClassName());
        Assert.assertNotNull(action.getInstance());
        Assert.assertEquals(action.getInstance().getClass(), InvocationDummy.class);
        Assert.assertEquals(action.getMethodName(), "invoke");
        Assert.assertEquals(action.getConstructorArgs().size(), 0);
        Assert.assertEquals(action.getMethodArgs().get(0), 0);
        Assert.assertEquals(action.getMethodArgs().get(1), "Test invocation");
        Assert.assertEquals(action.getMethodArgs().get(2), false);
    }
    
    @Test
    public void testUnsupportedMethodType() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to unsupported method type");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().contains(
                    "unsupported method argument type: 'integer'"));
        }
    }
}
