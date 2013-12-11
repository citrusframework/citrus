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

import com.consol.citrus.mail.message.CitrusMailMessageHeaders;
import com.consol.citrus.mail.model.AttachmentPart;
import com.consol.citrus.mail.model.BodyPart;
import com.consol.citrus.mail.model.MailMessage;
import com.consol.citrus.message.MessageHandler;
import org.springframework.integration.Message;

import java.util.Map;
import java.util.Stack;

/**
 * Message handler adapter splits mail message to multiple messages. Each message represents a mail part in
 * a multipart message. Message handler may be invoked several times respective for each mail part.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageSplittingHandlerAdapter extends MessageHandlerAdapter {
    /**
     * Default constructor using message handler implementation.
     *
     * @param messageHandler
     */
    public MessageSplittingHandlerAdapter(MessageHandler messageHandler) {
        super(messageHandler);
    }

    @Override
    protected Message<?> invokeMessageHandler(MailMessage mailMessage, Map<String, String> messageHeaders) {
        return split(mailMessage.getBody(), messageHeaders);
    }

    /**
     * Split mail message into several messages. Each body and each attachment results in separate message
     * invoked on message handler. Mail message response if any should be sent only once within test case.
     * However latest mail response sent by test case is returned, others are ignored.
     *
     * @param bodyPart
     * @param messageHeaders
     */
    private Message<?> split(BodyPart bodyPart, Map<String, String> messageHeaders) {
        MailMessage mailMessage = createMailMessage(messageHeaders);
        mailMessage.setBody(new BodyPart(bodyPart.getContent(), bodyPart.getContentType()));

        Stack<Message<?>> responseStack = new Stack<Message<?>>();
        if (bodyPart instanceof AttachmentPart) {
            fillStack(getMessageHandler().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(getMailMessageMapper().toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_CONTENT_TYPE, bodyPart.getContentType())
                    .setHeader(CitrusMailMessageHeaders.MAIL_FILENAME, ((AttachmentPart) bodyPart).getFileName())
                    .build()), responseStack);
        } else {
            fillStack(getMessageHandler().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(getMailMessageMapper().toXML(mailMessage))
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

    private void fillStack(Message<?> message, Stack<Message<?>> responseStack) {
        if (message != null) {
            responseStack.push(message);
        }
    }
}
