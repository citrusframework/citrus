package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.dsl.util.PositionHandle;
import com.consol.citrus.http.message.CitrusHttpMessageHeaders;
import org.springframework.http.HttpMethod;

/**
 * Special method for HTTP senders. This definition is used to set the Cirtrus special headers with special
 * meanings in a type safe manner.
 *
 * @author roland
 * @since 10.03.14
 */
public class SendHttpMessageActionDefinition extends SendMessageActionDefinition {

    /**
     * Constructor delegating to the parent constructor
     *
     * @param action action defined by this definiton
     * @param positionHandle position within the list of test actions.
     */
    public SendHttpMessageActionDefinition(SendMessageAction action, PositionHandle positionHandle) {
        super(action, positionHandle);
    }

    /**
     * Set the method of the request (GET, POST, ...)
     *
     * @param method method to set
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition method(HttpMethod method) {
        header(CitrusHttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Set the endpoint URI for the request
     *
     * @param uri absolute URI to use for the endpoint
     * @return chained definition builder
     */
    public SendHttpMessageActionDefinition uri(String uri) {
        header(CitrusHttpMessageHeaders.HTTP_REQUEST_URI,uri);
        return this;
    }

    // TODO: Add all other special constants. However, there are still constants which doesnt have
    // a meaning when used during sending (like HTTP_QUERY_PARAMS). This should be added as functionality
    // to the HTTP client itself.

}
