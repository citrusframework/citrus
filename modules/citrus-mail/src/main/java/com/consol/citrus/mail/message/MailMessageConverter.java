/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.mail.message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.mail.client.MailEndpointConfiguration;
import com.consol.citrus.mail.model.*;
import com.consol.citrus.message.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.xml.transform.Source;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class MailMessageConverter implements MessageConverter<MimeMailMessage, MailEndpointConfiguration> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MailMessageConverter.class);

    /** Mail delivery date format */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public MimeMailMessage convertOutbound(Message message, MailEndpointConfiguration endpointConfiguration) {
        MailMessage mailMessage = getMailMessage(message, endpointConfiguration);

        try {
            MimeMessage mimeMessage = endpointConfiguration.getJavaMailSender().createMimeMessage();
            MimeMailMessage mimeMailMessage = new MimeMailMessage(new MimeMessageHelper(mimeMessage, mailMessage.getBody().hasAttachments(), mailMessage.getBody().getCharsetName()));

            convertOutbound(mimeMailMessage, new DefaultMessage(mailMessage, message.copyHeaders()), endpointConfiguration);

            return mimeMailMessage;
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to create mail mime message", e);
        }
    }

    @Override
    public void convertOutbound(MimeMailMessage mimeMailMessage, Message message, MailEndpointConfiguration endpointConfiguration) {
        MailMessage mailMessage = getMailMessage(message, endpointConfiguration);

        try {
            mimeMailMessage.setFrom(mailMessage.getFrom());
            mimeMailMessage.setTo(StringUtils.commaDelimitedListToStringArray(mailMessage.getTo()));

            if (StringUtils.hasText(mailMessage.getCc())) {
                mimeMailMessage.setCc(StringUtils.commaDelimitedListToStringArray(mailMessage.getCc()));
            }

            if (StringUtils.hasText(mailMessage.getBcc())) {
                mimeMailMessage.setBcc(StringUtils.commaDelimitedListToStringArray(mailMessage.getBcc()));
            }

            mimeMailMessage.setReplyTo(mailMessage.getReplyTo() != null ? mailMessage.getReplyTo() : mailMessage.getFrom());
            mimeMailMessage.setSentDate(new Date());
            mimeMailMessage.setSubject(mailMessage.getSubject());
            mimeMailMessage.setText(mailMessage.getBody().getContent());

            if (mailMessage.getBody().hasAttachments()) {
                for (AttachmentPart attachmentPart : mailMessage.getBody().getAttachments().getAttachments()) {
                    mimeMailMessage.getMimeMessageHelper().addAttachment(attachmentPart.getFileName(),
                            new ByteArrayResource(attachmentPart.getContent().getBytes(Charset.forName(attachmentPart.getCharsetName()))),
                            attachmentPart.getContentType());
                }
            }
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to create mail mime message", e);
        }
    }

    @Override
    public Message convertInbound(MimeMailMessage message, MailEndpointConfiguration endpointConfiguration) {
        try {
            Map<String, Object> messageHeaders = createMessageHeaders(message);
            MailMessage mailMessage = createMailMessage(messageHeaders);
            mailMessage.setBody(handlePart(message.getMimeMessage()));

            return new DefaultMessage(mailMessage, messageHeaders);
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to convert mail mime message", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to convert mail mime message", e);
        }
    }

    /**
     * Creates a new mail message model object from message headers.
     * @param messageHeaders
     * @return
     */
    protected MailMessage createMailMessage(Map<String, Object> messageHeaders) {
        MailMessage message = new MailMessage();
        message.setFrom(messageHeaders.get(CitrusMailMessageHeaders.MAIL_FROM).toString());
        message.setTo(messageHeaders.get(CitrusMailMessageHeaders.MAIL_TO).toString());
        message.setCc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC).toString());
        message.setBcc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC).toString());
        message.setSubject(messageHeaders.get(CitrusMailMessageHeaders.MAIL_SUBJECT).toString());
        return message;
    }

    /**
     * Reads basic message information such as sender, recipients and mail subject to message headers.
     * @param msg
     * @return
     */
    protected Map<String,Object> createMessageHeaders(MimeMailMessage msg) throws MessagingException, IOException {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(CitrusMailMessageHeaders.MAIL_MESSAGE_ID, msg.getMimeMessage().getMessageID());
        headers.put(CitrusMailMessageHeaders.MAIL_FROM, StringUtils.arrayToCommaDelimitedString(msg.getMimeMessage().getFrom()));
        headers.put(CitrusMailMessageHeaders.MAIL_TO, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(javax.mail.Message.RecipientType.TO))));
        headers.put(CitrusMailMessageHeaders.MAIL_CC, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(javax.mail.Message.RecipientType.CC))));
        headers.put(CitrusMailMessageHeaders.MAIL_BCC, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getRecipients(javax.mail.Message.RecipientType.BCC))));
        headers.put(CitrusMailMessageHeaders.MAIL_REPLY_TO, StringUtils.arrayToCommaDelimitedString((msg.getMimeMessage().getReplyTo())));
        headers.put(CitrusMailMessageHeaders.MAIL_DATE, msg.getMimeMessage().getSentDate() != null ? dateFormat.format(msg.getMimeMessage().getSentDate()) : null);
        headers.put(CitrusMailMessageHeaders.MAIL_SUBJECT, msg.getMimeMessage().getSubject());
        headers.put(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, parseContentType(msg.getMimeMessage().getContentType()));

        return headers;
    }

    /**
     * Process message part. Can be a text, binary or multipart instance.
     * @param part
     * @return
     * @throws java.io.IOException
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

                body.append(line);
                body.append(System.getProperty("line.separator"));
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
     * Reads Citrus internal mail message model object from message payload. Either payload is actually a mail message object or
     * XML payload String is unmarshalled to mail message object.
     *
     * @param message
     * @param endpointConfiguration
     * @return
     */
    private MailMessage getMailMessage(Message message, MailEndpointConfiguration endpointConfiguration) {
        Object payload = message.getPayload();

        MailMessage mailMessage = null;
        if (payload != null) {
            if (payload instanceof MailMessage) {
                mailMessage = (MailMessage) payload;
            } else {
                mailMessage = (MailMessage) endpointConfiguration.getMailMarshaller()
                        .unmarshal(message.getPayload(Source.class));
            }
        }

        if (mailMessage == null) {
            throw new CitrusRuntimeException("Unable to create proper mail message from paylaod: " + payload);
        }

        return mailMessage;
    }
}
