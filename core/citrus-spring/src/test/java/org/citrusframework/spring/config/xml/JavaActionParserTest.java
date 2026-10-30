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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;

import org.citrusframework.actions.JavaAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.spring.util.InvocationDummy;

public class JavaActionParserTest extends AbstractActionParserTest<JavaAction> {

    @Test
    public void testJavaActionParser() {
        assertActionCount(3);
        assertActionClassAndName(JavaAction.class, "java");

        JavaAction action = getNextTestActionFromTest();
        Assertions.assertEquals("org.citrusframework.spring.util.InvocationDummy", action.getClassName());
        Assertions.assertEquals("invoke", action.getMethodName());
        Assertions.assertEquals(1, action.getConstructorArgs().size());
        Assertions.assertEquals("Test Invocation", action.getConstructorArgs().get(0));
        Assertions.assertEquals(1, action.getMethodArgs().size());
        Assertions.assertArrayEquals( new String[] {"1", "2"}, (String[]) action.getMethodArgs().get(0));

        action = getNextTestActionFromTest();
        Assertions.assertEquals("org.citrusframework.spring.util.InvocationDummy", action.getClassName());
        Assertions.assertEquals("invoke", action.getMethodName());
        Assertions.assertEquals(0, action.getConstructorArgs().size());
        Assertions.assertEquals(3, action.getMethodArgs().size());
        Assertions.assertEquals(4, action.getMethodArgs().get(0));
        Assertions.assertEquals("Test", action.getMethodArgs().get(1));
        Assertions.assertEquals(true, action.getMethodArgs().get(2));

        action = getNextTestActionFromTest();
        Assertions.assertNull(action.getClassName());
        Assertions.assertNotNull(action.getInstance());
        Assertions.assertEquals(InvocationDummy.class, action.getInstance().getClass());
        Assertions.assertEquals("invoke", action.getMethodName());
        Assertions.assertEquals(0, action.getConstructorArgs().size());
        Assertions.assertEquals(0, action.getMethodArgs().get(0));
        Assertions.assertEquals("Test invocation", action.getMethodArgs().get(1));
        Assertions.assertEquals(false, action.getMethodArgs().get(2));
    }

    @Test
    public void testUnsupportedMethodType() {
        try {
            createApplicationContext("failed");
            Assertions.fail("Missing bean creation exception due to unsupported method type");
        } catch (BeanDefinitionStoreException e) {
            Assertions.assertTrue(e.getCause().getMessage().contains(
                    "unsupported method argument type: 'integer'"));
        }
    }
}
