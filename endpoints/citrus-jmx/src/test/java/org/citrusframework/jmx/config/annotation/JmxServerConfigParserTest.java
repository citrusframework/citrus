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

package org.citrusframework.jmx.config.annotation;

import java.util.Collections;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.jmx.mbean.HelloBean;
import org.citrusframework.jmx.mbean.NewsBean;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class JmxServerConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "jmxServer1")
    @JmxServerConfig(serverUrl="platform",
        mbeans = { @MbeanConfig(type = HelloBean.class),
                   @MbeanConfig(name = "fooBean", objectDomain = "foo.object.domain", objectName = "type=FooBean",
                   operations = { @MbeanOperation(name = "fooOperation", parameter = { String.class, Integer.class }),
                                  @MbeanOperation(name = "barOperation")
                                },
                   attributes = { @MbeanAttribute(name = "fooAttribute", type = String.class),
                                  @MbeanAttribute(name = "barAttribute", type = Boolean.class)
                                })
                 })
    private JmxServer jmxServer1;

    @CitrusEndpoint
    @JmxServerConfig(serverUrl="service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi",
            messageConverter="messageConverter",
            environmentProperties="environmentProperties",
            timeout=10000L,
            mbeans = { @MbeanConfig(type = NewsBean.class, objectDomain="some.other.domain", objectName="name=NewsBean") })
    private JmxServer jmxServer2;

    @CitrusEndpoint
    @JmxServerConfig(host="localhost",
            port=2099,
            protocol="rmi",
            binding="jmxrmi",
            createRegistry=true,
            actor="testActor",
            mbeans = { @MbeanConfig(type = HelloBean.class, objectDomain = "hello")})
    private JmxServer jmxServer3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private JmxMessageConverter messageConverter;
    @Mock
    private Properties environmentProperties;
    @Mock
    private TestActor testActor;

    @BeforeEach
    public void setup() {
        lenient().when(referenceResolver.resolve("messageConverter", JmxMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("environmentProperties", Properties.class)).thenReturn(environmentProperties);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        lenient().when(environmentProperties.entrySet()).thenReturn(Collections.<Object, Object>singletonMap("com.sun.management.jmxremote.authenticate", "false").entrySet());
    }

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmxServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assertions.assertEquals("platform", jmxServer1.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals(0L, jmxServer1.getEndpointConfiguration().getEnvironmentProperties().size());
        Assertions.assertFalse(jmxServer1.isCreateRegistry());
        Assertions.assertEquals(2L, jmxServer1.getMbeans().size());
        Assertions.assertEquals(HelloBean.class, jmxServer1.getMbeans().get(0).getType());
        Assertions.assertEquals(0L, jmxServer1.getMbeans().get(0).getOperations().size());
        Assertions.assertEquals("fooBean", jmxServer1.getMbeans().get(1).getName());
        Assertions.assertEquals(2L, jmxServer1.getMbeans().get(1).getOperations().size());
        Assertions.assertEquals("fooOperation", jmxServer1.getMbeans().get(1).getOperations().get(0).getName());
        Assertions.assertEquals(2L, jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().size());
        Assertions.assertEquals("java.lang.String", jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(0).getType());
        Assertions.assertEquals("java.lang.Integer", jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(1).getType());
        Assertions.assertEquals("barOperation", jmxServer1.getMbeans().get(1).getOperations().get(1).getName());
        Assertions.assertNull(jmxServer1.getMbeans().get(1).getOperations().get(1).getParameter());
        Assertions.assertEquals(2L, jmxServer1.getMbeans().get(1).getAttributes().size());
        Assertions.assertEquals("fooAttribute", jmxServer1.getMbeans().get(1).getAttributes().get(0).getName());
        Assertions.assertEquals("java.lang.String", jmxServer1.getMbeans().get(1).getAttributes().get(0).getType());
        Assertions.assertEquals("barAttribute", jmxServer1.getMbeans().get(1).getAttributes().get(1).getName());
        Assertions.assertEquals("java.lang.Boolean", jmxServer1.getMbeans().get(1).getAttributes().get(1).getType());
        Assertions.assertEquals(5000L, jmxServer1.getEndpointConfiguration().getTimeout());

        // 2nd server
        Assertions.assertEquals(messageConverter, jmxServer2.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi", jmxServer2.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals(1L, jmxServer2.getEndpointConfiguration().getEnvironmentProperties().size());
        Assertions.assertEquals(1L, jmxServer2.getMbeans().size());
        Assertions.assertEquals(NewsBean.class, jmxServer2.getMbeans().get(0).getType());
        Assertions.assertEquals(10000L, jmxServer2.getEndpointConfiguration().getTimeout());

        // 3rd server
        Assertions.assertNotNull(jmxServer3.getActor());
        Assertions.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:2099/jmxrmi", jmxServer3.getEndpointConfiguration().getServerUrl());
        Assertions.assertEquals("localhost", jmxServer3.getEndpointConfiguration().getHost());
        Assertions.assertEquals(2099, jmxServer3.getEndpointConfiguration().getPort());
        Assertions.assertEquals("rmi", jmxServer3.getEndpointConfiguration().getProtocol());
        Assertions.assertEquals("jmxrmi", jmxServer3.getEndpointConfiguration().getBinding());
        Assertions.assertTrue(jmxServer3.isCreateRegistry());

        Assertions.assertEquals(1L, jmxServer3.getMbeans().size());
        Assertions.assertEquals(HelloBean.class, jmxServer3.getMbeans().get(0).getType());
        Assertions.assertEquals(testActor, jmxServer3.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("jmx.server").isPresent());
    }
}
