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

package org.citrusframework.dsl.endpoint;

import org.citrusframework.dsl.endpoint.camel.CamelEndpointCatalog;
import org.citrusframework.dsl.endpoint.channel.MessageChannelEndpointCatalog;
import org.citrusframework.dsl.endpoint.docker.DockerEndpointCatalog;
import org.citrusframework.dsl.endpoint.ftp.FtpEndpointCatalog;
import org.citrusframework.dsl.endpoint.ftp.ScpEndpointCatalog;
import org.citrusframework.dsl.endpoint.ftp.SftpEndpointCatalog;
import org.citrusframework.dsl.endpoint.http.HttpEndpointCatalog;
import org.citrusframework.dsl.endpoint.jms.JmsEndpointCatalog;
import org.citrusframework.dsl.endpoint.jmx.JmxEndpointCatalog;
import org.citrusframework.dsl.endpoint.kafka.KafkaEndpointCatalog;
import org.citrusframework.dsl.endpoint.kubernetes.KubernetesEndpointCatalog;
import org.citrusframework.dsl.endpoint.mail.MailEndpointCatalog;
import org.citrusframework.dsl.endpoint.rmi.RmiEndpointCatalog;
import org.citrusframework.dsl.endpoint.selenium.SeleniumEndpointCatalog;
import org.citrusframework.dsl.endpoint.ssh.SshEndpointCatalog;
import org.citrusframework.dsl.endpoint.vertx.VertxEndpointCatalog;
import org.citrusframework.dsl.endpoint.websocket.WebSocketEndpointCatalog;
import org.citrusframework.dsl.endpoint.ws.WebServiceEndpointCatalog;
import org.citrusframework.endpoint.direct.DirectEndpoints;

/**
 * @author Christoph Deppisch
 * @since 3.0
 */
public abstract class CitrusEndpoints {

    /**
     * Prevent public instantiation.
     */
    protected CitrusEndpoints() {
        super();
    }

    /**
     * Creates new DirectEndpoint sync or async builder.
     * @return
     */
    public static DirectEndpoints direct() {
        return DirectEndpoints.direct();
    }

    /**
     * Creates new ChannelEndpoint sync or async builder.
     * @return
     */
    public static MessageChannelEndpointCatalog channel() {
        return MessageChannelEndpointCatalog.channel();
    }

    /**
     * Creates new JmsEndpoint sync or async builder.
     * @return
     */
    public static JmsEndpointCatalog jms() {
        return JmsEndpointCatalog.jms();
    }

    /**
     * Creates new HttpClient or HttpServer builder.
     * @return
     */
    public static HttpEndpointCatalog http() {
        return HttpEndpointCatalog.http();
    }

    /**
     * Creates new WebServiceClient or WebServiceServer builder.
     * @return
     */
    public static WebServiceEndpointCatalog soap() {
        return WebServiceEndpointCatalog.soap();
    }

    /**
     * Creates new JmxClient or JmxServer builder.
     * @return
     */
    public static JmxEndpointCatalog jmx() {
        return JmxEndpointCatalog.jmx();
    }

    /**
     * Creates new RmiClient or RmiServer builder.
     * @return
     */
    public static RmiEndpointCatalog rmi() {
        return RmiEndpointCatalog.rmi();
    }

    /**
     * Creates new MailClient or MailServer builder.
     * @return
     */
    public static MailEndpointCatalog mail() {
        return MailEndpointCatalog.mail();
    }

    /**
     * Creates new FtpClient or FtpServer builder.
     * @return
     */
    public static FtpEndpointCatalog ftp() {
        return FtpEndpointCatalog.ftp();
    }

    /**
     * Creates new SftpClient or SftpServer builder.
     * @return
     */
    public static SftpEndpointCatalog sftp() {
        return SftpEndpointCatalog.sftp();
    }

    /**
     * Creates new ScpClient or SftpServer builder.
     * @return
     */
    public static ScpEndpointCatalog scp() {
        return ScpEndpointCatalog.scp();
    }

    /**
     * Creates new SshClient or SshServer builder.
     * @return
     */
    public static SshEndpointCatalog ssh() {
        return SshEndpointCatalog.ssh();
    }

    /**
     * Creates new VertxEndpoint sync or async builder.
     * @return
     */
    public static VertxEndpointCatalog vertx() {
        return VertxEndpointCatalog.vertx();
    }

    /**
     * Creates new WebSocketClient or WebSocketServer builder.
     * @return
     */
    public static WebSocketEndpointCatalog websocket() {
        return WebSocketEndpointCatalog.websocket();
    }

    /**
     * Creates new DockerClient builder.
     * @return
     */
    public static DockerEndpointCatalog docker() {
        return DockerEndpointCatalog.docker();
    }

    /**
     * Creates new KubernetesClient builder.
     * @return
     */
    public static KubernetesEndpointCatalog kubernetes() {
        return KubernetesEndpointCatalog.kubernetes();
    }

    /**
     * Creates new SeleniumBrowser builder.
     * @return
     */
    public static SeleniumEndpointCatalog selenium() {
        return SeleniumEndpointCatalog.selenium();
    }

    /**
     * Creates new KafkaEndpoint endpoint builder.
     * @return
     */
    public static KafkaEndpointCatalog kafka() {
        return KafkaEndpointCatalog.kafka();
    }

    /**
     * Creates new CamelEndpoint endpoint builder.
     * @return
     */
    public static CamelEndpointCatalog camel() {
        return CamelEndpointCatalog.camel();
    }

}
