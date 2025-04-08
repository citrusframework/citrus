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

package org.citrusframework.agent.plugin;

import java.io.File;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.citrusframework.agent.plugin.config.ReportConfiguration;
import org.citrusframework.agent.plugin.config.ServerConfiguration;

public abstract class AbstractAgentMojo extends AbstractMojo {

    @Parameter(property = "citrus.agent.skip", defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue= "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * Http connect timeout.
     */
    @Parameter(property = "citrus.agent.timeout", defaultValue = "60000")
    private int timeout = 60000;

    /**
     * The output directory of the assembled distribution file.
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File outputDirectory;

    /**
     * Remote server configuration.
     */
    @Parameter
    private ServerConfiguration server;

    /**
     * Report configuration such as output directory and file names.
     */
    @Parameter
    private ReportConfiguration report;

    /** Http client */
    private final CloseableHttpClient httpClient;

    /**
     * Constructor using default client.
     */
    protected AbstractAgentMojo() {
        Timeout timoutMillis = Timeout.ofMilliseconds(timeout);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(timoutMillis)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(timoutMillis)
                                .setResponseTimeout(timoutMillis)
                                .build())
                .build();
    }

    /**
     * Constructor using given client.
     * @param httpClient
     */
    protected AbstractAgentMojo(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Citrus agent is skipped.");
            return;
        }

        doExecute();
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
        if (server == null) {
            server = new ServerConfiguration();
        }

        return server;
    }

    /**
     * Sets the report.
     *
     * @param report
     */
    public void setReport(ReportConfiguration report) {
        this.report = report;
    }

    /**
     * Gets the report.
     *
     * @return
     */
    public ReportConfiguration getReport() {
        if (report == null) {
            report = new ReportConfiguration();
        }

        return report;
    }

    /**
     * Gets the outputDirectory.
     *
     * @return
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Gets the httpClient.
     *
     * @return
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
}
