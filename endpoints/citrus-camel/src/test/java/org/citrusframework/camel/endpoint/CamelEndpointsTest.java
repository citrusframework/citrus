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

package org.citrusframework.camel.endpoint;

import java.util.Map;

import org.citrusframework.endpoint.EndpointBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CamelEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("camel.sync"));
        Assert.assertTrue(endpointBuilders.containsKey("camel.inOnly"));
        Assert.assertTrue(endpointBuilders.containsKey("camel.async"));
        Assert.assertTrue(endpointBuilders.containsKey("camel.inOut"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("camel.sync").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("camel.sync").get().getClass(), CamelSyncEndpointBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("camel.inOut").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("camel.inOut").get().getClass(), CamelSyncEndpointBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("camel.async").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("camel.async").get().getClass(), CamelEndpointBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("camel.inOnly").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("camel.inOnly").get().getClass(), CamelEndpointBuilder.class);
    }

}
