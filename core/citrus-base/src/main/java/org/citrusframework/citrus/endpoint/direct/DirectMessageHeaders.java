package org.citrusframework.citrus.endpoint.direct;

import org.citrusframework.citrus.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 */
public class DirectMessageHeaders {

    /** Message reply queue name header */
    public static final String REPLY_QUEUE = MessageHeaders.PREFIX + "reply_queue";

    /**
     * Prevent instantiation of utility class.
     */
    private DirectMessageHeaders() {
        // utility class
    }
}
