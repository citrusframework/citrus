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

package com.consol.citrus.mail.server;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.model.*;
import com.consol.citrus.mail.model.BodyPart;
import com.consol.citrus.server.AbstractServer;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class MailServer extends AbstractServer implements SimpleMessageListener, InitializingBean {

    /** Server port */
    private int port = 25;

    /** XML message mapper */
    private XStream mailMessageMapper = new MailMessageMapper();

    /** Java mail session */
    private Session mailSession;

    /** Java mail properties */
    private Properties javaMailProperties = new Properties();

    /** Should accept automatically or handled via test case */
    private boolean autoAccept = true;

    /** Should split multipart messages for each mime part */
    private boolean splitMultipart = false;

    /** Mail delivery date format */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** Smtp server instance */
    private SMTPServer smtpServer;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MailServer.class);

    @Override
    protected void startup() {
        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(this));
        smtpServer.setSoftwareName(getName());
        smtpServer.setPort(port);
        smtpServer.start();
    }

    @Override
    protected void shutdown() {
        smtpServer.stop();
    }

    @Override
    public boolean accept(String from, String recipient) {
        if (autoAccept) {
            return true;
        }

        org.springframework.integration.Message<?> response = getEndpointAdapter().handleMessage(MessageBuilder
                .withPayload(mailMessageMapper.toXML(createAcceptRequest(from, recipient)))
                .build());

        if (response == null || response.getPayload() == null) {
            throw new CitrusRuntimeException("Did not receive accept response. Missing accept response because autoAccept is disabled.");
        }

        AcceptResponse acceptResponse = null;
        if (response.getPayload() instanceof AcceptResponse) {
            acceptResponse = (AcceptResponse) response.getPayload();
        } else if (response.getPayload() instanceof String) {
            acceptResponse = (AcceptResponse) mailMessageMapper.fromXML(response.getPayload().toString());
        }

        if (acceptResponse == null) {
            throw new CitrusRuntimeException("Unable to read accept response from paylaod: " + response);
        }

        return acceptResponse.isAccept();
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) {
        try {
            MimeMailMessage message = new MimeMailMessage(new MimeMessage(getSession(), data));
            Map<String, String> messageHeaders = createMessageHeaders(message);
            MailMessage mailMessage = createMailMessage(messageHeaders);
            mailMessage.setBody(handlePart(message.getMimeMessage()));

            org.springframework.integration.Message response = invokeMessageHandler(mailMessage, messageHeaders);

            if (response != null && response.getPayload() != null) {
                MailMessageResponse mailResponse = null;
                if (response.getPayload() instanceof MailMessageResponse) {
                    mailResponse = (MailMessageResponse) response.getPayload();
                } else if (response.getPayload() instanceof String) {
                    mailResponse = (MailMessageResponse) mailMessageMapper.fromXML(response.getPayload().toString());
                }

                if (mailResponse != null && mailResponse.getCode() != MailMessageResponse.OK_CODE) {
                    throw new RejectException(mailResponse.getCode(), mailResponse.getMessage());
                }
            }
        } catch (MessagingException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Invokes the message handler with constructed mail message and headers.
     * @param mailMessage
     * @param messageHeaders
     */
    protected org.springframework.integration.Message<?> invokeMessageHandler(MailMessage mailMessage, Map<String, String> messageHeaders) {
        if (splitMultipart) {
            return split(mailMessage.getBody(), messageHeaders);
        } else {
            return getEndpointAdapter().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(mailMessageMapper.toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .build());
        }
    }

    /**
     * Split mail message into several messages. Each body and each attachment results in separate message
     * invoked on message handler. Mail message response if any should be sent only once within test case.
     * However latest mail response sent by test case is returned, others are ignored.
     *
     * @param bodyPart
     * @param messageHeaders
     */
    private org.springframework.integration.Message<?> split(BodyPart bodyPart, Map<String, String> messageHeaders) {
        MailMessage mailMessage = createMailMessage(messageHeaders);
        mailMessage.setBody(new BodyPart(bodyPart.getContent(), bodyPart.getContentType()));

        Stack<org.springframework.integration.Message<?>> responseStack = new Stack<org.springframework.integration.Message<?>>();
        if (bodyPart instanceof AttachmentPart) {
            fillStack(getEndpointAdapter().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(mailMessageMapper.toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())
                    .setHeader(CitrusMailMessageHeaders.MAIL_FILENAME, ((AttachmentPart) bodyPart).getFileName())
                    .build()), responseStack);
        } else {
            fillStack(getEndpointAdapter().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(mailMessageMapper.toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())
                    .build()), responseStack);
        }

        if (bodyPart.hasAttachments()) {
            for (AttachmentPart attachmentPart : bodyPart.getAttachments()) {
                fillStack(split(attachmentPart, messageHeaders), responseStack);
            }
        }

        return responseStack.isEmpty() ? null : responseStack.pop();
    }

    private void fillStack(org.springframework.integration.Message<?> message, Stack<org.springframework.integration.Message<?>> responseStack) {
        if (message != null) {
            responseStack.push(message);
        }
    }

    /**
     * Process message part. Can be a text, binary or multipart instance.
     * @param part
     * @return
     * @throws IOException
     */
    protected BodyPart handlePart(MimePart part) throws IOException, MessagingException {
        String contentType = parseContentType(part.getContentType());

        if (part.isMimeType("multipart/*")) {
            return handleMultiPart((Multipart) part.getContent());
        } else if (part.isMimeType("text/*")) {
            return handleTextPart(part, contentType);
        } else if (part.isMimeType("image/*")) {
            return handleImageBinaryPart(part, contentType);
        } else if (part.isMimeType("application/*")) {
            return handleApplicationContentPart(part, contentType);
        } else {
            return handleBinaryPart(part, contentType);
        }
    }

    /**
     * Construct multipart body with first part being the body content and further parts being the attachments.
     * @param body
     * @return
     * @throws IOException
     */
    private BodyPart handleMultiPart(Multipart body) throws IOException, MessagingException {
        BodyPart bodyPart = null;
        for (int i = 0; i < body.getCount(); i++) {
            MimePart entity = (MimePart) body.getBodyPart(i);

            if (bodyPart == null) {
                bodyPart = handlePart(entity);
            } else {
                BodyPart attachment = handlePart(entity);
                bodyPart.addPart(new AttachmentPart(attachment.getContent(), parseContentType(attachment.getContentType()), entity.getFileName()));
            }
        }

        return bodyPart;
    }

    /**
     * Construct body part form special application data. Based on known application content types delegate to text,
     * image or binary body construction.
     * @param applicationData
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleApplicationContentPart(MimePart applicationData, String contentType) throws IOException, MessagingException {
        if (applicationData.isMimeType("application/pdf")) {
            return handleImageBinaryPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/rtf")) {
            return handleImageBinaryPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/java")) {
            return handleTextPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/x-javascript")) {
            return handleTextPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/xhtml+xml")) {
            return handleTextPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/json")) {
            return handleTextPart(applicationData, contentType);
        } else if (applicationData.isMimeType("application/postscript")) {
            return handleTextPart(applicationData, contentType);
        } else {
            return handleBinaryPart(applicationData, contentType);
        }
    }

    /**
     * Construct base64 body part from image data.
     * @param image
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleImageBinaryPart(MimePart image, String contentType) throws IOException, MessagingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileCopyUtils.copy(image.getInputStream(), bos);
        String base64 = Base64.encodeBase64String(bos.toByteArray());
        return new BodyPart(base64, contentType);
    }

    /**
     * Construct simple body part from binary data just adding file name as content.
     * @param mediaPart
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleBinaryPart(MimePart mediaPart, String contentType) throws IOException, MessagingException {
        String contentId = mediaPart.getContentID() != null ? "(" + mediaPart.getContentID() + ")" : "";
        return new BodyPart(mediaPart.getFileName() + contentId, contentType);
    }

    /**
     * Construct simple binary body part with base64 data.
     * @param textPart
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleTextPart(MimePart textPart, String contentType) throws IOException, MessagingException {
        String text = (String) textPart.getContent();
        return new BodyPart(stripMailBodyEnding(text), contentType);
    }

    /**
     * When content type has multiple lines this method just returns plain content type information in first line.
     * This is the case when multipart mixed content type has boundary information in next line.
     * @param contentType
     * @return
     * @throws IOException
     */
    private String parseContentType(String contentType) throws IOException {
        if (contentType.indexOf(System.getProperty("line.separator")) > 0) {
            BufferedReader reader = new BufferedReader(new StringReader(contentType));

            try {
                String plainContentType = reader.readLine();
                if (plainContentType != null && plainContentType.trim().endsWith(";")) {
                    plainContentType = plainContentType.trim().substring(0, plainContentType.length() - 1);
                }

                return plainContentType;
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Failed to close reader", e);
                }
            }
        }

        return contentType;
    }

    /**
     * Removes SMTP mail body ending which is defined by single '.' character in separate line marking
     * the mail body end of file.
     * @param textBody
     * @return
     */
    private String stripMailBodyEnding(String textBody) throws IOException {
        BufferedReader reader = null;
        StringBuilder body = new StringBuilder();

        try {
            reader = new BufferedReader(new StringReader(textBody));

            String line = reader.readLine();
            while (StringUtils.hasText(line)) {
                if (line.trim().equals(".")) {
                    break;
                }

                body.append(line + System.getProperty("line.separator"));
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn("Failed to close reader", e);
                }
            }
        }

        return body.toString().trim();
    }

    private AcceptRequest createAcceptRequest(String from, String recipient) {
        return new AcceptRequest(from, recipient);
    }

    /**
     * Creates a new mail message model object from message headers.
     * @param messageHeaders
     * @return
     */
    protected MailMessage createMailMessage(Map<String, String> messageHeaders) {
        MailMessage message = new MailMessage();
        message.setFrom(messageHeaders.get(CitrusMailMessageHeaders.MAIL_FROM));
        message.setTo(messageHeaders.get(CitrusMailMessageHeaders.MAIL_TO));
        message.setCc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC));
        message.setBcc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC));
        message.setSubject(messageHeaders.get(CitrusMailMessageHeaders.MAIL_SUBJECT));
        return message;
    }

    /**
     * Reads basic message information such as sender, recipients and mail subject to message headers.
     * @param msg
     * @return
     */
    protected Map<String,String> createMessageHeaders(MimeMailMessage msg) throws MessagingException, IOException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CitrusMailMessageHeaders.MAIL_MESSAGE_ID, msg.getMimeMessage().getMessageID());
        headers.put(CitrusMailMessageHeaders.MAIL_FROM, StringUtils.arrayToCommaDelimitedString(msg.getMimeMessage().getFrom()));
        headers.put(CitrusMailMessageHeaders.MAIL_TO, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(Message.RecipientType.TO))));
        headers.put(CitrusMailMessageHeaders.MAIL_CC, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(Message.RecipientType.CC))));
        headers.put(CitrusMailMessageHeaders.MAIL_BCC, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(Message.RecipientType.BCC))));
        headers.put(CitrusMailMessageHeaders.MAIL_REPLY_TO, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getReplyTo())));
        headers.put(CitrusMailMessageHeaders.MAIL_DATE, msg.getMimeMessage().getSentDate() != null ? dateFormat.format(msg.getMimeMessage().getSentDate()) : null);
        headers.put(CitrusMailMessageHeaders.MAIL_SUBJECT, msg.getMimeMessage().getSubject());
        headers.put(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, parseContentType(msg.getMimeMessage().getContentType()));

        return headers;
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
     * Gets the mail message mapper.
     * @return
     */
    public XStream getMailMessageMapper() {
        return mailMessageMapper;
    }

    /**
     * Sets the mail message mapper.
     * @param mailMessageMapper
     */
    public void setMailMessageMapper(XStream mailMessageMapper) {
        this.mailMessageMapper = mailMessageMapper;
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
    public SMTPServer getSmtpServer() {
        return smtpServer;
    }

    /**
     * Sets the smtp server instance.
     * @param smtpServer
     */
    public void setSmtpServer(SMTPServer smtpServer) {
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
}
