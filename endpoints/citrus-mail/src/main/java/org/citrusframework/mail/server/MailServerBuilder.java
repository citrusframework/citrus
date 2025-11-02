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

package org.citrusframework.mail.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.icegreen.greenmail.util.GreenMail;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class MailServerBuilder extends AbstractServerBuilder<MailServer, MailServerBuilder> {

    /** Endpoint target */
    private final MailServer endpoint = new MailServer();

    private String marshaller;
    private String messageConverter;
    private String smtpServer;

    @Override
    public MailServer build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(marshaller)) {
                marshaller(referenceResolver.resolve(marshaller, MailMarshaller.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, MailMessageConverter.class));
            }

            if (StringUtils.hasText(smtpServer)) {
                smtp(referenceResolver.resolve(smtpServer, GreenMail.class));
            }
        }
        return super.build();
    }

    @Override
    protected MailServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public MailServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @SchemaProperty(description = "The mail server port.")
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the mail marshaller.
     */
    public MailServerBuilder marshaller(MailMarshaller marshaller) {
        endpoint.setMarshaller(marshaller);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom mail message marshaller.")
    public void setMarshaller(String marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Sets the Java mail properties.
     */
    public MailServerBuilder javaMailProperties(Properties javaMailProperties) {
        endpoint.setJavaMailProperties(javaMailProperties);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Custom properties passed to the java mail implementation.")
    public void setJavaMailProperties(Map<String, Object> javaMailProperties) {
        Properties props = new Properties();
        props.putAll(javaMailProperties);
        javaMailProperties(props);
    }

    /**
     * Enables/disables user authentication.
     */
    public MailServerBuilder authRequired(boolean authRequired) {
        endpoint.setAuthRequired(authRequired);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "When enabled users must authenticate with the server."
    )
    public void setAuthRequired(boolean authRequired) {
        authRequired(authRequired);
    }

    /**
     * Enables/disables auto accept.
     */
    public MailServerBuilder autoAccept(boolean autoAccept) {
        endpoint.setAutoAccept(autoAccept);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "When enabled server will auto accept client connections."
    )
    public void setAutoAccept(boolean autoAccept) {
        autoAccept(autoAccept);
    }

    /**
     * Enables/disables split multipart.
     */
    public MailServerBuilder splitMultipart(boolean splitMultipart) {
        endpoint.setSplitMultipart(splitMultipart);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the server splits multipart messages into individual parts.")
    public void setSplitMultipart(boolean splitMultipart) {
        splitMultipart(splitMultipart);
    }

    /**
     * Sets the smtpServer property.
     */
    public MailServerBuilder smtp(GreenMail smtpServer) {
        endpoint.setSmtpServer(smtpServer);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the SMTP implementation.")
    public void setSmtp(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Sets the message converter.
     */
    public MailServerBuilder messageConverter(MailMessageConverter messageConverter) {
        endpoint.setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets custom message converter.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the known users.
     */
    public MailServerBuilder knownUsers(List<String> users) {
        endpoint.setKnownUsers(users);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets a list of known users that will be accepted when establishing a connection."
    )
    public void setKnownUsers(List<String> users) {
        knownUsers(users);
    }

    /**
     * Sets the known users.
     */
    public MailServerBuilder knownUsers(String... users) {
        endpoint.setKnownUsers(Arrays.asList(users));
        return this;
    }
}
