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

package org.citrusframework.agent.plugin.config;

import java.io.Serializable;
import java.util.Optional;

import org.apache.maven.plugins.annotations.Parameter;
import org.citrusframework.util.StringUtils;

public class ServerConfiguration implements Serializable {

    public static final String AGENT_SERVER_PORT_DEFAULT = "4567";

    @Parameter(property = "citrus.agent.server.url")
    private String url;

    @Parameter(property = "citrus.agent.server.name", defaultValue = "citrus-agent")
    private String name;

    @Parameter(property = "citrus.agent.server.local.port")
    private String localPort;

    @Parameter(property = "citrus.agent.server.auto.connect", defaultValue = "true")
    protected boolean autoConnect;

    @Parameter(property = "citrus.agent.server.connect.timeout", defaultValue = "10000")
    protected long connectTimeout;

    public ServerConfiguration() {
        name = "citrus-agent";
        autoConnect = true;
        connectTimeout = 10000L;
    }

    public String getUrl() {
        if (!StringUtils.hasText(url)) {
            url = "http://localhost:" + Optional.ofNullable(localPort).orElse(AGENT_SERVER_PORT_DEFAULT);
        }

        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
