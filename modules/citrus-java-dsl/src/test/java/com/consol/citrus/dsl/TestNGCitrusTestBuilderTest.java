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

package com.consol.citrus.dsl;

import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.dsl.definition.MockBuilder;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class TestNGCitrusTestBuilderTest {

    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testNG() {
        FooTest builder = new FooTest(applicationContextMock);

        reset(applicationContextMock);

        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();

        replay(applicationContextMock);

        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getName(), "FooTest");
        Assert.assertEquals(builder.testCase().getPackageName(), "com.consol.citrus.dsl");
        
        Assert.assertEquals(builder.testCase().getDescription(), "This is a Test");
        
        Assert.assertEquals(builder.testCase().getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(builder.testCase().getMetaInfo().getStatus(), Status.FINAL);

        verify(applicationContextMock);
    }
    
    private static class FooTest extends MockBuilder {

        /** Constructor */
        public FooTest(ApplicationContext applicationContext) {
            super(applicationContext);
        }

        @Override
        public void configure() {
            description("This is a Test");
            author("Christoph");
            status(Status.FINAL);
            
            echo("Hello Citrus!");
        }
    }
}
