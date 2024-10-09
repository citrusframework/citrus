/*
 * Copyright the original author or authors.
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

import java.util.Collections;

import org.citrusframework.channel.ChannelEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.context.TestContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ChannelEndpointFactoryTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testResolveChannelEndpoint() throws Exception {
        reset(referenceResolver);
        when(referenceResolver.resolveAll(EndpointComponent.class)).thenReturn(Collections.emptyMap());
        TestContext context = new TestContext();
        context.setReferenceResolver(referenceResolver);

        DefaultEndpointFactory factory = new DefaultEndpointFactory();
        Endpoint endpoint = factory.create("channel:channel.name", context);

        Assert.assertEquals(endpoint.getClass(), ChannelEndpoint.class);
        Assert.assertEquals(((ChannelEndpoint)endpoint).getEndpointConfiguration().getChannelName(), "channel.name");
    }
}
