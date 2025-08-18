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

package org.citrusframework.validation.ws;

import java.util.List;

import org.citrusframework.validation.context.ValidationContext;

public interface SoapFaultValidationContextBuilder<T extends ValidationContext, B extends SoapFaultValidationContextBuilder<T, B, D>, D extends SoapFaultDetailValidationContextBuilder<?, ?>>
        extends ValidationContext.Builder<T, B> {

    /**
     * Creates a new detail and returns it for future configuration.
     */
    D detail();

    /**
     * Add fault detail validation context.
     */
    B detail(D detailValidationContext);

    /**
     * Add fault detail validation context.
     */
    B details(List<D> detailValidationContexts);

    /**
     * Add fault detail validation context.
     */
    B details(D... detailValidationContexts);
}
