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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class PurgeEndpointTestDesignerTest extends AbstractTestNGUnitTest {
    private Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint2 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint3 = Mockito.mock(Endpoint.class);
    
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testPurgeEndpointsBuilderWithEndpoints() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .endpoints(endpoint1, endpoint2)
                        .endpoint(endpoint3);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "purge-endpoint");

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpoints().size(), 3);
        Assert.assertEquals(action.getEndpoints().toString(), "[" + endpoint1.toString() + ", " + endpoint2.toString() + ", " + endpoint3.toString() + "]");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);
    }
    
    @Test
    public void testPurgeEndpointBuilderWithNames() {
        reset(applicationContextMock);

        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .withApplicationContext(applicationContextMock)
                        .endpointNames("e1", "e2", "e3")
                        .endpoint("e4")
                        .selector("operation = 'sayHello'");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 4);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1, e2, e3, e4]");
        Assert.assertEquals(action.getBeanFactory(), applicationContextMock);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);

    }

    @Test
    public void testPurgeEndpointBuilderWithMessageSelector() {
        reset(applicationContextMock);

        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .withApplicationContext(applicationContextMock)
                        .endpointNames("e1", "e2", "e3")
                        .endpoint("e4")
                        .selector(Collections.<String, Object>singletonMap("operation", "sayHello"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 4);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1, e2, e3, e4]");
        Assert.assertEquals(action.getBeanFactory(), applicationContextMock);
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1);

    }
    
    @Test
    public void testMissingEndpointResolver() {
        reset(applicationContextMock);

        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .endpoint("e1");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), PurgeEndpointAction.class);

        PurgeEndpointAction action = (PurgeEndpointAction) test.getActions().get(0);
        Assert.assertEquals(action.getEndpointNames().size(), 1);
        Assert.assertEquals(action.getEndpointNames().toString(), "[e1]");
        Assert.assertNotNull(action.getBeanFactory());
        Assert.assertTrue(action.getBeanFactory() instanceof ApplicationContext);

    }
}
