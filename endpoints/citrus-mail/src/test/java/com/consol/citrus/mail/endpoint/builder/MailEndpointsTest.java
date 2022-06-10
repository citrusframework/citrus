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

package com.consol.citrus.mail.endpoint.builder;

import java.util.Map;

import com.consol.citrus.endpoint.EndpointBuilder;
import com.consol.citrus.mail.client.MailClientBuilder;
import com.consol.citrus.mail.server.MailServerBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class MailEndpointsTest {

    @Test
    public void shouldLookupEndpoints() {
        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("mail.client"));
        Assert.assertTrue(endpointBuilders.containsKey("mail.server"));
    }

    @Test
    public void shouldLookupEndpoint() {
        Assert.assertTrue(EndpointBuilder.lookup("mail.client").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("mail.client").get().getClass(), MailClientBuilder.class);
        Assert.assertTrue(EndpointBuilder.lookup("mail.server").isPresent());
        Assert.assertEquals(EndpointBuilder.lookup("mail.server").get().getClass(), MailServerBuilder.class);
    }

}
