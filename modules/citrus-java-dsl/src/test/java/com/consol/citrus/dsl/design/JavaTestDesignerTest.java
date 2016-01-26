/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.JavaAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class JavaTestDesignerTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testJavaBuilderWithClassName() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final List<Object> constructorArgs = new ArrayList<Object>();
        constructorArgs.add(5);
        constructorArgs.add(7);
        
        final List<Object> methodArgs = new ArrayList<Object>();
        methodArgs.add(4);
        
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                java("com.consol.citrus.dsl.util.JavaTest")
                      .constructorArgs(constructorArgs)
                      .methodArgs(methodArgs)
                      .method("add");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), JavaAction.class);
        
        JavaAction action = ((JavaAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "java");
        
        Assert.assertEquals(action.getClassName(), "com.consol.citrus.dsl.util.JavaTest");
        Assert.assertNull(action.getInstance());
        Assert.assertEquals(action.getMethodName(), "add");
        Assert.assertEquals(action.getMethodArgs().size(), 1);
    }
    
    @Test
    public void testJavaBuilderWithClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final List<Object> methodArgs = new ArrayList<Object>();
        methodArgs.add(new TestContext());
        
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                java(EchoAction.class)
                      .methodArgs(methodArgs)
                      .method("execute");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), JavaAction.class);
        
        JavaAction action = ((JavaAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "java");
        
        Assert.assertEquals(action.getClassName(), EchoAction.class.getSimpleName());
        Assert.assertNull(action.getInstance());
        Assert.assertEquals(action.getMethodName(), "execute");
        Assert.assertEquals(action.getMethodArgs().size(), 1);
    }
    
    @Test
    public void testJavaBuilderWithObjectInstance() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final List<Object> methodArgs = new ArrayList<Object>();
        methodArgs.add(new TestContext());
        
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                java(new EchoAction())
                      .methodArgs(methodArgs)
                      .method("execute");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), JavaAction.class);
        
        JavaAction action = ((JavaAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "java");
        
        Assert.assertNull(action.getClassName());
        Assert.assertNotNull(action.getInstance());
        Assert.assertEquals(action.getMethodName(), "execute");
        Assert.assertEquals(action.getMethodArgs().size(), 1);
    }

}
