package org.citrusframework.testapi;

import java.util.Map;

/**
 * Interface representing a generated API from an OpenAPI specification.
 * Provides methods to retrieve metadata about the API such as title, version,
 * prefix, and information extensions.
 */
public interface GeneratedApi {

    /**
     * Retrieves the title of the OpenAPI specification, as specified in the info section of the API.
     *
     * @return the title of the OpenAPI specification
     */
    String getApiTitle();

    /**
     * Retrieves the version of the OpenAPI specification, as specified in the info section of the API.
     *
     * @return the version of the OpenAPI specification
     */
    String getApiVersion();

    /**
     * Retrieves the prefix used for the API, as specified in the API generation configuration.
     *
     * @return the prefix used for the API
     */
    String getApiPrefix();

    /**
     * Retrieves the specification extensions of the OpenAPI defined in the "info" section.
     * <p>
     * Specification extensions, also known as vendor extensions, are custom key-value pairs used to describe extra
     * functionality not covered by the standard OpenAPI Specification. These properties start with "x-".
     * This method collects only the extensions defined in the "info" section of the API.
     * </p>
     *
     * @return a map containing the specification extensions defined in the "info" section of the API,
     *         where keys are extension names and values are extension values
     */
    Map<String, String> getApiInfoExtensions();
}