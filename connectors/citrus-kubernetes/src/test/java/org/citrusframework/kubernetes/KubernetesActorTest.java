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

package org.citrusframework.kubernetes;

import java.util.HashMap;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.MockWebServer;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class KubernetesActorTest {

    private final KubernetesMockServer k8sServer = new KubernetesMockServer(new Context(), new MockWebServer(),
            new HashMap<>(), new KubernetesCrudDispatcher(), false);

    KubernetesClient k8sClient;

    @BeforeClass
    public void setupMocks() {
        k8sServer.init();
        k8sClient = k8sServer.createClient();
    }

    @AfterClass(alwaysRun = true)
    public void stop() {
        k8sServer.destroy();
    }

    public void shouldVerifyConnectedState() {
        try {
            KubernetesActor.resetConnectionState();
            Assert.assertFalse(new KubernetesActor(k8sClient).isDisabled());
        } finally {
            KubernetesActor.resetConnectionState();
        }

        try {
            KubernetesClient k8sClientMock = Mockito.mock(KubernetesClient.class);
            Assert.assertTrue(new KubernetesActor(k8sClientMock).isDisabled());
        } finally {
            KubernetesActor.resetConnectionState();
        }
    }

    public void shouldOverruleConnectedState() {
        boolean initial = KubernetesSettings.isEnabled();
        try {
            KubernetesActor.resetConnectionState();
            System.setProperty("citrus.kubernetes.enabled", "false");
            Assert.assertTrue(new KubernetesActor(k8sClient).isDisabled());
        } finally {
            System.setProperty("citrus.kubernetes.enabled", Boolean.toString(initial));
            KubernetesActor.resetConnectionState();
        }

        initial = Boolean.parseBoolean(System.getProperty("citrus.test.actor.k8s.enabled", "true"));
        try {
            System.setProperty("citrus.test.actor.k8s.enabled", "false");
            Assert.assertTrue(new KubernetesActor(k8sClient).isDisabled());
        } finally {
            System.setProperty("citrus.test.actor.k8s.enabled", Boolean.toString(initial));
            KubernetesActor.resetConnectionState();
        }
    }
}
