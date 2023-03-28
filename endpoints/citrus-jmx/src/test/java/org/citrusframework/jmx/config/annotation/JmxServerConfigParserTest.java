/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jmx.config.annotation;

import java.util.Collections;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.jmx.mbean.HelloBean;
import org.citrusframework.jmx.mbean.NewsBean;
import org.citrusframework.jmx.message.JmxMessageConverter;
import org.citrusframework.jmx.server.JmxServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class JmxServerConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", JmxMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("environmentProperties", Properties.class)).thenReturn(environmentProperties);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        when(environmentProperties.entrySet()).thenReturn(Collections.<Object, Object>singletonMap("com.sun.management.jmxremote.authenticate", "false").entrySet());
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testJmxServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st server
        Assert.assertEquals(jmxServer1.getEndpointConfiguration().getServerUrl(), "platform");
        Assert.assertEquals(jmxServer1.getEndpointConfiguration().getEnvironmentProperties().size(), 0L);
        Assert.assertFalse(jmxServer1.isCreateRegistry());
        Assert.assertEquals(jmxServer1.getMbeans().size(), 2L);
        Assert.assertEquals(jmxServer1.getMbeans().get(0).getType(), HelloBean.class);
        Assert.assertEquals(jmxServer1.getMbeans().get(0).getOperations().size(), 0L);
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getName(), "fooBean");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().size(), 2L);
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().get(0).getName(), "fooOperation");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().size(), 2L);
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(0).getType(), "java.lang.String");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().get(0).getParameter().getParameter().get(1).getType(), "java.lang.Integer");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getOperations().get(1).getName(), "barOperation");
        Assert.assertNull(jmxServer1.getMbeans().get(1).getOperations().get(1).getParameter());
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getAttributes().size(), 2L);
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getAttributes().get(0).getName(), "fooAttribute");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getAttributes().get(0).getType(), "java.lang.String");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getAttributes().get(1).getName(), "barAttribute");
        Assert.assertEquals(jmxServer1.getMbeans().get(1).getAttributes().get(1).getType(), "java.lang.Boolean");
        Assert.assertEquals(jmxServer1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd server
        Assert.assertEquals(jmxServer2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(jmxServer2.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        Assert.assertEquals(jmxServer2.getEndpointConfiguration().getEnvironmentProperties().size(), 1L);
        Assert.assertEquals(jmxServer2.getMbeans().size(), 1L);
        Assert.assertEquals(jmxServer2.getMbeans().get(0).getType(), NewsBean.class);
        Assert.assertEquals(jmxServer2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd server
        Assert.assertNotNull(jmxServer3.getActor());
        Assert.assertEquals(jmxServer3.getEndpointConfiguration().getServerUrl(), "service:jmx:rmi:///jndi/rmi://localhost:2099/jmxrmi");
        Assert.assertEquals(jmxServer3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(jmxServer3.getEndpointConfiguration().getPort(), 2099);
        Assert.assertEquals(jmxServer3.getEndpointConfiguration().getProtocol(), "rmi");
        Assert.assertEquals(jmxServer3.getEndpointConfiguration().getBinding(), "jmxrmi");
        Assert.assertTrue(jmxServer3.isCreateRegistry());

        Assert.assertEquals(jmxServer3.getMbeans().size(), 1L);
        Assert.assertEquals(jmxServer3.getMbeans().get(0).getType(), HelloBean.class);
        Assert.assertEquals(jmxServer3.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("jmx.server").isPresent());
    }
}
