package org.citrusframework.report;

/**
 * @author Christoph Deppisch
 */
public interface MessageListenerAware {

    /**
     * Adds a new message listener.
     * @param listener
     */
    void addMessageListener(MessageListener listener);
}
