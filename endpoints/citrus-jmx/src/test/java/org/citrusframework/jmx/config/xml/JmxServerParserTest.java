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

package org.citrusframework.jmx.config.xml;

import org.citrusframework.TestActor;
import org.citrusframework.jmx.mbean.HelloBean;
import org.citrusframework.jmx.mbean.NewsBean;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @since 2.5
 */
public class JmxServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testJmxServerParser() {
        Map<String, JmxServer> endpoints = beanDefinitionContext.getBeansOfType(JmxServer.class);

        Assertions.assertEquals(3, endpoints.size());

        // 1st server
        JmxServer jmxServer = endpoints.get("jmxServer1");
        Assertions.assertEquals("platform", jmxServer.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals(0L, jmxServer.getEndpointConfiguration().getEnvironmentProperties().size());
        Assertions.assertFalse(jmxServer.isCreateRegistry());
        Assertions.assertEquals(2L, jmxServer.getMbeans().size());
        Assertions.assertEquals(HelloBean.class, jmxServer.getMbeans().get(0).getType());
        Assertions.assertEquals(0L, jmxServer.getMbeans().get(0).getOperations().size());
        Assertions.assertEquals("fooBean", jmxServer.getMbeans().get(1).getName());
        Assertions.assertEquals(2L, jmxServer.getMbeans().get(1).getOperations().size());
        Assertions.assertEquals("fooOperation", jmxServer.getMbeans().get(1).getOperations().get(0).getName());
        Assertions.assertEquals(2L, jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().size());
        Assertions.assertEquals("java.lang.String", jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(0).getType());
        Assertions.assertEquals("java.lang.Integer", jmxServer.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(1).getType());
        Assertions.assertEquals("barOperation", jmxServer.getMbeans().get(1).getOperations().get(1).getName());
        Assertions.assertNull(jmxServer.getMbeans().get(1).getOperations().get(1).getParameter());
        Assertions.assertEquals(2L, jmxServer.getMbeans().get(1).getAttributes().size());
        Assertions.assertEquals("fooAttribute", jmxServer.getMbeans().get(1).getAttributes().get(0).getName());
        Assertions.assertEquals("java.lang.String", jmxServer.getMbeans().get(1).getAttributes().get(0).getType());
        Assertions.assertEquals("barAttribute", jmxServer.getMbeans().get(1).getAttributes().get(1).getName());
        Assertions.assertEquals("java.lang.Boolean", jmxServer.getMbeans().get(1).getAttributes().get(1).getType());
        Assertions.assertEquals(5000L, jmxServer.getEndpointConfiguration().getTimeout());

        // 2nd server
        jmxServer = endpoints.get("jmxServer2");
        Assertions.assertEquals(beanDefinitionContext.getBean("messageConverter"), jmxServer.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", jmxServer.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals(1L, jmxServer.getEndpointConfiguration().getEnvironmentProperties().size());
        Assertions.assertEquals(1L, jmxServer.getMbeans().size());
        Assertions.assertEquals(NewsBean.class, jmxServer.getMbeans().get(0).getType());
        Assertions.assertEquals(10000L, jmxServer.getEndpointConfiguration().getTimeout());

        // 3rd server
        jmxServer = endpoints.get("jmxServer3");
        Assertions.assertNotNull(jmxServer.getActor());
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:2099/jmxrmi", jmxServer.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals("localhost", jmxServer.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, jmxServer.getEndpointConfiguration().getPort());
        Assertions.assertEquals("rmi", jmxServer.getEndpointConfiguration().getProtocol());
        Assertions.assertEquals("jmxrmi", jmxServer.getEndpointConfiguration().getBinding());
        Assertions.assertTrue(jmxServer.isCreateRegistry());

        Assertions.assertEquals(1L, jmxServer.getMbeans().size());
        Assertions.assertEquals(HelloBean.class, jmxServer.getMbeans().get(0).getType());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), jmxServer.getActor());
    }
}
