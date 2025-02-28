package org.citrusframework.openapi;

import org.citrusframework.message.MessageHeaders;

public class OpenApiMessageHeaders {

    public static final String OAS_PREFIX = MessageHeaders.PREFIX + "oas_";

    public static final String OAS_UNIQUE_OPERATION_ID = OAS_PREFIX + "unique_operation_id";

    public static final String OAS_SPECIFICATION_ID = OAS_PREFIX + "unique_specification_id";

    public static final String OAS_MESSAGE_TYPE = OAS_PREFIX + "message_type";

    public static final String RESPONSE_TYPE = OAS_PREFIX + "response";

    public static final String REQUEST_TYPE = OAS_PREFIX + "request";

    private OpenApiMessageHeaders() {
        // Static access only
    }
}
