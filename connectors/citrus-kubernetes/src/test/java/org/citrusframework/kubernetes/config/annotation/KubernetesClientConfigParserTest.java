/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.kubernetes.config.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.citrusframework.annotations.CitrusAnnotations.injectEndpoints;
import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 */
public class KubernetesClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "k8sClient1")
    @KubernetesClientConfig()
    private KubernetesClient client1;

    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version="v1",
            username="user",
            password="s!cr!t",
            namespace="user_namespace",
            messageConverter="messageConverter",
            objectMapper="objectMapper")
    private KubernetesClient client2;

    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version="v1",
            oauthToken = "xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51",
            namespace="user_namespace",
            messageConverter="messageConverter",
            objectMapper="objectMapper")
    private KubernetesClient client3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KubernetesMessageConverter messageConverter;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeClass
    public void setup() {
        openMocks(this);

        when(referenceResolver.resolve("messageConverter", KubernetesMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("objectMapper", ObjectMapper.class)).thenReturn(objectMapper);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testKubernetesClientParser() {
        injectEndpoints(this, context);

        // 1st client
        assertNotNull(client1.getClient());

        // 2nd client
        assertNotNull(client2.getClient());
        assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8443/");
        assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        assertEquals(client2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        assertEquals(client2.getEndpointConfiguration().getObjectMapper(), objectMapper);

        // 3rd client
        assertNotNull(client3.getClient());
        assertEquals(client3.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl(), "http://localhost:8443/");
        assertEquals(client3.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        assertEquals(client3.getEndpointConfiguration().getKubernetesClientConfig().getOauthToken(), "xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51");
        assertEquals(client3.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        assertEquals(client3.getEndpointConfiguration().getMessageConverter(), messageConverter);
        assertEquals(client3.getEndpointConfiguration().getObjectMapper(), objectMapper);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = lookup();
        assertEquals(validators.size(), 5L);
        assertNotNull(validators.get("direct.async"));
        assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        assertNotNull(validators.get("direct.sync"));
        assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        assertNotNull(validators.get("http.client"));
        assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        assertNotNull(validators.get("http.server"));
        assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        assertNotNull(validators.get("k8s.client"));
        assertEquals(validators.get("k8s.client").getClass(), KubernetesClientConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("k8s.client").isPresent());
    }
}
