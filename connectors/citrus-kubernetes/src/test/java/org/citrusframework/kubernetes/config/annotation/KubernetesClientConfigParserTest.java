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

package org.citrusframework.kubernetes.config.annotation;

import java.util.Map;

import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.citrusframework.base.annotations.CitrusAnnotations.injectEndpoints;
import static org.citrusframework.config.annotation.AnnotationConfigParser.lookup;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class KubernetesClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "k8sClient1")
    @KubernetesClientConfig()
    private KubernetesClient client1;

    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version = "v1",
            username = "user",
            password = "s!cr!t",
            namespace = "user_namespace",
            messageConverter = "messageConverter",
            jsonMapper = "jsonMapper")
    private KubernetesClient client2;

    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version = "v1",
            oauthToken = "xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51",
            namespace = "user_namespace",
            messageConverter = "messageConverter",
            jsonMapper = "jsonMapper")
    private KubernetesClient client3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KubernetesMessageConverter messageConverter;
    @Mock
    private JsonMapper jsonMapper;

    @BeforeEach
    public void setup() {
        
        lenient().when(referenceResolver.resolve("messageConverter", KubernetesMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("jsonMapper", JsonMapper.class)).thenReturn(jsonMapper);
    }

    @BeforeEach
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
        assertEquals("http://localhost:8443/", client2.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        assertEquals("v1", client2.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion());
        assertEquals("user", client2.getEndpointConfiguration().getKubernetesClientConfig().getUsername());
        assertEquals("s!cr!t", client2.getEndpointConfiguration().getKubernetesClientConfig().getPassword());
        assertEquals("user_namespace", client2.getEndpointConfiguration().getKubernetesClientConfig().getNamespace());
        assertEquals(messageConverter, client2.getEndpointConfiguration().getMessageConverter());
        assertEquals(jsonMapper, client2.getEndpointConfiguration().getJsonMapper());

        // 3rd client
        assertNotNull(client3.getClient());
        assertEquals("http://localhost:8443/", client3.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl());
        assertEquals("v1", client3.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion());
        assertEquals("xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51", client3.getEndpointConfiguration().getKubernetesClientConfig().getOauthToken());
        assertEquals("user_namespace", client3.getEndpointConfiguration().getKubernetesClientConfig().getNamespace());
        assertEquals(messageConverter, client3.getEndpointConfiguration().getMessageConverter());
        assertEquals(jsonMapper, client3.getEndpointConfiguration().getJsonMapper());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = lookup();
        assertEquals(5L, validators.size());
        assertNotNull(validators.get("direct.async"));
        assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        assertNotNull(validators.get("direct.sync"));
        assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        assertNotNull(validators.get("http.client"));
        assertEquals(HttpClientConfigParser.class, validators.get("http.client").getClass());
        assertNotNull(validators.get("http.server"));
        assertEquals(HttpServerConfigParser.class, validators.get("http.server").getClass());
        assertNotNull(validators.get("k8s.client"));
        assertEquals(KubernetesClientConfigParser.class, validators.get("k8s.client").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(lookup("k8s.client").isPresent());
    }
}
