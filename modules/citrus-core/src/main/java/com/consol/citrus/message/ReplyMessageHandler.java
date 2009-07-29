package com.consol.citrus.message;

import org.springframework.integration.core.Message;

public interface ReplyMessageHandler {
    void onReplyMessage(Message<?> replyMessage);
}
