package org.citrusframework.openapi;

/**
 * The {@code OpenApiMessageType} enum defines the types of OpenAPI messages,
 * specifically REQUEST and RESPONSE. Each type is associated with a specific
 * header name, which is used to identify the type of message in the OpenAPI
 * message headers.
 */
public enum OpenApiMessageType {

    REQUEST(OpenApiMessageHeaders.REQUEST_TYPE), RESPONSE(OpenApiMessageHeaders.RESPONSE_TYPE);

    private final String headerName;

    OpenApiMessageType(String headerName) {
        this.headerName = headerName;
    }

    public String toHeaderName() {
        return headerName;
    }
}
