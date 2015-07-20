/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.GroovyActionBuilder;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;

import static org.easymock.EasyMock.*;

public class GroovyTestRunnerTest extends AbstractTestNGUnitTest {
    private Resource scriptResource = EasyMock.createMock(Resource.class);
    private Resource scriptTemplate = EasyMock.createMock(Resource.class);
    private File file = EasyMock.createMock(File.class);
            
    @Test
    public void testGroovyBuilderWithResource() throws IOException {
        reset(scriptResource);
        expect(scriptResource.getInputStream()).andReturn(new ByteArrayInputStream("println 'Wow groovy!'".getBytes())).once();
        replay(scriptResource);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                groovy(new BuilderSupport<GroovyActionBuilder>() {
                    @Override
                    public void configure(GroovyActionBuilder builder) {
                        builder.script(scriptResource)
                                .skipTemplate();
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "groovy");
        
        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Wow groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);
        
        verify(scriptResource);
    }
    
    @Test
    public void testGroovyBuilderWithScript() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                groovy(new BuilderSupport<GroovyActionBuilder>() {
                    @Override
                    public void configure(GroovyActionBuilder builder) {
                        builder.script("println 'Groovy!'")
                                .skipTemplate();
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);
    }
    
    @Test
    public void testGroovyBuilderWithTemplate() throws IOException {
        reset(scriptTemplate, file);
        expect(scriptTemplate.getFile()).andReturn(file).once();
        expect(file.getAbsolutePath()).andReturn("classpath:com/consol/citrus/script/script-template.groovy").once();
        replay(scriptTemplate, file);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                groovy(new BuilderSupport<GroovyActionBuilder>() {
                    @Override
                    public void configure(GroovyActionBuilder builder) {
                        builder.script("context.setVariable('message', 'Groovy!')")
                                .template(scriptTemplate);
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:com/consol/citrus/script/script-template.groovy");
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
    
    @Test
    public void testGroovyBuilderWithTemplatePath() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                groovy(new BuilderSupport<GroovyActionBuilder>() {
                    @Override
                    public void configure(GroovyActionBuilder builder) {
                        builder.script("context.setVariable('message', 'Groovy!')")
                                .template("classpath:com/consol/citrus/script/script-template.groovy");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Groovy!");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)test.getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplatePath());
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
}