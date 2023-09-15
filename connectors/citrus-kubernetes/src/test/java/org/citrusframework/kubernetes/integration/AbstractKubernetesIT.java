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

package org.citrusframework.kubernetes.integration;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class AbstractKubernetesIT extends TestNGCitrusSpringSupport {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AbstractKubernetesIT.class);

    /** Kubernetes' connection state, checks connectivity only once per test run */
    private static boolean connected = false;

    @BeforeSuite(alwaysRun = true)
    public void checkKubernetesEnvironment() {
        boolean enabled = Boolean.parseBoolean(System.getProperty("citrus.kuberenetes.it.enabled", Boolean.FALSE.toString()));
        if (enabled) {
            try {
                Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
                    KubernetesClient kubernetesClient = new DefaultKubernetesClient();
                    kubernetesClient.pods().list();
                    return true;
                });

                connected = future.get(5000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warn("Skipping Kubernetes test execution as no proper Kubernetes environment is available on host system!", e);
            }
        }
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (connected) {
            super.run(callBack, testResult);
        } else {
            testResult.setStatus(ITestResult.SKIP);
        }
    }
}
