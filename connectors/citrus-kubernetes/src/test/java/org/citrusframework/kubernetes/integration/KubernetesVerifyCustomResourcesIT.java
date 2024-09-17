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

import java.util.Collections;

import io.fabric8.kubernetes.api.model.ConditionBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesVerifyCustomResourcesIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldVerifyCustomResources() {
        given(doFinally().actions(context -> k8sClient.getClient().resources(Foo.class)
                .inNamespace(namespace)
                .withName("my-foo")
                .delete()));

        given(context -> {
            Foo foo = new Foo();
            foo.setSpec(new Foo.FooSpec());
            foo.getSpec().setMessage("Hello");
            foo.getMetadata().setName("my-foo");
            foo.getMetadata().setNamespace(namespace);
            foo.setStatus(new Foo.FooStatus());
            foo.getStatus().setConditions(Collections.singletonList(
                    new ConditionBuilder()
                            .withType("Ready")
                            .withStatus("true")
                            .build()));

            k8sClient.getClient().resources(Foo.class)
                    .inNamespace(namespace)
                    .resource(foo)
                    .create();
        });

        then(kubernetes()
                .client(k8sClient.getClient())
                .customResources()
                .verify("my-foo")
                .type(Foo.class)
                .inNamespace(namespace)
                .isReady()
                .maxAttempts(2));
    }

    @Test
    @CitrusTest
    public void shouldVerifyCustomResourcesByLabel() {
        given(doFinally().actions(context -> k8sClient.getClient().resources(Foo.class)
                .inNamespace(namespace)
                .withName("my-foo")
                .delete()));

        given(context -> {
            Foo foo = new Foo();
            foo.setSpec(new Foo.FooSpec());
            foo.getSpec().setMessage("Hello");
            foo.getMetadata().setName("my-foo");
            foo.getMetadata().setNamespace(namespace);
            foo.getMetadata().getLabels().put("test", "citrus");
            foo.setStatus(new Foo.FooStatus());
            foo.getStatus().setConditions(Collections.singletonList(
                    new ConditionBuilder()
                            .withType("Ready")
                            .withStatus("true")
                            .build()));

            k8sClient.getClient().resources(Foo.class)
                    .inNamespace(namespace)
                    .resource(foo)
                    .create();
        });

        then(kubernetes()
                .client(k8sClient.getClient())
                .customResources()
                .verify("test", "citrus")
                .type(Foo.class)
                .inNamespace(namespace)
                .isReady()
                .maxAttempts(2));
    }

    @Test
    @CitrusTest
    public void shouldFailValidationForCustomResourceStatus() {
        given(doFinally().actions(context -> k8sClient.getClient().resources(Foo.class)
                .inNamespace(namespace)
                .withName("my-foo")
                .delete()));

        given(context -> {
            Foo foo = new Foo();
            foo.setSpec(new Foo.FooSpec());
            foo.getSpec().setMessage("Hello");
            foo.getMetadata().setName("my-foo");
            foo.getMetadata().setNamespace(namespace);
            foo.setStatus(new Foo.FooStatus());
            foo.getStatus().setConditions(Collections.singletonList(
                    new ConditionBuilder()
                            .withType("Ready")
                            .withStatus("false")
                            .build()));

            k8sClient.getClient().resources(Foo.class)
                    .inNamespace(namespace)
                    .resource(foo)
                    .create();
        });

        then(assertException()
                .exception(ActionTimeoutException.class)
                .when(kubernetes()
                    .client(k8sClient.getClient())
                    .customResources()
                    .verify("my-foo")
                    .type(Foo.class)
                    .inNamespace(namespace)
                    .isReady()
                    .maxAttempts(2)));
    }

}
