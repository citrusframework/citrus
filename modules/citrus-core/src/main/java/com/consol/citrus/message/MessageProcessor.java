package com.consol.citrus.message;

import java.util.List;

import org.springframework.integration.core.Message;

/**
 * Message processor having 0-n message handlers that will take care of incoming messages.
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 *
 */
public interface MessageProcessor {
    Message processMessage(Message message);

    List getMessageHandler();

    void setMessageHandler(List messageHandler);

    MessageHandler getDefaultMessageHandler();

    void setDefaultMessageHandler(MessageHandler messageHandler);

    String getMatchElement();

    void setMatchElement(String xpath);
}
