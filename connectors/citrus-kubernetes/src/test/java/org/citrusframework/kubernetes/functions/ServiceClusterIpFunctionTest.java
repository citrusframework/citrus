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
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
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

public class ServiceClusterIpFunctionTest extends UnitTestSupport {

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

    @Test
    public void shouldResolveServiceClusterIp() {
        Service service = new Service();
        service.setMetadata(new ObjectMeta());
        service.getMetadata().setNamespace(KubernetesSupport.getNamespace(context));
        service.getMetadata().setName("myService");
        service.setSpec(new ServiceSpec());
        service.getSpec().setClusterIP("127.0.0.1");

        context.getReferenceResolver().bind("k8sClient", k8sClient);

        reset(k8sClient, clientOperation, serviceResource);
        when(k8sClient.services()).thenReturn(clientOperation);
        when(clientOperation.inNamespace(anyString())).thenReturn(clientOperation);
        when(clientOperation.withName("myService")).thenReturn(serviceResource);
        when(serviceResource.get()).thenReturn(service);

        Assert.assertEquals(new ServiceClusterIpFunction().execute(List.of("myService"), context), "127.0.0.1");
    }

    @Test
    public void shouldFallbackToExternalIPs() {
        Service service = new Service();
        service.setMetadata(new ObjectMeta());
        service.getMetadata().setNamespace(KubernetesSupport.getNamespace(context));
        service.getMetadata().setName("myExternalService");
        service.setSpec(new ServiceSpec());
        service.getSpec().setExternalIPs(List.of("127.0.0.1"));

        context.getReferenceResolver().bind("k8sClient", k8sClient);

        reset(k8sClient, clientOperation, serviceResource);
        when(k8sClient.services()).thenReturn(clientOperation);
        when(clientOperation.inNamespace(anyString())).thenReturn(clientOperation);
        when(clientOperation.withName("myExternalService")).thenReturn(serviceResource);
        when(serviceResource.get()).thenReturn(service);

        Assert.assertEquals(new ServiceClusterIpFunction().execute(List.of("myExternalService"), context), "127.0.0.1");
    }

    @Test
    public void shouldFailOnUnknownService() {
        context.getReferenceResolver().bind("k8sClient", k8sClient);

        reset(k8sClient, clientOperation, serviceResource);
        when(k8sClient.services()).thenReturn(clientOperation);
        when(clientOperation.inNamespace(anyString())).thenReturn(clientOperation);
        when(clientOperation.withName(anyString())).thenReturn(serviceResource);

        Assert.assertThrows(CitrusRuntimeException.class,
                () -> new ServiceClusterIpFunction().execute(List.of("unknown"), context));
    }

    @Test
    public void shouldFailOnNoServiceClusterIpAvailable() {
        Service service = new Service();
        service.setMetadata(new ObjectMeta());
        service.getMetadata().setNamespace(KubernetesSupport.getNamespace(context));
        service.getMetadata().setName("no-cluster-ip-service");
        service.setSpec(new ServiceSpec());

        context.getReferenceResolver().bind("k8sClient", k8sClient);

        reset(k8sClient, clientOperation, serviceResource);
        when(k8sClient.services()).thenReturn(clientOperation);
        when(clientOperation.inNamespace(anyString())).thenReturn(clientOperation);
        when(clientOperation.withName("no-cluster-ip-service")).thenReturn(serviceResource);
        when(serviceResource.get()).thenReturn(service);

        Assert.assertThrows(CitrusRuntimeException.class,
                () -> new ServiceClusterIpFunction().execute(List.of("no-cluster-ip-service"), context));
    }

    @Test
    public void shouldResolve() {
        Assert.assertNotNull(new DefaultFunctionLibrary().getFunction("serviceClusterIp"));
    }
}
