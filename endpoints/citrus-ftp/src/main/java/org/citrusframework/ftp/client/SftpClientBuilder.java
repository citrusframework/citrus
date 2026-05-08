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

package org.citrusframework.ftp.client;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.7.5
 */
@SchemaType(module = "citrus-ftp")
@XmlType(name = "", propOrder = {})
public class SftpClientBuilder extends AbstractEndpointBuilder<SftpClient> {

    /** Endpoint target */
    private final SftpClient endpoint = new SftpClient();

    private String correlator;

    @Override
    public SftpClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }
        }

        return super.build();
    }

    @Override
    protected SftpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the host property.
     */
    public SftpClientBuilder host(String host) {
        endpoint.getEndpointConfiguration().setHost(host);
        return this;
    }

    @SchemaProperty(description = "The Ftp server host.")
    @XmlAttribute
    public void setHost(String host) {
        host(host);
    }

    /**
     * Sets the port property.
     */
    public SftpClientBuilder port(int port) {
        endpoint.getEndpointConfiguration().setPort(port);
        return this;
    }

    @SchemaProperty(description = "The Ftp server port.")
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the auto read files property.
     */
    public SftpClientBuilder autoReadFiles(boolean autoReadFiles) {
        endpoint.getEndpointConfiguration().setAutoReadFiles(autoReadFiles);
        return this;
    }

    @SchemaProperty(description = "When enabled the client automatically reads new files.")
    @XmlAttribute(name = "auto-read-files")
    public void setAutoReadFiles(boolean autoReadFiles) {
        autoReadFiles(autoReadFiles);
    }

    /**
     * Sets the local passive mode property.
     */
    public SftpClientBuilder localPassiveMode(boolean localPassiveMode) {
        endpoint.getEndpointConfiguration().setLocalPassiveMode(localPassiveMode);
        return this;
    }

    @SchemaProperty(description = "SEnables the local passive mode.")
    @XmlAttribute(name = "local-passive-mode")
    public void setLocalPassiveMode(boolean localPassiveMode) {
        localPassiveMode(localPassiveMode);
    }

    /**
     * Sets the client username.
     */
    public SftpClientBuilder username(String username) {
        endpoint.getEndpointConfiguration().setUser(username);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user name."
    )
    @XmlAttribute
    public void setUsername(String username) {
        username(username);
    }

    /**
     * Sets the client password.
     */
    public SftpClientBuilder password(String password) {
        endpoint.getEndpointConfiguration().setPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the user password."
    )
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the privateKeyPath property.
     */
    public SftpClientBuilder privateKeyPath(String privateKeyPath) {
        endpoint.getEndpointConfiguration().setPrivateKeyPath(privateKeyPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the private key path."
    )
    @XmlAttribute(name = "private-key-path")
    public void setPrivateKeyPath(String privateKeyPath) {
        privateKeyPath(privateKeyPath);
    }

    /**
     * Sets the privateKeyPassword property.
     */
    public SftpClientBuilder privateKeyPassword(String privateKeyPassword) {
        endpoint.getEndpointConfiguration().setPrivateKeyPassword(privateKeyPassword);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the private key password."
    )
    @XmlAttribute(name = "private-key-password")
    public void setPrivateKeyPassword(String privateKeyPassword) {
        privateKeyPassword(privateKeyPassword);
    }

    /**
     * Sets the strictHostChecking property.
     */
    public SftpClientBuilder strictHostChecking(boolean strictHostChecking) {
        endpoint.getEndpointConfiguration().setStrictHostChecking(strictHostChecking);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Enable strict host checking."
    )
    @XmlAttribute(name = "strict-host-checking")
    public void setStrictHostChecking(boolean strictHostChecking) {
        strictHostChecking(strictHostChecking);
    }

    /**
     * Sets the knownHosts property.
     */
    public SftpClientBuilder knownHosts(String knownHosts) {
        endpoint.getEndpointConfiguration().setKnownHosts(knownHosts);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "List of known hosts."
    )
    @XmlAttribute(name = "known-hosts")
    public void setKnownHosts(String knownHosts) {
        knownHosts(knownHosts);
    }

    /**
     * Sets the preferred authentications property.
     */
    public SftpClientBuilder preferredAuthentications(String preferredAuthentications) {
        endpoint.getEndpointConfiguration().setPreferredAuthentications(preferredAuthentications);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the preferred authentication mechanism."
    )
    @XmlAttribute(name = "preferred-authentications")
    public void setPreferredAuthentications(String preferredAuthentications) {
        preferredAuthentications(preferredAuthentications);
    }

    /**
     * Sets the sessionConfigs property.
     */
    public SftpClientBuilder sessionConfigs(Map<String, String> sessionConfigs) {
        endpoint.getEndpointConfiguration().setSessionConfigs(sessionConfigs);
        return this;
    }

    @SchemaProperty(description = "The session configuration.")
    @XmlTransient
    public void setSessionConfigs(Map<String, String> sessionConfigs) {
        sessionConfigs(sessionConfigs);
    }

    @XmlAttribute(name = "session-configs")
    public void setSessionConfigs(String sessionConfigs) {
        setSessionConfigs(Arrays.stream(sessionConfigs.split(","))
                .map(String::trim)
                .filter(expression -> expression.contains("="))
                .map(expression -> expression.split("=", 2))
                .collect(Collectors.toMap(tokens -> tokens[0].trim(), tokens -> tokens[1].trim())));
    }

    /**
     * Sets the message correlator.
     */
    public SftpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    @XmlAttribute(name = "message-correlator")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the error handling strategy.
     */
    public SftpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandler") },
            description = "Sets the error handling strategy."
    )
    @XmlAttribute(name = "error-handling-strategy")
    public void setErrorHandlingStrategy(String errorStrategy) {
        try {
            errorHandlingStrategy(ErrorHandlingStrategy.fromName(errorStrategy));
        } catch (IllegalArgumentException e) {
            errorHandlingStrategy(ErrorHandlingStrategy.valueOf(errorStrategy));
        }
    }

    /**
     * Sets the polling interval.
     */
    public SftpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public SftpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    @XmlAttribute
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
