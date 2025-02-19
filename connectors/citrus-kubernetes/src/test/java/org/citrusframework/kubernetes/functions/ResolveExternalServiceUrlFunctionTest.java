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

package org.citrusframework.kubernetes.functions;

import java.util.List;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.ServiceStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.kubernetes.UnitTestSupport;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ResolveExternalServiceUrlFunctionTest extends UnitTestSupport {

    @Mock
    private KubernetesClient k8sClient;
    @Mock
    private ServiceResource<Service> serviceResource;
    @Mock
    private MixedOperation<Service, ServiceList, ServiceResource<Service>> clientOperation;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private final ResolveExternalServiceUrlFunction function = new ResolveExternalServiceUrlFunction();

    @Test
    public void shouldResolveExternalServiceUrl() {
        Service service = new Service();
        service.setMetadata(new ObjectMeta());
        service.getMetadata().setNamespace(KubernetesSupport.getNamespace(context));
        service.getMetadata().setName("myService");
        service.setSpec(new ServiceSpec());
        service.getSpec().setClusterIP("127.0.0.1");
        service.getSpec().setExternalIPs(List.of("10.0.0.41"));
        service.getSpec().getPorts().add(new ServicePortBuilder().withPort(80).withNodePort(4567).build());

        service.setStatus(new ServiceStatus());
        context.getReferenceResolver().bind("k8sClient", k8sClient);

        reset(k8sClient, clientOperation, serviceResource);
        when(k8sClient.services()).thenReturn(clientOperation);
        when(clientOperation.inNamespace(anyString())).thenReturn(clientOperation);
        when(clientOperation.withName("myService")).thenReturn(serviceResource);
        when(serviceResource.get()).thenReturn(service);

        Assert.assertEquals(function.execute(List.of("myService"), context), "http://10.0.0.41:4567");
    }

    @Test
    public void shouldResolve() {
        Assert.assertNotNull(new DefaultFunctionLibrary().getFunction("resolveExternalServiceUrl"));
    }
}
