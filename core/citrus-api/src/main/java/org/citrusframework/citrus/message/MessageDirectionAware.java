package org.citrusframework.citrus.message;

/**
 * @author Christoph Deppisch
 */
public interface MessageDirectionAware {

    /**
     * Indicates the direction of messages this processor should apply to.
     * @return
     */
    MessageDirection getDirection();
}
