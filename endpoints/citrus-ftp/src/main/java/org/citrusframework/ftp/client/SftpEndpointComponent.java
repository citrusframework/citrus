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

package org.citrusframework.ftp.client;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpointComponent;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpEndpointComponent extends AbstractEndpointComponent {

    /**
     * Default constructor using the name for this component.
     */
    public SftpEndpointComponent() {
        super("sftp");
    }

    @Override
    protected Endpoint createEndpoint(String resourcePath, Map<String, String> parameters, TestContext context) {
        SftpClient ftpClient = new SftpClient();

        ftpClient.getEndpointConfiguration().setHost(getHost(resourcePath));
        ftpClient.getEndpointConfiguration().setPort(getPort(resourcePath, ftpClient.getEndpointConfiguration()));

        enrichEndpointConfiguration(ftpClient.getEndpointConfiguration(),
                getEndpointConfigurationParameters(parameters, FtpEndpointConfiguration.class), context);

        try {
            ftpClient.initialize();
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
