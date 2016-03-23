/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.endpoint;

import com.consol.citrus.annotations.CitrusEndpoint;

/**
 * Endpoint builder interface. All endpoint builder implementations do implement this interface
 * in order to build endpoints using a fluent Java API.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface EndpointBuilder<T extends Endpoint> {

    /**
     * Builds the endpoint.
     * @return
     */
    T build();

    /**
     * Builds the endpoint from given endpoint annotations.
     * @param endpointAnnotation
     * @return
     */
    T build(CitrusEndpoint endpointAnnotation);
}
