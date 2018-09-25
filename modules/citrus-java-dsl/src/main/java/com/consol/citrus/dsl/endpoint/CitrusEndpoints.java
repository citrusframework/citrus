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

package com.consol.citrus.dsl.endpoint;

import com.consol.citrus.channel.ChannelEndpointBuilder;
import com.consol.citrus.channel.ChannelSyncEndpointBuilder;
import com.consol.citrus.docker.client.DockerClientBuilder;
import com.consol.citrus.dsl.endpoint.jdbc.JdbcDbServerEndpointBuilder;
import com.consol.citrus.dsl.endpoint.selenium.SeleniumBrowserEndpointBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointBuilder;
import com.consol.citrus.ftp.client.*;
import com.consol.citrus.ftp.server.FtpServerBuilder;
import com.consol.citrus.ftp.server.SftpServerBuilder;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.jms.endpoint.JmsEndpointBuilder;
import com.consol.citrus.jms.endpoint.JmsSyncEndpointBuilder;
import com.consol.citrus.jmx.client.JmxClientBuilder;
import com.consol.citrus.jmx.server.JmxServerBuilder;
import com.consol.citrus.kafka.endpoint.KafkaEndpointBuilder;
import com.consol.citrus.kubernetes.client.KubernetesClientBuilder;
import com.consol.citrus.mail.client.MailClientBuilder;
import com.consol.citrus.mail.server.MailServerBuilder;
import com.consol.citrus.rmi.client.RmiClientBuilder;
import com.consol.citrus.rmi.server.RmiServerBuilder;
import com.consol.citrus.ssh.client.SshClientBuilder;
import com.consol.citrus.ssh.server.SshServerBuilder;
import com.consol.citrus.vertx.endpoint.VertxEndpointBuilder;
import com.consol.citrus.vertx.endpoint.VertxSyncEndpointBuilder;
import com.consol.citrus.websocket.client.WebSocketClientBuilder;
import com.consol.citrus.websocket.server.WebSocketServerBuilder;
import com.consol.citrus.ws.client.WebServiceClientBuilder;
import com.consol.citrus.ws.server.WebServiceServerBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class CitrusEndpoints {

    /**
     * Prevent public instantiation.
     */
    protected CitrusEndpoints() {
        super();
    }

    /**
     * Creates new ChannelEndpoint sync or async builder.
     * @return
     */
    public static AsyncSyncEndpointBuilder<ChannelEndpointBuilder, ChannelSyncEndpointBuilder> channel() {
        return new AsyncSyncEndpointBuilder<>(new ChannelEndpointBuilder(), new ChannelSyncEndpointBuilder());
    }

    /**
     * Creates new JmsEndpoint sync or async builder.
     * @return
     */
    public static AsyncSyncEndpointBuilder<JmsEndpointBuilder, JmsSyncEndpointBuilder> jms() {
        return new AsyncSyncEndpointBuilder<>(new JmsEndpointBuilder(), new JmsSyncEndpointBuilder());
    }

    /**
     * Creates new HttpClient or HttpServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<HttpClientBuilder, HttpServerBuilder> http() {
        return new ClientServerEndpointBuilder<>(new HttpClientBuilder(), new HttpServerBuilder());
    }

    /**
     * Creates new WebServiceClient or WebServiceServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<WebServiceClientBuilder, WebServiceServerBuilder> soap() {
        return new ClientServerEndpointBuilder<>(new WebServiceClientBuilder(), new WebServiceServerBuilder());
    }

    /**
     * Creates new JmxClient or JmxServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<JmxClientBuilder, JmxServerBuilder> jmx() {
        return new ClientServerEndpointBuilder<>(new JmxClientBuilder(), new JmxServerBuilder());
    }

    /**
     * Creates new RmiClient or RmiServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<RmiClientBuilder, RmiServerBuilder> rmi() {
        return new ClientServerEndpointBuilder<>(new RmiClientBuilder(), new RmiServerBuilder());
    }

    /**
     * Creates new MailClient or MailServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<MailClientBuilder, MailServerBuilder> mail() {
        return new ClientServerEndpointBuilder<>(new MailClientBuilder(), new MailServerBuilder());
    }

    /**
     * Creates new FtpClient or FtpServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<FtpClientBuilder, FtpServerBuilder> ftp() {
        return new ClientServerEndpointBuilder<>(new FtpClientBuilder(), new FtpServerBuilder());
    }

    /**
     * Creates new SftpClient or SftpServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<SftpClientBuilder, SftpServerBuilder> sftp() {
        return new ClientServerEndpointBuilder<>(new SftpClientBuilder(), new SftpServerBuilder());
    }

    /**
     * Creates new ScpClient or SftpServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<ScpClientBuilder, SftpServerBuilder> scp() {
        return new ClientServerEndpointBuilder<>(new ScpClientBuilder(), new SftpServerBuilder());
    }

    /**
     * Creates new SshClient or SshServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<SshClientBuilder, SshServerBuilder> ssh() {
        return new ClientServerEndpointBuilder<>(new SshClientBuilder(), new SshServerBuilder());
    }

    /**
     * Creates new VertxEndpoint sync or async builder.
     * @return
     */
    public static AsyncSyncEndpointBuilder<VertxEndpointBuilder, VertxSyncEndpointBuilder> vertx() {
        return new AsyncSyncEndpointBuilder<>(new VertxEndpointBuilder(), new VertxSyncEndpointBuilder());
    }

    /**
     * Creates new WebSocketClient or WebSocketServer builder.
     * @return
     */
    public static ClientServerEndpointBuilder<WebSocketClientBuilder, WebSocketServerBuilder> websocket() {
        return new ClientServerEndpointBuilder<>(new WebSocketClientBuilder(), new WebSocketServerBuilder());
    }

    /**
     * Creates new DockerClient builder.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ClientServerEndpointBuilder<DockerClientBuilder, DockerClientBuilder> docker() {
        return new ClientServerEndpointBuilder(new DockerClientBuilder(), new DockerClientBuilder()) {
            @Override
            public EndpointBuilder<? extends Endpoint> server() {
                throw new UnsupportedOperationException("Citrus Docker stack has no support for server implementation");
            }
        };
    }

    /**
     * Creates new KubernetesClient builder.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ClientServerEndpointBuilder<KubernetesClientBuilder, KubernetesClientBuilder> kubernetes() {
        return new ClientServerEndpointBuilder(new KubernetesClientBuilder(), new KubernetesClientBuilder()) {
            @Override
            public EndpointBuilder<? extends Endpoint> server() {
                throw new UnsupportedOperationException("Citrus Kubernetes stack has no support for server implementation");
            }
        };
    }

    /**
     * Creates new SeleniumBrowser builder.
     * @return
     */
    public static SeleniumBrowserEndpointBuilder selenium() {
        return new SeleniumBrowserEndpointBuilder();
    }

    /**
     * Creates new JdbcDbServer builder.
     * @return
     */
    public static JdbcDbServerEndpointBuilder jdbc() {
        return new JdbcDbServerEndpointBuilder();
    }

    /**
     * Creates new KafkaEndpoint endpoint builder.
     * @return
     */
    public static AsyncSyncEndpointBuilder<KafkaEndpointBuilder, KafkaEndpointBuilder> kafka() {
        return new AsyncSyncEndpointBuilder<>(new KafkaEndpointBuilder(), new KafkaEndpointBuilder());
    }

}
