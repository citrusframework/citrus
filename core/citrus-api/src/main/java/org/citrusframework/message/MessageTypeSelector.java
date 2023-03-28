package org.citrusframework.message;

/**
 * @author Christoph Deppisch
 */
public interface MessageTypeSelector {

    /**
     * Checks if this message processor is capable of handling the given message type.
     *
     * @param messageType the message type representation as String (e.g. xml, json, csv, plaintext).
     * @return true if this component supports the message type.
     */
    boolean supportsMessageType(String messageType);
}
