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

package org.citrusframework.camel.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelSyncEndpointParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testCamelSyncEndpointParser() {
        Map<String, CamelSyncEndpoint> endpoints = beanDefinitionContext.getBeansOfType(CamelSyncEndpoint.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st message receiver
        CamelSyncEndpoint camelEndpoint = endpoints.get("camelSyncEndpoint1");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getCamelContext(), beanDefinitionContext.getBean("camelContext"));
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getEndpointUri(), "direct:news-feed1");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        camelEndpoint = endpoints.get("camelSyncEndpoint2");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getCamelContext(), beanDefinitionContext.getBean("specialCamelContext"));
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getEndpointUri(), "direct:news-feed2");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        camelEndpoint = endpoints.get("camelSyncEndpoint3");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getEndpointUri(), "direct:news-feed3");
        Assert.assertEquals(camelEndpoint.getEndpointConfiguration().getPollingInterval(), 200L);
        Assert.assertEquals(camelEndpoint.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
