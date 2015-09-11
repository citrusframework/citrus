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

package com.consol.citrus.actions;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class PurgeEndpointActionTest extends AbstractTestNGUnitTest {
	
    @Autowired
    @Qualifier(value="mockEndpoint")
    private Endpoint mockEndpoint;

    private Endpoint emptyEndpoint = EasyMock.createMock(Endpoint.class);

    private Consumer consumer = EasyMock.createMock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = EasyMock.createMock(SelectiveConsumer.class);

    @Test
    public void testPurgeWithEndpointNames() throws Exception {
        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction();
        purgeEndpointAction.setBeanFactory(applicationContext);

        List<String> endpointNames = new ArrayList<>();
        endpointNames.add("mockEndpoint");
        purgeEndpointAction.setEndpointNames(endpointNames);

        reset(mockEndpoint, consumer, selectiveConsumer);

        expect(mockEndpoint.getName()).andReturn("mockEndpoint").atLeastOnce();
        expect(mockEndpoint.createConsumer()).andReturn(consumer).once();
        expect(consumer.receive(context, 100L)).andReturn(new DefaultMessage()).once();
        expect(consumer.receive(context, 100L)).andThrow(new ActionTimeoutException()).once();

        replay(mockEndpoint, consumer, selectiveConsumer);

        purgeEndpointAction.execute(context);

        verify(mockEndpoint, consumer, selectiveConsumer);
    }

	@SuppressWarnings("unchecked")
    @Test
    public void testPurgeWithEndpointObjects() throws Exception {
	    PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction();
        purgeEndpointAction.setBeanFactory(applicationContext);

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(mockEndpoint);
        endpoints.add(emptyEndpoint);
        purgeEndpointAction.setEndpoints(endpoints);
        
        reset(mockEndpoint, emptyEndpoint, consumer, selectiveConsumer);

        expect(mockEndpoint.getName()).andReturn("mockEndpoint").atLeastOnce();
        expect(emptyEndpoint.getName()).andReturn("emptyEndpoint").atLeastOnce();
        expect(mockEndpoint.createConsumer()).andReturn(consumer).once();
        expect(emptyEndpoint.createConsumer()).andReturn(consumer).once();

        expect(consumer.receive(context, 100L)).andReturn(new DefaultMessage()).once();
        expect(consumer.receive(context, 100L)).andThrow(new ActionTimeoutException()).once();
        expect(consumer.receive(context, 100L)).andThrow(new ActionTimeoutException()).once();

        replay(mockEndpoint, emptyEndpoint, consumer, selectiveConsumer);
        
        purgeEndpointAction.execute(context);
        
        verify(mockEndpoint, emptyEndpoint, consumer, selectiveConsumer);
    }
	
	@Test
    public void testPurgeWithMessageSelectorString() throws Exception {
        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction();
        purgeEndpointAction.setBeanFactory(applicationContext);

        purgeEndpointAction.setMessageSelectorString("operation = 'sayHello'");
        
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(mockEndpoint);
        purgeEndpointAction.setEndpoints(endpoints);
        
        reset(mockEndpoint, consumer, selectiveConsumer);

        expect(mockEndpoint.getName()).andReturn("mockEndpoint").atLeastOnce();
        expect(mockEndpoint.createConsumer()).andReturn(selectiveConsumer).once();
        expect(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).andReturn(new DefaultMessage()).once();
        expect(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).andThrow(new ActionTimeoutException()).once();

        replay(mockEndpoint, consumer, selectiveConsumer);
        
        purgeEndpointAction.execute(context);
        
        verify(mockEndpoint, consumer, selectiveConsumer);
    }

    @Test
    public void testPurgeWithMessageSelector() throws Exception {
        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction();
        purgeEndpointAction.setBeanFactory(applicationContext);

        purgeEndpointAction.setMessageSelector(Collections.<String, Object>singletonMap("operation", "sayHello"));

        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(mockEndpoint);
        purgeEndpointAction.setEndpoints(endpoints);

        reset(mockEndpoint, consumer, selectiveConsumer);

        expect(mockEndpoint.getName()).andReturn("mockEndpoint").atLeastOnce();
        expect(mockEndpoint.createConsumer()).andReturn(selectiveConsumer).once();
        expect(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).andReturn(new DefaultMessage()).once();
        expect(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).andThrow(new ActionTimeoutException()).once();

        replay(mockEndpoint, consumer, selectiveConsumer);

        purgeEndpointAction.execute(context);

        verify(mockEndpoint, consumer, selectiveConsumer);
    }
	
}
