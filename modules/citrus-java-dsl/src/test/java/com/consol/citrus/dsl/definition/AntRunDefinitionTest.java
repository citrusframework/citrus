/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.tools.ant.BuildListener;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AntRunAction;

/**
 * @author Christoph Deppisch
 */
public class AntRunDefinitionTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testAntRunBuilder() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                antrun("com/consol/ant/build.xml")
                    .target("doBuild");
            }
        };
        
        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/ant/build.xml");
        Assert.assertEquals(action.getTarget(), "doBuild");
    }
    
    @Test
    public void testAntRunBuilderWithTargets() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                antrun("com/consol/ant/build.xml")
                    .targets("prepare", "test", "release");
            }
        };
        
        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/ant/build.xml");
        Assert.assertNull(action.getTarget());
        Assert.assertEquals(action.getTargets(), "prepare,test,release");
    }
    
    @Test
    public void testAntRunBuilderWithProperty() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                antrun("com/consol/ant/build.xml")
                    .target("doBuild")
                    .property("name", "MyBuildTest")
                    .property("filePath", "/home/sayHello.txt");
            }
        };
        
        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/ant/build.xml");
        Assert.assertEquals(action.getTarget(), "doBuild");
        Assert.assertEquals(action.getProperties().size(), 2L);
        Assert.assertEquals(action.getProperties().getProperty("name"), "MyBuildTest");
        Assert.assertEquals(action.getProperties().getProperty("filePath"), "/home/sayHello.txt");
    }
    
    @Test
    public void testAntRunBuilderWithPropertyFile() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                antrun("com/consol/ant/build.xml")
                    .target("doBuild")
                    .propertyFile("/ant/build.properties");
            }
        };
        
        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildFilePath(), "com/consol/ant/build.xml");
        Assert.assertEquals(action.getTarget(), "doBuild");
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertEquals(action.getPropertyFilePath(), "/ant/build.properties");
    }
    
    @Test
    public void testAntRunBuilderWithBuildListener() {
        final BuildListener buildListener = EasyMock.createMock(BuildListener.class);
        
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                antrun("com/consol/ant/build.xml")
                    .target("doBuild")
                    .listener(buildListener);
            }
        };
        
        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AntRunAction.class);
        
        AntRunAction action = (AntRunAction)builder.testCase().getActions().get(0);
        Assert.assertEquals(action.getName(), "antrun");
        Assert.assertEquals(action.getBuildListener(), buildListener);
    }
}
