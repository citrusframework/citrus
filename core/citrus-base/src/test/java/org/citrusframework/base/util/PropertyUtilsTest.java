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

package org.citrusframework.base.util;

import org.citrusframework.TestActor;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.util.PropertyUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertyUtilsTest extends UnitTestSupport {

    @Test
    public void shouldBindEndpointConfigurationProperties() {
        System.setProperty("citrus.endpoint.config.foo.queueName", "fooQueue");
        System.setProperty("citrus.endpoint.config.foo.timeout", "100");

        DirectEndpoint endpoint = new DirectEndpoint();
        context.getReferenceResolver().bind("foo", endpoint);

        PropertyUtils.configure("foo", endpoint, context.getReferenceResolver());

        Assert.assertEquals(endpoint.getEndpointConfiguration().getQueueName(), "fooQueue");
        Assert.assertEquals(endpoint.getEndpointConfiguration().getTimeout(), 100L);
    }

    @Test
    public void shouldBindEndpointBeanReference() {
        System.setProperty("citrus.endpoint.bar.actor", "#bean:testActor");
        System.setProperty("citrus.endpoint.config.bar.queue", "#bean:fooQueue");

        DirectEndpoint endpoint = new DirectEndpoint();
        context.getReferenceResolver().bind("bar", endpoint);
        context.getReferenceResolver().bind("fooQueue", new DefaultMessageQueue("fooQueue"));
        context.getReferenceResolver().bind("testActor", new TestActor("testActor"));

        PropertyUtils.configure("bar", endpoint, context.getReferenceResolver());

        Assert.assertEquals(endpoint.getEndpointConfiguration().getQueue(), context.getReferenceResolver().resolve("fooQueue"));
        Assert.assertEquals(endpoint.getActor(), context.getReferenceResolver().resolve("testActor"));
    }
}
