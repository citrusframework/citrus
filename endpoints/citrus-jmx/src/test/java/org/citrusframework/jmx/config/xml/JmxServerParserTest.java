/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.jmx.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.jmx.mbean.HelloBean;
import org.citrusframework.jmx.mbean.NewsBean;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JmxServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmxServerParser() {
        Map<String, JmxServer> endpoints = beanDefinitionContext.getBeansOfType(JmxServer.class);

        Assert.assertEquals(endpoints.size(), 3);

        // 1st server
        JmxServer jmxServer = endpoints.get("jmxServer1");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getEnvironmentProperties().size(), 0L);
        Assert.assertEquals(jmxServer.isCreateRegistry(), false);
        Assert.assertEquals(jmxServer.getMbeans().size(), 2L);
        Assert.assertEquals(jmxServer.getMbeans().get(0).getType(), HelloBean.class);
        Assert.assertEquals(jmxServer.getMbeans().get(0).getOperations().size(), 0L);
        Assert.assertEquals(jmxServer.getMbeans().get(1).getName(), "fooBean");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().size(), 2L);
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().get(0).getName(), "fooOperation");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().size(), 2L);
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(0).getType(), "java.lang.String");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(1).getType(), "java.lang.Integer");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getOperations().get(1).getName(), "barOperation");
        Assert.assertNull(jmxServer.getMbeans().get(1).getOperations().get(1).getParameter());
        Assert.assertEquals(jmxServer.getMbeans().get(1).getAttributes().size(), 2L);
        Assert.assertEquals(jmxServer.getMbeans().get(1).getAttributes().get(0).getName(), "fooAttribute");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getAttributes().get(0).getType(), "java.lang.String");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getAttributes().get(1).getName(), "barAttribute");
        Assert.assertEquals(jmxServer.getMbeans().get(1).getAttributes().get(1).getType(), "java.lang.Boolean");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd server
        jmxServer = endpoints.get("jmxServer2");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getMessageConverter(), beanDefinitionContext.getBean("messageConverter"));
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getEnvironmentProperties().size(), 1L);
        Assert.assertEquals(jmxServer.getMbeans().size(), 1L);
        Assert.assertEquals(jmxServer.getMbeans().get(0).getType(), NewsBean.class);
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd server
        jmxServer = endpoints.get("jmxServer3");
        Assert.assertNotNull(jmxServer.getActor());
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:2099/jmxrmi");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getProtocol(), "rmi");
        Assert.assertEquals(jmxServer.getEndpointConfiguration().getBinding(), "jmxrmi");
        Assert.assertEquals(jmxServer.isCreateRegistry(), true);

        Assert.assertEquals(jmxServer.getMbeans().size(), 1L);
        Assert.assertEquals(jmxServer.getMbeans().get(0).getType(), HelloBean.class);
        Assert.assertEquals(jmxServer.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
