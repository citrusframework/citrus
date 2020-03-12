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

import com.consol.citrus.channel.endpoint.builder.MessageChannelEndpoints;
import com.consol.citrus.docker.endpoint.builder.DockerEndpoints;
import com.consol.citrus.ftp.endpoint.builder.FtpEndpoints;
import com.consol.citrus.ftp.endpoint.builder.ScpEndpoints;
import com.consol.citrus.ftp.endpoint.builder.SftpEndpoints;
import com.consol.citrus.http.endpoint.builder.HttpEndpoints;
import com.consol.citrus.jdbc.endpoint.builder.JdbcEndpoints;
import com.consol.citrus.jms.endpoint.JmsEndpoints;
import com.consol.citrus.jmx.endpoint.builder.JmxEndpoints;
import com.consol.citrus.kafka.endpoint.builder.KafkaEndpoints;
import com.consol.citrus.kubernetes.endpoint.builder.KubernetesEndpoints;
import com.consol.citrus.mail.endpoint.builder.MailEndpoints;
import com.consol.citrus.rmi.endpoint.builder.RmiEndpoints;
import com.consol.citrus.selenium.endpoint.builder.SeleniumEndpoints;
import com.consol.citrus.ssh.endpoint.builder.SshEndpoints;
import com.consol.citrus.vertx.endpoint.builder.VertxEndpoints;
import com.consol.citrus.websocket.endpoint.builder.WebSocketEndpoints;
import com.consol.citrus.ws.endpoint.builder.WebServiceEndpoints;

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
    public static MessageChannelEndpoints channel() {
        return MessageChannelEndpoints.channel();
    }

    /**
     * Creates new JmsEndpoint sync or async builder.
     * @return
     */
    public static JmsEndpoints jms() {
        return JmsEndpoints.jms();
    }

    /**
     * Creates new HttpClient or HttpServer builder.
     * @return
     */
    public static HttpEndpoints http() {
        return HttpEndpoints.http();
    }

    /**
     * Creates new WebServiceClient or WebServiceServer builder.
     * @return
     */
    public static WebServiceEndpoints soap() {
        return WebServiceEndpoints.soap();
    }

    /**
     * Creates new JmxClient or JmxServer builder.
     * @return
     */
    public static JmxEndpoints jmx() {
        return JmxEndpoints.jmx();
    }

    /**
     * Creates new RmiClient or RmiServer builder.
     * @return
     */
    public static RmiEndpoints rmi() {
        return RmiEndpoints.rmi();
    }

    /**
     * Creates new MailClient or MailServer builder.
     * @return
     */
    public static MailEndpoints mail() {
        return MailEndpoints.mail();
    }

    /**
     * Creates new FtpClient or FtpServer builder.
     * @return
     */
    public static FtpEndpoints ftp() {
        return FtpEndpoints.ftp();
    }

    /**
     * Creates new SftpClient or SftpServer builder.
     * @return
     */
    public static SftpEndpoints sftp() {
        return SftpEndpoints.sftp();
    }

    /**
     * Creates new ScpClient or SftpServer builder.
     * @return
     */
    public static ScpEndpoints scp() {
        return ScpEndpoints.scp();
    }

    /**
     * Creates new SshClient or SshServer builder.
     * @return
     */
    public static SshEndpoints ssh() {
        return SshEndpoints.ssh();
    }

    /**
     * Creates new VertxEndpoint sync or async builder.
     * @return
     */
    public static VertxEndpoints vertx() {
        return VertxEndpoints.vertx();
    }

    /**
     * Creates new WebSocketClient or WebSocketServer builder.
     * @return
     */
    public static WebSocketEndpoints websocket() {
        return WebSocketEndpoints.websocket();
    }

    /**
     * Creates new DockerClient builder.
     * @return
     */
    public static DockerEndpoints docker() {
        return DockerEndpoints.docker();
    }

    /**
     * Creates new KubernetesClient builder.
     * @return
     */
    public static KubernetesEndpoints kubernetes() {
        return KubernetesEndpoints.kubernetes();
    }

    /**
     * Creates new SeleniumBrowser builder.
     * @return
     */
    public static SeleniumEndpoints selenium() {
        return SeleniumEndpoints.selenium();
    }

    /**
     * Creates new JdbcDbServer builder.
     * @return
     */
    public static JdbcEndpoints jdbc() {
        return JdbcEndpoints.jdbc();
    }

    /**
     * Creates new KafkaEndpoint endpoint builder.
     * @return
     */
    public static KafkaEndpoints kafka() {
        return KafkaEndpoints.kafka();
    }

}
