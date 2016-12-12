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

package com.consol.citrus.kubernetes.config.annotation;

import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
            certFile="/path/to/some/cert/ca.pem")
    private KubernetesClient client2;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
        referenceResolver.setApplicationContext(applicationContext);
    }

    @Test
    public void testKubernetesClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st client
        Assert.assertNotNull(client1.getEndpointConfiguration().getKubernetesClient());

        // 2nd client
        Assert.assertNotNull(client2.getEndpointConfiguration().getKubernetesClient());
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getMasterUrl().toString(), "http://localhost:8443/");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getApiVersion(), "v1");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getUsername(), "user");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getPassword(), "s!cr!t");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getNamespace(), "user_namespace");
        Assert.assertEquals(client2.getEndpointConfiguration().getKubernetesClientConfig().getCaCertFile(), "/path/to/some/cert/ca.pem");
    }
}
