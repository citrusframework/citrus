/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.mail.client;

import com.consol.citrus.endpoint.AbstractEndpointConfiguration;
import com.consol.citrus.mail.model.MailMessageMapper;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Mail sender implementation */
    private JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    /** Mail message mapper converts from XML to mail message object */
    private MailMessageMapper mailMessageMapper = new MailMessageMapper();

    /**
     * Gets the mail protocol.
     * @return the mail protocol.
     */
    public String getProtocol() {
        return javaMailSender.getProtocol();
    }

    /**
     * Set the mail protocol. Default is "smtp".
     * @param protocol
     */
    public void setProtocol(String protocol) {
        javaMailSender.setProtocol(protocol);
    }

    /**
     * Gets the mail host.
     * @return the mail host.
     */
    public String getHost() {
        return javaMailSender.getHost();
    }

    /**
     * Set the mail server host, typically an SMTP host.
     * @param host
     */
    public void setHost(String host) {
        javaMailSender.setHost(host);
    }

    /**
     * Gets the mail port.
     * @return the mail port.
     */
    public int getPort() {
        return javaMailSender.getPort();
    }

    /**
     * Set the mail server port.
     * Default is the Java mail port for SMTP (25).
     * @param port
     */
    public void setPort(int port) {
        javaMailSender.setPort(port);
    }

    /**
     * Gets the mail username.
     * @return the mail username.
     */
    public String getUsername() {
        return javaMailSender.getUsername();
    }

    /**
     * Set the username for accessing the mail host. Underlying mail seesion
     * has to be configured with the property <code>"mail.smtp.auth"</code> set to
     * <code>true</code>.
     * @param username
     */
    public void setUsername(String username) {
        javaMailSender.setUsername(username);
    }

    /**
     * Gets the mail password.
     * @return the mail ppassword.
     */
    public String getPassword() {
        return javaMailSender.getPassword();
    }

    /**
     * Set the password for accessing the mail host. Underlying mail seesion
     * has to be configured with the property <code>"mail.smtp.auth"</code> set to
     * <code>true</code>.
     * @param password
     */
    public void setPassword(String password) {
        javaMailSender.setPassword(password);
    }

    /**
     * Gets the mail properties.
     * @return the mail properties.
     */
    public Properties getJavaMailProperties() {
        return javaMailSender.getJavaMailProperties();
    }

    /**
     * Set JavaMail properties for the mail session such as <code>"mail.smtp.auth"</code>
     * when using username and password. New session is created when properties are set.
     * @param javaMailProperties
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        javaMailSender.setJavaMailProperties(javaMailProperties);
    }

    /**
     * Gets the mail message mapper implementation.
     * @return
     */
    public MailMessageMapper getMailMessageMapper() {
        return mailMessageMapper;
    }

    /**
     * Sets the mail message mapper implementation.
     * @param mailMessageMapper
     */
    public void setMailMessageMapper(MailMessageMapper mailMessageMapper) {
        this.mailMessageMapper = mailMessageMapper;
    }

    /**
     * Gets the Java mail sender implementation.
     * @return
     */
    public JavaMailSenderImpl getJavaMailSender() {
        return javaMailSender;
    }

    /**
     * Sets the Java mail sender implementation.
     * @param javaMailSender
     */
    public void setJavaMailSender(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

}
