package org.citrusframework.testapi;

/**
 * Interface representing a generated API request corresponding to an operation in an OpenAPI specification.
 * Provides methods to retrieve metadata about the request such as operation name, HTTP method, and path.
 */
public interface GeneratedApiRequest {

    /**
     * Retrieves the name of the OpenAPI operation associated with the request.
     *
     * @return the name of the OpenAPI operation
     */
    String getOperationName();

    /**
     * Retrieves the HTTP method used for the request.
     *
     * @return the HTTP method used for the request (e.g., GET, POST)
     */
    String getMethod();

    /**
     * Retrieves the path used for the request.
     *
     * @return the path used for the request
     */
    String getPath();
}
