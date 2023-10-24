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

package org.citrusframework.mail.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.xml.transform.Source;

import com.icegreen.greenmail.mail.MailAddress;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.user.UserManager;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.mail.client.MailEndpointConfiguration;
import org.citrusframework.mail.message.CitrusMailMessageHeaders;
import org.citrusframework.mail.message.MailMessage;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.AcceptResponse;
import org.citrusframework.mail.model.AttachmentPart;
import org.citrusframework.mail.model.BodyPart;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.model.MailRequest;
import org.citrusframework.mail.model.MailResponse;
import org.citrusframework.message.Message;
import org.citrusframework.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMailMessage;

/**
 * Mail server implementation starts new SMTP server instance and listens for incoming mail messages. Incoming mail messages
 * are converted to XML representation and forwarded to some message endpoint adapter (e.g. forwarding mail content to
 * a message channel).
 * <p>
 * By default, incoming messages are accepted automatically. When auto accept is disabled the endpoint adapter is invoked with
 * accept request and test case has to decide accept outcome in response.
 * <p>
 * In case of incoming multipart mail messages the server is able to split the body parts into separate XML messages
 * handled by the endpoint adapter.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailServer extends AbstractServer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MailServer.class);

    /** Server port */
    private int port = 25;

    /** XML message mapper */
    private MailMarshaller marshaller = new MailMarshaller();

    /** Mail message converter */
    private MailMessageConverter messageConverter = new MailMessageConverter();

    /** Java mail session */
    private Session mailSession;

    /** Java mail properties */
    private Properties javaMailProperties = new Properties();

    /** Should accept automatically or handled via test case */
    private boolean autoAccept = true;

    /** Requires users to properly authenticate with the server */
    private boolean authRequired = true;

    /** Should split multipart messages for each mime part */
    private boolean splitMultipart = false;

    private final List<String> knownUsers = new ArrayList<>();

    /** Smtp server instance */
    private GreenMail smtpServer;

    @Override
    protected void startup() {
        smtpServer = new GreenMail(new ServerSetup(port, null, "smtp"));

        if (!authRequired) {
            smtpServer.getManagers().getSmtpManager().getUserManager().setAuthRequired(false);
        } else {
            addKnownUsers(smtpServer.getManagers().getSmtpManager().getUserManager());
        }

        smtpServer.getManagers().getSmtpManager().getUserManager().setMessageDeliveryHandler((msg, mailAddress) -> {
            GreenMailUser user = smtpServer.getManagers().getUserManager().getUserByEmail(mailAddress.getEmail());

            if (null == user) {
                String login = mailAddress.getUser();
                String email = mailAddress.getEmail();
                String password = mailAddress.getEmail();
                user = smtpServer.getManagers().getUserManager().createUser(email, login, password);
            }

            if (!accept(msg.getReturnPath().getEmail(), msg.getToAddresses())) {
                throw new AuthenticationFailedException("Invalid user");
            }

            deliver(msg.getMessage());
            return user;
        });

        smtpServer.start();
    }

    private void addKnownUsers(UserManager userManager) {
        knownUsers.stream()
            .map(userSpec -> userSpec.split(":"))
            .map(credentials -> {
                if (credentials.length > 3) {
                    return new String[] { credentials[0], credentials[1], credentials[2] };
                } else if (credentials.length == 2) {
                    return new String[] { credentials[0], credentials[1], credentials[0] };
                } else if (credentials.length == 1) {
                    return new String[] { credentials[0], credentials[0], credentials[0] };
                } else {
                    return credentials;
                }
            })
            .filter(credentials -> credentials.length == 3)
            .forEach(credentials -> {
                try {
                    userManager.createUser(credentials[0], credentials[1], credentials[2]);
                } catch (UserException e) {
                    logger.warn(String.format("Failed to create known user: %s:%s:%s", credentials[0], credentials[1], credentials[2]));
                }
            });
    }

    @Override
    protected void shutdown() {
        smtpServer.stop();
    }

    public boolean accept(String from, List<MailAddress> recipients) {
        if (autoAccept) {
            return true;
        }

        Message response = getEndpointAdapter().handleMessage(
                MailMessage.accept(from, recipients.stream().map(MailAddress::getEmail).collect(Collectors.joining(",")))
                           .marshaller(marshaller));

        if (response == null || response.getPayload() == null) {
            throw new CitrusRuntimeException("Did not receive accept response. Missing accept response because autoAccept is disabled.");
        }

        AcceptResponse acceptResponse = null;
        if (response.getPayload() instanceof AcceptResponse) {
            acceptResponse = (AcceptResponse) response.getPayload();
        } else if (response.getPayload() instanceof String) {
            acceptResponse = (AcceptResponse) marshaller.unmarshal(response.getPayload(Source.class));
        }

        if (acceptResponse == null) {
            throw new CitrusRuntimeException("Unable to read accept response from payload: " + response);
        }

        return acceptResponse.isAccept();
    }

    public void deliver(MimeMessage mimeMessage) {
        MimeMailMessage mimeMailMessage = new MimeMailMessage(mimeMessage);
        MailMessage request = messageConverter.convertInbound(mimeMailMessage, getEndpointConfiguration(), null);
        Message response = invokeEndpointAdapter(request);

        if (response != null && response.getPayload() != null) {
            MailResponse mailResponse = null;
            if (response.getPayload() instanceof MailResponse) {
                mailResponse = (MailResponse) response.getPayload();
            } else if (response.getPayload() instanceof String) {
                mailResponse = (MailResponse) marshaller.unmarshal(response.getPayload(Source.class));
            }

            if (mailResponse != null && mailResponse.getCode() != MailResponse.OK_CODE) {
                throw new CitrusRuntimeException(String.format("%s %s", mailResponse.getCode(), mailResponse.getMessage()));
            }
        }
    }

    /**
     * Invokes the endpoint adapter with constructed mail message and headers.
     */
    protected Message invokeEndpointAdapter(MailMessage mail) {
        if (splitMultipart) {
            return split(mail.getPayload(MailRequest.class).getBody(), mail.getHeaders());
        } else {
            return getEndpointAdapter().handleMessage(mail);
        }
    }

    /**
     * Split mail message into several messages. Each body and each attachment results in separate message
     * invoked on endpoint adapter. Mail message response if any should be sent only once within test case.
     * However, the latest mail response sent by test case is returned, others are ignored.
     */
    private Message split(BodyPart bodyPart, Map<String, Object> messageHeaders) {
        MailMessage mailRequest = messageConverter.createMailRequest(messageHeaders, new BodyPart(bodyPart.getContent(), bodyPart.getContentType()), marshaller);

        Stack<Message> responseStack = new Stack<>();
        if (bodyPart instanceof AttachmentPart) {
            fillStack(getEndpointAdapter().handleMessage(mailRequest
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())
                    .setHeader(CitrusMailMessageHeaders.MAIL_FILENAME, ((AttachmentPart) bodyPart).getFileName())), responseStack);
        } else {
            fillStack(getEndpointAdapter().handleMessage(mailRequest
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())), responseStack);
        }

        if (bodyPart.hasAttachments()) {
            for (AttachmentPart attachmentPart : bodyPart.getAttachments().getAttachments()) {
                fillStack(split(attachmentPart, messageHeaders), responseStack);
            }
        }

        return responseStack.isEmpty() ? null : responseStack.pop();
    }

    private void fillStack(Message message, Stack<Message> responseStack) {
        if (message != null) {
            responseStack.push(message);
        }
    }

    @Override
    public MailEndpointConfiguration getEndpointConfiguration() {
        MailEndpointConfiguration endpointConfiguration = new MailEndpointConfiguration();
        endpointConfiguration.setMessageConverter(messageConverter);
        endpointConfiguration.setMarshaller(marshaller);
        endpointConfiguration.setJavaMailProperties(javaMailProperties);

        return endpointConfiguration;
    }

    /**
     * Return a new mail session if not already created before.
     */
    public synchronized Session getSession() {
        if (mailSession == null) {
            mailSession = Session.getInstance(javaMailProperties);
        }

        return mailSession;
    }

    /**
     * Users must authenticate properly with the server.
     * @return
     */
    public boolean isAuthRequired() {
        return authRequired;
    }

    /**
     * Enable/disable the user authentication on this server.
     * @param authRequired
     */
    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    /**
     * Is auto accept enabled.
     */
    public boolean isAutoAccept() {
        return autoAccept;
    }

    /**
     * Enable/disable auto accept feature.
     */
    public void setAutoAccept(boolean autoAccept) {
        this.autoAccept = autoAccept;
    }

    /**
     * Gets the mail message marshaller.
     */
    public MailMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the mail message marshaller.
     */
    public void setMarshaller(MailMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the Java mail properties.
     */
    public Properties getJavaMailProperties() {
        return javaMailProperties;
    }

    /**
     * Sets the Java mail properties.
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    /**
     * Gets the server port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the server port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the smtp server instance.
     */
    public GreenMail getSmtpServer() {
        return smtpServer;
    }

    /**
     * Sets the smtp server instance.
     */
    public void setSmtpServer(GreenMail smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Gets the split multipart message.
     */
    public boolean isSplitMultipart() {
        return splitMultipart;
    }

    /**
     * Sets the split multipart message.
     */
    public void setSplitMultipart(boolean splitMultipart) {
        this.splitMultipart = splitMultipart;
    }

    /**
     * Gets the message converter.
     */
    public MailMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     */
    public void setMessageConverter(MailMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Gets the known users.
     * @return
     */
    public List<String> getKnownUsers() {
        return knownUsers;
    }

    /**
     * Sets the known users.
     * @param knownUsers
     */
    public void setKnownUsers(List<String> knownUsers) {
        this.knownUsers.addAll(knownUsers);
    }

    /**
     * Adds a new user known to this mail server.
     * @param email
     * @param login
     * @param password
     */
    public void addKnownUser(String email, String login, String password) {
        this.knownUsers.add(String.join(":", email, login, password));
    }
}
