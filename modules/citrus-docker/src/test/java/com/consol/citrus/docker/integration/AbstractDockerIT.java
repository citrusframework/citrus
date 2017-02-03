/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.docker.integration;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeSuite;

import java.util.concurrent.*;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class AbstractDockerIT extends AbstractTestNGCitrusTest {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractDockerIT.class);

    /** Docker connection state, checks connectivity only once per test run */
    private static boolean connected = false;

    @BeforeSuite(alwaysRun = true)
    public void checkDockerEnvironment() {
        try {
            Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
                DockerClient dockerClient = DockerClientImpl.getInstance()
                        .withDockerCmdExecFactory(new JerseyDockerCmdExecFactory());

                dockerClient.pingCmd().exec();
                return true;
            });

            future.get(5000, TimeUnit.MILLISECONDS);
            connected = true;
        } catch (Exception e) {
            log.warn("Skipping Docker test execution as no proper Docker environment is available on host system!", e);
        }
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (connected) {
            super.run(callBack, testResult);
        }
    }
}
