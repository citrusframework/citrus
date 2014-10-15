/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.ftp.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.AbstractEndpointComponent;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpEndpointComponent extends AbstractEndpointComponent {

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        FtpClient ftpClient = new FtpClient();

        ftpClient.getEndpointConfiguration().setHost(getHost(resourcePath));
        ftpClient.getEndpointConfiguration().setPort(getPort(resourcePath, ftpClient.getEndpointConfiguration()));

        enrichEndpointConfiguration(ftpClient.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, FtpEndpointConfiguration.class), context);

        try {
            ftpClient.afterPropertiesSet();
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create dynamic endpoint", e);
        }

        return ftpClient;
    }

    /**
     * Extract port number from resource path. If not present use default port from endpoint configuration.
     * @param resourcePath
     * @param endpointConfiguration
     * @return
     */
    private Integer getPort(String resourcePath, FtpEndpointConfiguration endpointConfiguration) {
        if (resourcePath.contains(":")) {
            return Integer.valueOf(resourcePath.split(":")[1]);
        }

        return endpointConfiguration.getPort();
    }

    /**
     * Extract host name from resource path.
     * @param resourcePath
     * @return
     */
    private String getHost(String resourcePath) {
        if (resourcePath.contains(":")) {
            return resourcePath.split(":")[0];
        }

        return resourcePath;
    }
}
