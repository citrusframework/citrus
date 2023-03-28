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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.xml.transform.Source;

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
import com.icegreen.greenmail.mail.MailAddress;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMailMessage;

/**
 * Mail server implementation starts new SMTP server instance and listens for incoming mail messages. Incoming mail messages
 * are converted to XML representation and forwarded to some message endpoint adapter (e.g. forwarding mail content to
 * a message channel).
 *
 * By default incoming messages are accepted automatically. When auto accept is disabled the endpoint adapter is invoked with
 * accept request and test case has to decide accept outcome in response.
 *
 * In case of incoming multipart mail messages the server is able to split the body parts into separate XML messages
 * handled by the endpoint adapter.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailServer extends AbstractServer {

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

    /** Should split multipart messages for each mime part */
    private boolean splitMultipart = false;

    /** Smtp server instance */
    private GreenMail smtpServer;

    @Override
    protected void startup() {
        smtpServer = new GreenMail(new ServerSetup(port, null, "smtp"));
        smtpServer.getManagers().getSmtpManager().getUserManager().setMessageDeliveryHandler((msg, mailAddress) -> {
            GreenMailUser user = smtpServer.getManagers().getUserManager().getUserByEmail(mailAddress.getEmail());

            if (null == user) {
                String login = mailAddress.getEmail();
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

    public void deliver(MimeMessage msg) {
        MimeMailMessage mimeMailMessage = new MimeMailMessage(msg);
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
     * @param mail
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
     * However latest mail response sent by test case is returned, others are ignored.
     *
     * @param bodyPart
     * @param messageHeaders
     */
    private Message split(BodyPart bodyPart, Map<String, Object> messageHeaders) {
        MailMessage mailRequest = createMailMessage(messageHeaders, bodyPart.getContent(), bodyPart.getContentType());

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

    /**
     * Creates a new mail message model object from message headers.
     * @param messageHeaders
     * @param body
     * @param contentType
     * @return
     */
    protected MailMessage createMailMessage(Map<String, Object> messageHeaders, String body, String contentType) {
        return MailMessage.request(messageHeaders)
                .marshaller(marshaller)
                .from(messageHeaders.get(CitrusMailMessageHeaders.MAIL_FROM).toString())
                .to(messageHeaders.get(CitrusMailMessageHeaders.MAIL_TO).toString())
                .cc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC).toString())
                .bcc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC).toString())
                .subject(messageHeaders.get(CitrusMailMessageHeaders.MAIL_SUBJECT).toString())
                .body(body, contentType);
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
     * Return new mail session if not already created before.
     * @return
     */
    public synchronized Session getSession() {
        if (this.mailSession == null) {
            this.mailSession = Session.getInstance(this.javaMailProperties);
        }
        return this.mailSession;
    }

    /**
     * Is auto accept enabled.
     * @return
     */
    public boolean isAutoAccept() {
        return autoAccept;
    }

    /**
     * Enable/disable auto accept feature.
     * @param autoAccept
     */
    public void setAutoAccept(boolean autoAccept) {
        this.autoAccept = autoAccept;
    }

    /**
     * Gets the mail message marshaller.
     * @return
     */
    public MailMarshaller getMarshaller() {
        return marshaller;
    }

    /**
     * Sets the mail message marshaller.
     * @param marshaller
     */
    public void setMarshaller(MailMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    /**
     * Gets the Java mail properties.
     * @return
     */
    public Properties getJavaMailProperties() {
        return javaMailProperties;
    }

    /**
     * Sets the Java mail properties.
     * @param javaMailProperties
     */
    public void setJavaMailProperties(Properties javaMailProperties) {
        this.javaMailProperties = javaMailProperties;
    }

    /**
     * Gets the server port.
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the server port.
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the smtp server instance.
     * @return
     */
    public GreenMail getSmtpServer() {
        return smtpServer;
    }

    /**
     * Sets the smtp server instance.
     * @param smtpServer
     */
    public void setSmtpServer(GreenMail smtpServer) {
        this.smtpServer = smtpServer;
    }

    /**
     * Gets the split multipart message.
     * @return
     */
    public boolean isSplitMultipart() {
        return splitMultipart;
    }

    /**
     * Sets the split multipart message.
     * @param splitMultipart
     */
    public void setSplitMultipart(boolean splitMultipart) {
        this.splitMultipart = splitMultipart;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public MailMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(MailMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
