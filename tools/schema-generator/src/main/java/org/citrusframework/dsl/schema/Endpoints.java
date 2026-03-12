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
import org.citrusframework.endpoint.context.ContextEndpointsBuilder;
import org.citrusframework.endpoint.direct.DirectEndpointsBuilder;
import org.citrusframework.ftp.endpoint.builder.FtpEndpointBuilder;
import org.citrusframework.ftp.endpoint.builder.ScpEndpointBuilder;
import org.citrusframework.ftp.endpoint.builder.SftpEndpointBuilder;
import org.citrusframework.http.endpoint.builder.HttpEndpointBuilder;
import org.citrusframework.jms.endpoint.JmsEndpointsBuilder;
import org.citrusframework.kafka.endpoint.builder.KafkaEndpointsBuilder;
import org.citrusframework.kubernetes.endpoint.builder.KubernetesEndpointBuilder;
import org.citrusframework.mail.endpoint.builder.MailEndpointBuilder;
import org.citrusframework.selenium.endpoint.SeleniumEndpointBuilder;
import org.citrusframework.vertx.endpoint.builder.VertxEndpointsBuilder;
import org.citrusframework.websocket.endpoint.builder.WebSocketEndpointBuilder;
import org.citrusframework.ws.endpoint.builder.SoapWebServiceEndpointBuilder;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

@SchemaType(oneOf = {
    "http",
    "soap",
    "webSocket",
    "context",
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
    "selenium",
})
public interface Endpoints {

    @SchemaProperty(description = "Http client and server endpoints", module = "citrus-http")
    default void setHttp(HttpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "SOAP WebService client and server endpoints", module = "citrus-ws")
    default void setSoap(SoapWebServiceEndpointBuilder builder) {
    }

    @SchemaProperty(description = "WebSocket client and server endpoints", module = "citrus-websocket")
    default void setWebSocket(WebSocketEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Mail client and server endpoints", module = "citrus-mail")
    default void setMail(MailEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Ftp client and server endpoints", module = "citrus-ftp")
    default void setFtp(FtpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Sftp client and server endpoints", module = "citrus-ftp")
    default void setSftp(SftpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Scp client and server endpoint", module = "citrus-ftp")
    default void setScp(ScpEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Docker client", module = "citrus-docker")
    default void setDocker(DockerEndpointBuilder builder) {
    }

    @SchemaProperty(description = "Kubernetes client", module = "citrus-kubernetes")
    default void setKubernetes(KubernetesEndpointBuilder builder) {
    }

    @SchemaProperty(title = "JMS", description = "JMS endpoint", module = "citrus-jms")
    default void setJms(JmsEndpointsBuilder builder) {
    }

    @SchemaProperty(title = "Vert.x", description = "Vert.x endpoint.", module = "citrus-vertx")
    default void setVertx(VertxEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Direct endpoint.")
    default void setDirect(DirectEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Context endpoint.")
    default void setContext(ContextEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Kafka endpoint.", module = "citrus-kafka")
    default void setKafka(KafkaEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Camel endpoint.", module = "citrus-camel")
    default void setCamel(CamelEndpointsBuilder builder) {
    }

    @SchemaProperty(title = "Spring channel", description = "Spring channel endpoint.", module = "citrus-spring-integration")
    default void setChannel(MessageChannelEndpointsBuilder builder) {
    }

    @SchemaProperty(description = "Selenium endpoint.", module = "citrus-selenium")
    default void setSelenium(SeleniumEndpointBuilder builder) {
    }

}
