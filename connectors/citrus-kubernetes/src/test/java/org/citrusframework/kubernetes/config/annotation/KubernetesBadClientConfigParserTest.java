/*
 * Copyright 2024 the original author or authors.
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
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.annotations.CitrusAnnotations.injectEndpoints;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * @author Valentin Soldo
 */
public class KubernetesBadClientConfigParserTest extends AbstractTestNGUnitTest {

    // Endpoint badly configured with missing password
    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version = "v1",
            username = "user",
            namespace = "user_namespace",
            messageConverter = "messageConverter",
            objectMapper = "objectMapper")
    private KubernetesClient client1;

    // Badly configured with oauthToken and username
    @CitrusEndpoint
    @KubernetesClientConfig(url = "http://localhost:8443",
            version="v1",
            username="user",
            password="s!cr!t",
            oauthToken = "xx508xx63817x752xx74004x30705xx92x58349x5x78f5xx34xxxxx51",
            namespace="user_namespace",
            messageConverter="messageConverter",
            objectMapper="objectMapper")
    private KubernetesClient client2;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private KubernetesMessageConverter messageConverter;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", KubernetesMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("objectMapper", ObjectMapper.class)).thenReturn(objectMapper);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testBadConfiguredKubernetesClient() {
        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () -> injectEndpoints(this, context));
        assertEquals(exception.getMessage(), "Parameters not set correctly - check if either an oauthToke or password and username is set");
    }
}
