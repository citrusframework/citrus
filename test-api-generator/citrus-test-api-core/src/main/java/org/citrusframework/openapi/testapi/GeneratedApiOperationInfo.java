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
 * Interface representing information about a generated API operation as defined in an OpenAPI
 * specification. It provides methods to access metadata related to the operation, including the
 * operation name, HTTP method, and request path.
 */
public interface GeneratedApiOperationInfo {

    /**
     * Retrieves the name of the OpenAPI operation.
     *
     * @return the name of the OpenAPI operation
     */
    String getOperationName();

    /**
     * Retrieves the HTTP method corresponding to the operation.
     *
     * @return the HTTP method specified in the OpenAPI operation
     */
    String getMethod();

    /**
     * Retrieves the path corresponding to the operation.
     *
     * @return the path of the OpenAPI operation
     */
    String getPath();
}
