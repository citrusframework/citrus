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

package org.citrusframework.rmi.endpoint;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class RmiEndpointUtils {

    /**
     * Prevent instantiation.
     */
    private RmiEndpointUtils() {
        super();
    }

    /**
     * Extract service binding information from endpoint resource path. This is usualle the path after the port specification.
     * @param resourcePath
     * @return
     */
    public static String getBinding(String resourcePath) {
        if (resourcePath.contains("/")) {
            return resourcePath.substring(resourcePath.indexOf('/') + 1);
        }

        return null;
    }

    /**
     * Extract port number from resource path. If not present use default port from endpoint configuration.
     * @param resourcePath
     * @param endpointConfiguration
     * @return
     */
    public static Integer getPort(String resourcePath, RmiEndpointConfiguration endpointConfiguration) {
        if (resourcePath.contains(":")) {
            String portSpec = resourcePath.split(":")[1];

            if (portSpec.contains("/")) {
                portSpec = portSpec.substring(0, portSpec.indexOf('/'));
            }

            return Integer.valueOf(portSpec);
        }

        return endpointConfiguration.getPort();
    }

    /**
     * Extract host name from resource path.
     * @param resourcePath
     * @return
     */
    public static String getHost(String resourcePath) {
        String hostSpec;
        if (resourcePath.contains(":")) {
            hostSpec = resourcePath.split(":")[0];
        } else {
            hostSpec = resourcePath;
        }

        if (hostSpec.contains("/")) {
            hostSpec = hostSpec.substring(0, hostSpec.indexOf('/'));
        }

        return hostSpec;
    }
}
