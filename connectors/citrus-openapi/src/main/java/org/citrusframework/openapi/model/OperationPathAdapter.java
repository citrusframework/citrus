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

package org.citrusframework.openapi.model;

import io.apicurio.datamodels.openapi.models.OasOperation;
import org.citrusframework.openapi.util.OpenApiUtils;

import static java.lang.String.format;

/**
 * Adapts the different paths associated with an OpenAPI operation to the {@link OasOperation}.
 * This record holds the API path, context path, full path, and the associated {@link OasOperation} object.
 *
 * @param apiPath     The API path for the operation.
 * @param contextPath The context path in which the API is rooted.
 * @param fullPath    The full path combining context path and API path.
 * @param operation   The {@link OasOperation} object representing the operation details.
 */
public record OperationPathAdapter(String apiPath, String contextPath, String fullPath, OasOperation operation, String uniqueOperationId) {

    @Override
    public String toString() {
        return format("%s (%s)",OpenApiUtils.getMethodPath(operation.getMethod(), apiPath), operation.operationId);
    }
}
