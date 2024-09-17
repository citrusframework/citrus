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

package org.citrusframework.kubernetes.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesDeleteCustomResourcesIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldDeleteCustomResource() {
        given(context -> {
            Foo foo = new Foo();
            foo.setSpec(new Foo.FooSpec());
            foo.getSpec().setMessage("Hello");
            foo.getMetadata().setName("my-foo");
            foo.getMetadata().setNamespace(namespace);

            k8sClient.getClient().resources(Foo.class)
                    .inNamespace(namespace)
                    .resource(foo)
                    .create();
        });

        when(kubernetes()
                .client(k8sClient.getClient())
                .customResources()
                .delete("my-foo")
                .type(Foo.class)
                .inNamespace(namespace));

        then(context -> {
            Foo foo = k8sClient.getClient().resources(Foo.class)
                    .inNamespace(namespace)
                    .withName("my-foo")
                    .get();

            Assert.assertNull(foo);
        });
    }

}
