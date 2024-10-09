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

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.citrusframework.TestActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test actor disabled when running a local test where no Kubernetes and Openshift is involved.
 */
public class KubernetesActor extends TestActor {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(KubernetesActor.class);

    /** Kubernetes' connection state, checks connectivity to Kubernetes cluster */
    private static AtomicBoolean connected;

    private final KubernetesClient kubernetesClient;

    public KubernetesActor(KubernetesClient kubernetesClient) {
        setName("k8s");

        if (kubernetesClient != null) {
            this.kubernetesClient = kubernetesClient;
        } else {
            this.kubernetesClient = new KubernetesClientBuilder().build();
        }
    }

    @Override
    public boolean isDisabled() {
        synchronized (logger) {
            if (connected == null) {
                if (KubernetesSettings.isEnabled()) {
                    try {
                        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
                            kubernetesClient.pods().list();
                            return true;
                        });

                        connected = new AtomicBoolean((future.get(KubernetesSettings.getConnectTimeout(), TimeUnit.MILLISECONDS)));
                    } catch (Exception e) {
                        logger.warn("Skipping Kubernetes action as no proper Kubernetes environment is available on host system!", e);
                        connected = new AtomicBoolean(false);
                    }
                } else {
                    return false;
                }
            }

            return !connected.get();
        }
    }
}
