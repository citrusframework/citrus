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

package org.citrusframework.openapi;

/**
 * Adapter class that links an OAS entity to its associated OpenAPI specification context.
 * This class provides methods to access both the OpenAPI specification and the specific OAS entity.
 *
 * @param <T> the type to which the specification is adapted.
 */
public record OpenApiSpecificationAdapter<T>(OpenApiSpecification openApiSpecification, T entity) {

}
