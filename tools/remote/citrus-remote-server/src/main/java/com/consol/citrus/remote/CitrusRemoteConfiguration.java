/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.remote;

import com.consol.citrus.main.CitrusAppConfiguration;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class CitrusRemoteConfiguration extends CitrusAppConfiguration {

    /** Server port */
    private int port = 4567;

    /**
     * Gets the port.
     *
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Applies configuration with settable properties at runtime.
     * @param configuration
     */
    public void apply(CitrusAppConfiguration configuration) {
        setPackages(configuration.getPackages());
        setTestClasses(configuration.getTestClasses());
        setIncludes(configuration.getIncludes());
        addDefaultProperties(configuration.getDefaultProperties());
    }
}
