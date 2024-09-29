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

package org.citrusframework.openapi.testapi;

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
