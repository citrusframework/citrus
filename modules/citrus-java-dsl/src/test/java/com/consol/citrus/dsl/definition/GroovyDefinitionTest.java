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

package com.consol.citrus.dsl.definition;

import org.easymock.EasyMock;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.script.GroovyAction;

public class GroovyDefinitionTest {
    private Resource scriptResource = EasyMock.createMock(Resource.class);
    
    private Resource scriptTemplate = EasyMock.createMock(Resource.class);
            
    @Test
    public void testGroovyBuilderWithResource() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                groovy(scriptResource)
                    .skipTemplate();
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getFileResource(), scriptResource);
        Assert.assertEquals(action.isUseScriptTemplate(), false);
    }
    
    @Test
    public void testGroovyBuilderWithScript() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .skipTemplate();
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getScript(), "println 'Groovy!'");
        Assert.assertEquals(action.isUseScriptTemplate(), false);
    }
    
    @Test
    public void testGroovyBuilderWithTemplate() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .template(scriptTemplate);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getScriptTemplateResource(), scriptTemplate);
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
    
    @Test
    public void testGroovyBuilderWithTemplatePath() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                groovy("println 'Groovy!'")
                    .template("classpath:script-template.groovy");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), GroovyAction.class);
        
        GroovyAction action = (GroovyAction)builder.getTestCase().getActions().get(0);
        Assert.assertNotNull(action.getScriptTemplateResource());
        Assert.assertEquals(action.isUseScriptTemplate(), true);
    }
}