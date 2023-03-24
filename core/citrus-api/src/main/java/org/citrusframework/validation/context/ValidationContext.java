/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.validation.context;


/**
 * Basic validation context holding validation specific information.
 *
 * @author Christoph Deppisch
 */
public interface ValidationContext {

    /**
     * Fluent builder
     * @param <T> context type
     * @param <B> builder reference to self
     */
    interface Builder<T extends ValidationContext, B extends Builder<T, B>> {

        /**
         * Builds new validation context instance.
         * @return the built context.
         */
        T build();
    }
}
