/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class AbstractEndpointAdapterTest {

    /** Endpoint adapter mock */
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);

    @Test
    public void testEndpointAdapter() {
        AbstractEndpointAdapter abstractEndpointAdapter = new AbstractEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return null;
            }

            @Override
            public Endpoint getEndpoint() {
                return null;
            }

            @Override
            public EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };

        Assert.assertNull(abstractEndpointAdapter.handleMessage(new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>")));
    }

    @Test
    public void testFallbackEndpointAdapter() {
        AbstractEndpointAdapter abstractEndpointAdapter = new AbstractEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                return null;
            }

            @Override
            public Endpoint getEndpoint() {
                return null;
            }

            @Override
            public EndpointConfiguration getEndpointConfiguration() {
                return null;
            }
        };

        Message request = new DefaultMessage("<TestMessage><text>Hi!</text></TestMessage>");

        reset(endpointAdapter);
        when(endpointAdapter.handleMessage(request)).thenReturn(new DefaultMessage("OK"));
        abstractEndpointAdapter.setFallbackEndpointAdapter(endpointAdapter);
        Message response = abstractEndpointAdapter.handleMessage(request);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(String.class), "OK");

    }
}
