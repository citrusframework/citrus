/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.channel.endpoint.builder;

import java.util.Map;

import org.citrusframework.channel.ChannelEndpointBuilder;
import org.citrusframework.channel.ChannelSyncEndpointBuilder;
import org.citrusframework.endpoint.EndpointBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MessageChannelEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("channel.sync"));
        Assert.assertTrue(endpointBuilders.containsKey("channel.async"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("channel.sync").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("channel.sync").get().getClass(), ChannelSyncEndpointBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("channel.async").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("channel.async").get().getClass(), ChannelEndpointBuilder.class);
    }

}
