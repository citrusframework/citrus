/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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