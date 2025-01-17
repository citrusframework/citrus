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

    public KubernetesActor() {
        this(null);
    }

    public KubernetesActor(KubernetesClient kubernetesClient) {
        super("k8s");

        synchronized (logger) {
            if (connected == null) {
                if (kubernetesClient != null) {
                    connected = new AtomicBoolean(verifyConnected(kubernetesClient));
                } else {
                    try (KubernetesClient tempClient = new KubernetesClientBuilder().build()) {
                        connected = new AtomicBoolean(verifyConnected(tempClient));
                    }
                }
            }
        }
    }

    @Override
    public boolean isDisabled() {
        if (!KubernetesSettings.isEnabled()) {
            return true;
        }

        return !connected.get() || super.isDisabled();
    }

    public static boolean verifyConnected(KubernetesClient kubernetesClient) {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
                var pods = kubernetesClient.pods();
                if (pods == null) {
                    logger.warn("Skipping Kubernetes actions as no proper Kubernetes environment is available on host system!");
                    return false;
                }
                pods.list();
                return true;
            });

            return future.get(KubernetesSettings.getConnectTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn(("Skipping Kubernetes actions as no proper Kubernetes environment is available on host system! " +
                    "Caused by: %s - %s").formatted(e.getClass(), e.getMessage()));
            return false;
        }
    }

    public static void resetConnectionState() {
        synchronized (logger) {
            connected = null;
        }
    }
}
