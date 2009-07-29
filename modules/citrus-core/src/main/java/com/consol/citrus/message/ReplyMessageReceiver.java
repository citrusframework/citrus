package com.consol.citrus.message;

import org.springframework.integration.core.Message;


public class ReplyMessageReceiver implements MessageReceiver, ReplyMessageHandler {

    private Message replyMessage = null;
    
    public Message<?> receive() {
        return replyMessage;
    }

    public Message<?> receive(long timeout) {
        return receive();
    }

    public Message<?> receiveSelected(String selector) {
        throw new UnsupportedOperationException(ReplyMessageReceiver.class + " does not support selected receiving of messages");
    }

    public Message<?> receiveSelected(String selector, long timeout) {
        return receiveSelected(selector);
    }

    public void onReplyMessage(Message<?> replyMessage) {
        this.replyMessage = replyMessage;
    }

    /**
     * @return the replyMessage
     */
    public Message getReplyMessage() {
        return replyMessage;
    }

}
