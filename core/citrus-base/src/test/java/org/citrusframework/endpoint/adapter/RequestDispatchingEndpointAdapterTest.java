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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.mapping.MappingKeyExtractor;
import org.citrusframework.endpoint.adapter.mapping.SimpleMappingStrategy;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class RequestDispatchingEndpointAdapterTest {

    @Test
    public void testDispatchRequest() {
        RequestDispatchingEndpointAdapter endpointAdapter = new RequestDispatchingEndpointAdapter();

        endpointAdapter.setMappingKeyExtractor(new MappingKeyExtractor() {
            @Override
            public String extractMappingKey(Message request) {
                return "foo";
            }
        });

        SimpleMappingStrategy mappingStrategy = new SimpleMappingStrategy();
        mappingStrategy.setAdapterMappings(Collections.<String, EndpointAdapter>singletonMap("foo", new EmptyResponseEndpointAdapter()));
        endpointAdapter.setMappingStrategy(mappingStrategy);

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestMessage>Hello World!</TestMessage>"));

        Assert.assertEquals(response.getPayload(), "");
    }
}
