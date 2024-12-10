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

package org.citrusframework.testcontainers;

import java.net.URL;
import java.time.Duration;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

/**
 * Utility class helps to create proper Testcontainers wait strategies from different input.
 */
public final class WaitStrategyHelper {

    private WaitStrategyHelper() {
        // prevent instantiation of utility class
    }

    public static WaitStrategy getNoopStrategy() {
        return new WaitStrategy() {
            @Override
            public void waitUntilReady(WaitStrategyTarget waitStrategyTarget) {
            }

            @Override
            public WaitStrategy withStartupTimeout(Duration startupTimeout) {
                return this;
            }
        };
    }

    public static WaitStrategy waitFor(URL url) {
        if ("https".equals(url.getProtocol())) {
            return Wait.forHttps(url.getPath());
        } else {
            return Wait.forHttp(url.getPath());
        }
    }

    public static WaitStrategy waitFor(String logMessage) {
        return waitFor(logMessage, 1);
    }

    public static WaitStrategy waitFor(String logMessage, int times) {
        return Wait.forLogMessage(logMessage, times);
    }
}
