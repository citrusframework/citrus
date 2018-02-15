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

package com.consol.citrus.remote.plugin;

import com.consol.citrus.remote.plugin.config.ServerConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractCitrusRemoteMojo extends AbstractMojo {

    @Parameter(property = "citrus.remote.plugin.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Http connect timeout.
     */
    @Parameter(property = "citrus.remote.plugin.timeout", defaultValue = "60000")
    private int timeout;

    /**
     * Remote server configuration.
     */
    @Parameter
    private ServerConfiguration server;

    /** Http client */
    private final HttpClient httpClient;

    /**
     * Constructor using default client.
     */
    public AbstractCitrusRemoteMojo() {
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT)
                        .setConnectionRequestTimeout(timeout)
                        .setConnectTimeout(timeout)
                        .setSocketTimeout(timeout)
                        .build())
                .build();
    }

    /**
     * Constructor using given client.
     * @param httpClient
     */
    public AbstractCitrusRemoteMojo(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            doExecute();
        }
    }

    /**
     * Subclass execution logic.
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * Sets the server.
     *
     * @param server
     */
    public void setServer(ServerConfiguration server) {
        this.server = server;
    }

    /**
     * Gets the server configuration.
     * @return
     */
    public ServerConfiguration getServer() {
        return server;
    }

    /**
     * Gets the httpClient.
     *
     * @return
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }
}
