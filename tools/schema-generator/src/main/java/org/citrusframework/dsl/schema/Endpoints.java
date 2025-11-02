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

package org.citrusframework.dsl.schema;

import org.citrusframework.camel.endpoint.CamelEndpointsBuilder;
import org.citrusframework.channel.endpoint.builder.MessageChannelEndpointsBuilder;
import org.citrusframework.docker.endpoint.builder.DockerEndpointBuilder;
import org.citrusframework.endpoint.direct.DirectEndpointsBuilder;
import org.citrusframework.ftp.endpoint.builder.FtpEndpointBuilder;
import org.citrusframework.ftp.endpoint.builder.ScpEndpointBuilder;
import org.citrusframework.ftp.endpoint.builder.SftpEndpointBuilder;
import org.citrusframework.http.endpoint.builder.HttpEndpointBuilder;
import org.citrusframework.jms.endpoint.JmsEndpointsBuilder;
import org.citrusframework.kafka.endpoint.builder.KafkaEndpointsBuilder;
import org.citrusframework.kubernetes.endpoint.builder.KubernetesEndpointBuilder;
import org.citrusframework.mail.endpoint.builder.MailEndpointBuilder;
import org.citrusframework.selenium.endpoint.SeleniumBrowserBuilder;
import org.citrusframework.vertx.endpoint.builder.VertxEndpointsBuilder;
import org.citrusframework.websocket.endpoint.builder.WebSocketEndpointBuilder;
import org.citrusframework.ws.endpoint.builder.SoapWebServiceEndpointBuilder;
import org.citrusframework.yaml.SchemaType;
import org.citrusframework.yaml.SchemaProperty;

@SchemaType(oneOf = {
    "http",
    "soap",
    "webSocket",
    "mail",
    "ftp",
    "sftp",
    "scp",
    "docker",
    "kubernetes",
    "jms",
    "vertx",
    "direct",
    "kafka",
    "camel",
    "channel",
    "seleniumBrowser",
})
public interface Endpoints {

    @SchemaProperty(description = "Http client and server endpoints")
    default void setHttp(HttpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "SOAP WebService client and server endpoints")
    default void setSoap(SoapWebServiceEndpointBuilder builder) {
    }

    @SchemaProperty(description = "WebSocket client and server endpoints")
    default void setWebSocket(WebSocketEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Mail client and server endpoints")
    default void setMail(MailEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Ftp client and server endpoints")
    default void setFtp(FtpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Sftp client and server endpoints")
    default void setSftp(SftpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Scp client and server endpoint")
    default void setScp(ScpEndpointBuilder builder) {
    }

    @SchemaProperty(
            description = "Docker client")
    default void setDocker(DockerEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Kubernetes client")
    default void setKubernetes(KubernetesEndpointBuilder builder) {
    }

    @SchemaProperty(title = "JMS", description = "JMS endpoint.")
    default void setJms(JmsEndpointsBuilder builder) {
    }

    @SchemaProperty(title = "Vert.x", description = "Vert.x endpoint.")
    default void setVertx(VertxEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Direct endpoint.")
    default void setDirect(DirectEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Kafka endpoint.")
    default void setKafka(KafkaEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Camel endpoint.")
    default void setCamel(CamelEndpointsBuilder builder) {
    }

    @SchemaProperty(title = "Spring channel", description = "Spring channel endpoint.")
    default void setChannel(MessageChannelEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Selenium browser endpoint.")
    default void setSeleniumBrowser(SeleniumBrowserBuilder builder) {
    }

}
