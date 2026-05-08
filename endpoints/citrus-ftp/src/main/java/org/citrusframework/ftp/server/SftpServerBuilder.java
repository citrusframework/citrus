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

package org.citrusframework.ftp.server;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.server.AbstractServerBuilder;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;

/**
 * @since 2.5
 */
@SchemaType(module = "citrus-ftp")
@XmlType(name = "", propOrder = {})
public class SftpServerBuilder extends AbstractServerBuilder<SftpServer, SftpServerBuilder> {

    /** Endpoint target */
    private final SftpServer endpoint = new SftpServer();

    @Override
    protected SftpServer getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the port property.
     */
    public SftpServerBuilder port(int port) {
        endpoint.setPort(port);
        return this;
    }

    @SchemaProperty(description = "The Ftp server port.")
    @XmlAttribute
    public void setPort(int port) {
        port(port);
    }

    /**
     * Sets the autoConnect property.
     */
    public SftpServerBuilder autoConnect(boolean autoConnect) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setAutoConnect(autoConnect);
        return this;
    }

    @SchemaProperty(description = "When enabled the server uses automatic connect mode.")
    @XmlAttribute(name = "auto-connect")
    public void setAutoConnect(boolean autoConnect) {
        autoConnect(autoConnect);
    }

    /**
     * Sets the autoLogin property.
     */
    public SftpServerBuilder autoLogin(boolean autoLogin) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setAutoLogin(autoLogin);
        return this;
    }

    @SchemaProperty(description = "When enabled the server uses automatic login mode.")
    @XmlAttribute(name = "auto-login")
    public void setAutoLogin(boolean autoLogin) {
        autoLogin(autoLogin);
    }

    /**
     * Sets the user property.
     */
    public SftpServerBuilder user(String user) {
        endpoint.setUser(user);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the allowed user name."
    )
    @XmlAttribute
    public void setUser(String user) {
        user(user);
    }

    /**
     * Sets the password property.
     */
    public SftpServerBuilder password(String password) {
        endpoint.setPassword(password);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the allowed user password."
    )
    @XmlAttribute
    public void setPassword(String password) {
        password(password);
    }

    /**
     * Sets the hostKeyPath property.
     */
    public SftpServerBuilder hostKeyPath(String hostKeyPath) {
        endpoint.setHostKeyPath(hostKeyPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the host key certificate path."
    )
    @XmlAttribute(name = "host-key-path")
    public void setHostKeyPath(String hostKeyPath) {
        hostKeyPath(hostKeyPath);
    }

    /**
     * Sets the userHomePath property.
     */
    public SftpServerBuilder userHomePath(String userHomePath) {
        endpoint.setUserHomePath(userHomePath);
        return this;
    }

    @SchemaProperty(description = "Sets the user home path directory.")
    @XmlAttribute(name = "user-home-path")
    public void setUserHomePath(String userHomePath) {
        userHomePath(userHomePath);
    }

    /**
     * Sets the allowedKeyPath property.
     */
    public SftpServerBuilder allowedKeyPath(String allowedKeyPath) {
        endpoint.setAllowedKeyPath(allowedKeyPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the allowed key certificate path."
    )
    @XmlAttribute(name = "allowed-key-path")
    public void setAllowedKeyPath(String allowedKeyPath) {
        allowedKeyPath(allowedKeyPath);
    }

    /**
     * Sets the polling interval.
     */
    public SftpServerBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    @XmlAttribute(name = "polling-interval")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the strictHostChecking property.
     */
    public SftpServerBuilder strictHostChecking(boolean strictHostChecking) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setStrictHostChecking(strictHostChecking);
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
    public SftpServerBuilder knownHosts(String knownHosts) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setKnownHosts(knownHosts);
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
     * Sets the privateKeyPath property.
     */
    public SftpServerBuilder privateKeyPath(String privateKeyPath) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPrivateKeyPath(privateKeyPath);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Sets the private key certificate path."
    )
    @XmlAttribute(name = "private-kay-path")
    public void setPrivateKeyPath(String privateKeyPath) {
        privateKeyPath(privateKeyPath);
    }

    /**
     * Sets the privateKeyPassword property.
     */
    public SftpServerBuilder privateKeyPassword(String privateKeyPassword) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPrivateKeyPassword(privateKeyPassword);
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
     * Sets the preferredAuthentications property.
     */
    public SftpServerBuilder preferredAuthentications(String preferredAuthentications) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setPreferredAuthentications(preferredAuthentications);
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
    public SftpServerBuilder sessionConfigs(Map<String, String> sessionConfigs) {
        ((SftpEndpointConfiguration) endpoint.getEndpointConfiguration()).setSessionConfigs(sessionConfigs);
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
}
