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
import com.consol.citrus.message.MessageHandler;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.*;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Apache james mime4j mail message parser */
    private MessageBuilder messageBuilder = new DefaultMessageBuilder();

    /** XML message mapper */
    private XStream mailMessageMapper = new MailMessageMapper();

    /** Mail delivery date format */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

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
            Message message = messageBuilder.parseMessage(data);
            Map<String, String> messageHeaders = createMessageHeaders(message);
            MailMessage mailMessage = createMailMessage(messageHeaders);
            mailMessage.setBody(handlePart(message));

            invokeMessageHandler(mailMessage, messageHeaders);
        } catch (MimeException e) {
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
    protected BodyPart handlePart(Entity part) throws IOException {
        Body body = part.getBody();
        if (body instanceof TextBody) {
            return handleTextPart((TextBody) body, part.getMimeType());
        } else if (body instanceof BinaryBody) {
            return handleBinaryPart((BinaryBody) body, part.getMimeType());
        } else if (body instanceof Multipart) {
            return handleMultipart((Multipart) body);
        } else {
            throw new CitrusRuntimeException("Unsupported mail body part: " + body.getClass());
        }
    }

    /**
     * Construct multipart body with first part being the body content and further parts being the attachments.
     * @param body
     * @return
     * @throws IOException
     */
    private BodyPart handleMultipart(Multipart body) throws IOException {
        BodyPart bodyPart = null;
        for (Entity entity : body.getBodyParts()) {
            if (bodyPart == null) {
                bodyPart = handlePart(entity);
            } else {
                BodyPart attachment = handlePart(entity);
                bodyPart.addPart(new AttachmentPart(attachment.getContent(), attachment.getContentType(), entity.getFilename()));
            }
        }

        return bodyPart;
    }

    /**
     * Construct simple text body part.
     * @param body
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleBinaryPart(BinaryBody body, String contentType) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileCopyUtils.copy(body.getInputStream(), bos);
        String base64 = Base64.encodeBase64String(bos.toByteArray());

        return new BodyPart(base64, contentType);
    }

    /**
     * Construct simple binary body part with base64 data.
     * @param body
     * @param contentType
     * @return
     * @throws IOException
     */
    protected BodyPart handleTextPart(TextBody body, String contentType) throws IOException {
        String textBody = FileCopyUtils.copyToString(body.getReader());
        textBody = stripMailBodyEnding(textBody);

        return new BodyPart(textBody, contentType);
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

                body.append(line + System.lineSeparator());
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
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
    protected Map<String,String> createMessageHeaders(Message msg) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(CitrusMailMessageHeaders.MAIL_MESSAGE_ID, msg.getMessageId());
        headers.put(CitrusMailMessageHeaders.MAIL_FROM, extractMailboxes(msg.getFrom()));
        headers.put(CitrusMailMessageHeaders.MAIL_TO, extractAddresses(msg.getTo()));
        headers.put(CitrusMailMessageHeaders.MAIL_CC, extractAddresses(msg.getCc()));
        headers.put(CitrusMailMessageHeaders.MAIL_BCC, extractAddresses(msg.getBcc()));
        headers.put(CitrusMailMessageHeaders.MAIL_REPLY_TO, extractAddresses(msg.getReplyTo()));
        headers.put(CitrusMailMessageHeaders.MAIL_DATE, msg.getDate() != null ? dateFormat.format(msg.getDate()) : null);
        headers.put(CitrusMailMessageHeaders.MAIL_SUBJECT, msg.getSubject());
        headers.put(CitrusMailMessageHeaders.MAIL_MIME_TYPE, msg.getMimeType());

        return headers;
    }

    /**
     * Reads all mailboxes in list and return comma separated string.
     * @param mailboxList
     * @return
     */
    private String extractMailboxes(MailboxList mailboxList) {
        if (CollectionUtils.isEmpty(mailboxList)) {
            return "";
        }

        List<String> mailboxAdresses = new ArrayList<String>(mailboxList.size());
        for (Mailbox mailbox : mailboxList) {
            mailboxAdresses.add(mailbox.getAddress());
        }

        return StringUtils.arrayToCommaDelimitedString(mailboxAdresses.toArray(new String[mailboxList.size()]));
    }

    /**
     * Gets comma separated string of all addresses in list.
     * @param addressList
     * @return
     */
    private String extractAddresses(AddressList addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            return "";
        }

        List<String> adresses = new ArrayList<String>(addressList.size());
        for (Address address : addressList) {
            adresses.add(address.toString());
        }

        return StringUtils.arrayToCommaDelimitedString(adresses.toArray(new String[addressList.size()]));
    }

    /**
     * Gets the message handler to invoke with mail message.
     * @return
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Sets the message handler.
     * @return
     */
    public MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    /**
     * Sets the mail message builder.
     * @param messageBuilder
     */
    public void setMessageBuilder(MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
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
}
