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

package org.citrusframework.citrus.dsl.design;

import java.util.Collections;
import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.PurgeEndpointAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class PurgeEndpointTestDesignerTest extends UnitTestSupport {
    private Endpoint endpoint1 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint2 = Mockito.mock(Endpoint.class);
    private Endpoint endpoint3 = Mockito.mock(Endpoint.class);

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testPurgeEndpointsBuilderWithEndpoints() {
        MockTestDesigner builder = new MockTestDesigner(context) {
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
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .withReferenceResolver(referenceResolver)
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
        Assert.assertEquals(action.getReferenceResolver(), referenceResolver);
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0);

    }

    @Test
    public void testPurgeEndpointBuilderWithMessageSelector() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                purgeEndpoints()
                        .withReferenceResolver(referenceResolver)
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
        Assert.assertEquals(action.getReferenceResolver(), referenceResolver);
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1);

    }

    @Test
    public void testMissingEndpointResolver() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
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
        Assert.assertNotNull(action.getReferenceResolver());
    }
}
