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

package org.citrusframework.mail.message;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimePart;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.mail.client.MailEndpointConfiguration;
import org.citrusframework.mail.model.AttachmentPart;
import org.citrusframework.mail.model.BodyPart;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.model.MailRequest;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @author Christian Guggenmos
 * @since 2.0
 */
public class MailMessageConverter implements MessageConverter<MimeMailMessage, MimeMailMessage, MailEndpointConfiguration> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MailMessageConverter.class);

    /** Mail delivery date format */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public MimeMailMessage convertOutbound(Message message, MailEndpointConfiguration endpointConfiguration, TestContext context) {
        MailRequest mailMessage = getMailRequest(message, endpointConfiguration);

        try {
            Session session = Session.getInstance(endpointConfiguration.getJavaMailProperties(), endpointConfiguration.getAuthenticator());
            session.setDebug(logger.isDebugEnabled());

            MimeMessage mimeMessage = new MimeMessage(session);
            MimeMailMessage mimeMailMessage = new MimeMailMessage(new MimeMessageHelper(mimeMessage,
                    mailMessage.getBody().hasAttachments(),
                    parseCharsetFromContentType(mailMessage.getBody().getContentType())));

            convertOutbound(mimeMailMessage, new DefaultMessage(mailMessage, message.getHeaders()), endpointConfiguration, context);

            return mimeMailMessage;
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to create mail mime message", e);
        }
    }

    @Override
    public void convertOutbound(MimeMailMessage mimeMailMessage, Message message, MailEndpointConfiguration endpointConfiguration, TestContext context) {
        MailRequest mailRequest = getMailRequest(message, endpointConfiguration);

        try {
            mimeMailMessage.setFrom(mailRequest.getFrom());
            mimeMailMessage.setTo(mailRequest.getTo().split(","));

            if (StringUtils.hasText(mailRequest.getCc())) {
                mimeMailMessage.setCc(mailRequest.getCc().split(","));
            }

            if (StringUtils.hasText(mailRequest.getBcc())) {
                mimeMailMessage.setBcc(mailRequest.getBcc().split(","));
            }

            mimeMailMessage.setReplyTo(mailRequest.getReplyTo() != null ? mailRequest.getReplyTo() : mailRequest.getFrom());
            mimeMailMessage.setSentDate(new Date());
            mimeMailMessage.setSubject(mailRequest.getSubject());
            mimeMailMessage.setText(mailRequest.getBody().getContent());

            if (mailRequest.getBody().hasAttachments()) {
                for (AttachmentPart attachmentPart : mailRequest.getBody().getAttachments().getAttachments()) {
                    Resource attachmentSource = new ByteArrayResource(attachmentPart.getContent().getBytes(Charset.forName(parseCharsetFromContentType(attachmentPart.getContentType()))));
                    mimeMailMessage.getMimeMessageHelper().addAttachment(attachmentPart.getFileName(), attachmentSource,
                            attachmentPart.getContentType());
                }
            }
        } catch (MessagingException e) {
            throw new CitrusRuntimeException("Failed to create mail mime message", e);
        }
    }

    @Override
    public MailMessage convertInbound(MimeMailMessage message, MailEndpointConfiguration endpointConfiguration, TestContext context) {
        try {
            Map<String, Object> messageHeaders = createMessageHeaders(message);
            return createMailRequest(messageHeaders, handlePart(message.getMimeMessage()), endpointConfiguration.getMarshaller());
        } catch (MessagingException | IOException e) {
            throw new CitrusRuntimeException("Failed to convert mail mime message", e);
        }
    }

    /**
     * Creates a new mail message model object from message headers.
     * @param messageHeaders
     * @param bodyPart
     * @param marshaller
     * @return
     */
    public MailMessage createMailRequest(Map<String, Object> messageHeaders, BodyPart bodyPart, MailMarshaller marshaller) {
        MailMessage message = MailMessage.request(messageHeaders)
                        .marshaller(marshaller)
                        .from(messageHeaders.get(CitrusMailMessageHeaders.MAIL_FROM).toString())
                        .to(messageHeaders.get(CitrusMailMessageHeaders.MAIL_TO).toString())
                        .subject(messageHeaders.get(CitrusMailMessageHeaders.MAIL_SUBJECT).toString())
                        .body(bodyPart);

        if (StringUtils.hasText(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC).toString())) {
            message.cc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_CC).toString());
        }

        if (StringUtils.hasText(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC).toString())) {
            message.bcc(messageHeaders.get(CitrusMailMessageHeaders.MAIL_BCC).toString());
        }

        return message;
    }

    /**
     * Reads basic message information such as sender, recipients and mail subject to message headers.
     * @param msg
     * @return
     */
    protected Map<String,Object> createMessageHeaders(MimeMailMessage msg) throws MessagingException, IOException {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CitrusMailMessageHeaders.MAIL_MESSAGE_ID, msg.getMimeMessage().getMessageID());
        headers.put(CitrusMailMessageHeaders.MAIL_FROM, String.join(",", Optional.ofNullable(msg.getMimeMessage().getFrom()).stream().flatMap(Arrays::stream).map(Object::toString).toList()));
        headers.put(CitrusMailMessageHeaders.MAIL_TO, String.join(",", Optional.ofNullable(msg.getMimeMessage().getRecipients(jakarta.mail.Message.RecipientType.TO)).stream().flatMap(Arrays::stream).map(Object::toString).toList()));
        headers.put(CitrusMailMessageHeaders.MAIL_CC, String.join(",", Optional.ofNullable(msg.getMimeMessage().getRecipients(jakarta.mail.Message.RecipientType.CC)).stream().flatMap(Arrays::stream).map(Object::toString).toList()));
        headers.put(CitrusMailMessageHeaders.MAIL_BCC, String.join(",", Optional.ofNullable(msg.getMimeMessage().getRecipients(jakarta.mail.Message.RecipientType.BCC)).stream().flatMap(Arrays::stream).map(Object::toString).toList()));
        headers.put(CitrusMailMessageHeaders.MAIL_REPLY_TO, String.join(",", Optional.ofNullable(msg.getMimeMessage().getReplyTo()).stream().flatMap(Arrays::stream).map(Object::toString).toList()));
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
        try (InputStream in = image.getInputStream()) {
            bos.write(in.readAllBytes());
            bos.flush();
        }
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

        String content;
        if (textPart.getContent() instanceof String) {
            content = (String) textPart.getContent();
        } else if (textPart.getContent() instanceof InputStream) {
            content = FileUtils.readToString((InputStream) textPart.getContent(), Charset.forName(parseCharsetFromContentType(contentType)));
        } else {
            throw new CitrusRuntimeException("Cannot handle text content of type: " + textPart.getContent().getClass().toString());
        }

        return new BodyPart(stripMailBodyEnding(content), contentType);
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
            while (line != null && !line.equals(".")) {
                body.append(line);
                body.append(System.getProperty("line.separator"));
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("Failed to close reader", e);
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
    private MailRequest getMailRequest(Message message, MailEndpointConfiguration endpointConfiguration) {
        Object payload = message.getPayload();

        MailRequest mailRequest = null;
        if (payload != null) {
            if (payload instanceof MailRequest) {
                mailRequest = (MailRequest) payload;
            } else {
                mailRequest = (MailRequest) endpointConfiguration.getMarshaller()
                        .unmarshal(message.getPayload(Source.class));
            }
        }

        if (mailRequest == null) {
            throw new CitrusRuntimeException("Unable to create proper mail message from payload: " + payload);
        }

        return mailRequest;
    }

    /**
     * When content type has multiple lines this method just returns plain content type information in first line.
     * This is the case when multipart mixed content type has boundary information in next line.
     * @param contentType
     * @return
     * @throws IOException
     */
    static String parseContentType(String contentType) throws IOException {
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
                    logger.warn("Failed to close reader", e);
                }
            }
        }

        return contentType;
    }

    /**
     * Parses the charset definition from a "Content-Type" header value, e.g. text/plain; charset=UTF-8, and returns it exclusively.
     * @param contentType 'Content-Type' header value as String
     * @return a charset information parsed from the Content-Type, or {@link CitrusSettings#CITRUS_FILE_ENCODING} as default if there is no charset definition
     */
    static String parseCharsetFromContentType(String contentType) {
        final String charsetPrefix = "charset=";
        if (org.apache.commons.lang3.StringUtils.contains(contentType, charsetPrefix)) {
            String charsetName = org.apache.commons.lang3.StringUtils.substringAfter(contentType, charsetPrefix);
            return org.apache.commons.lang3.StringUtils.substringBefore(charsetName, ";");
        } else {
            return CitrusSettings.CITRUS_FILE_ENCODING;
        }
    }
}
