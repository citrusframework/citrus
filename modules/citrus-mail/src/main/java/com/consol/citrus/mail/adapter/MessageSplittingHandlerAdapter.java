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
import com.consol.citrus.mail.model.*;
import com.consol.citrus.message.MessageHandler;

import java.util.Map;

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
    protected void invokeMessageHandler(MailMessage mailMessage, Map<String, String> messageHeaders) {
        split(mailMessage.getBody(), messageHeaders);
    }

    /**
     * Split mail message into several messages. Each body and each attachment results in separate message
     * invoked on message handler.
     *
     * @param bodyPart
     * @param messageHeaders
     */
    private void split(BodyPart bodyPart, Map<String, String> messageHeaders) {
        MailMessage mailMessage = createMailMessage(messageHeaders);
        mailMessage.setBody(new BodyPart(bodyPart.getContent(), bodyPart.getContentType()));

        if (bodyPart instanceof AttachmentPart) {
            getMessageHandler().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(getMailMessageMapper().toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_MIME_TYPE, bodyPart.getContentType())
                    .setHeader(CitrusMailMessageHeaders.MAIL_FILENAME, ((AttachmentPart) bodyPart).getFileName())
                    .build());
        } else {
            getMessageHandler().handleMessage(org.springframework.integration.support.MessageBuilder
                    .withPayload(getMailMessageMapper().toXML(mailMessage))
                    .copyHeaders(messageHeaders)
                    .setHeader(CitrusMailMessageHeaders.MAIL_MIME_TYPE, bodyPart.getContentType())
                    .build());
        }

        if (bodyPart.hasAttachments()) {
            for (AttachmentPart attachmentPart : bodyPart.getAttachments()) {
                split(attachmentPart, messageHeaders);
            }
        }
    }
}
