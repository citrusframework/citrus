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

package org.citrusframework.server;

import org.citrusframework.endpoint.Endpoint;

/**
 * Server representation in Citrus is a runnable instance accepting client
 * connections.
 *
 * @since 2007
 *
 */
public interface Server extends Endpoint, Runnable {

    /**
     * Start the server
     */
    void start();

    /**
     * Stop the server.
     */
    void stop();

    /**
     * Is server running.
     * @return
     */
    boolean isRunning();
}
