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

package org.citrusframework.testcontainers.integration;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.okhttp.OkDockerHttpClient;
import org.citrusframework.testcontainers.TestcontainersActor;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testcontainers.shaded.com.github.dockerjava.core.DefaultDockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientConfig;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerClientImpl;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

/**
 * Makes sure tests are executed only on hosts that meet prerequisites such as a Docker compatible engine installed and
 * clients able to connect.
 */
public class AbstractTestcontainersIT extends TestNGCitrusSpringSupport {

    /** Docker connection state, checks connectivity only once per test run */
    private static boolean disabled = false;

    @BeforeSuite(alwaysRun = true)
    public void checkDockerEnvironment() {
        disabled = new TestcontainersActor().isDisabled();
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (disabled) {
            testResult.setStatus(ITestResult.SKIP);
        } else {
            super.run(callBack, testResult);
        }
    }

    protected DockerClient createDockerClient() {
        DockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        return DockerClientImpl.getInstance(clientConfig,
                new OkDockerHttpClient.Builder().dockerHost(clientConfig.getDockerHost()).build()
        );
    }
}
