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
package com.consol.citrus.mail.adapter;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.model.*;
import com.consol.citrus.mail.model.BodyPart;
import com.consol.citrus.message.MessageHandler;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.subethamail.smtp.helper.SimpleMessageListener;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Mail handler adapter invokes message handler for each mail delivery. Adapter converts mail message content to
 * XML mail message representation. Test case can validate mail message using well known XML comparison in Citrus.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageHandlerAdapter implements SimpleMessageListener {

    /** Message handler invoke on mail delivery */
    private final MessageHandler messageHandler;

    /** XML message mapper */
    private XStream mailMessageMapper = new MailMessageMapper();

    /** Java mail session */
    private Session mailSession;

    /** Java mail properties */
    private Properties javaMailProperties = new Properties();

    /** Mail delivery date format */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageHandlerAdapter.class);

    /**
     * Default constructor using message handler implementation.
     * @param messageHandler
     */
    public MessageHandlerAdapter(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public boolean accept(String from, String recipient) {
        // by default accept all messages
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) {
        try {
            MimeMailMessage message = new MimeMailMessage(new MimeMessage(getSession(), data));
            Map<String, String> messageHeaders = createMessageHeaders(message);
            MailMessage mailMessage = createMailMessage(messageHeaders);
            mailMessage.setBody(handlePart(message.getMimeMessage()));

            invokeMessageHandler(mailMessage, messageHeaders);
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
    protected void invokeMessageHandler(MailMessage mailMessage, Map<String, String> messageHeaders) {
        messageHandler.handleMessage(org.springframework.integration.support.MessageBuilder
                .withPayload(mailMessageMapper.toXML(mailMessage))
                .copyHeaders(messageHeaders)
                .build());
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
     * Fixed Java mail strange behavior to include next line of text to content type.
     * @param contentType
     * @return
     * @throws IOException
     */
    private String parseContentType(String contentType) throws IOException {
        if (contentType.indexOf(System.getProperty("line.separator")) > 0) {
            BufferedReader reader = new BufferedReader(new StringReader(contentType));

            try {
                return reader.readLine();
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
     * Gets the message handler to invoke with mail message.
     * @return
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
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
}
