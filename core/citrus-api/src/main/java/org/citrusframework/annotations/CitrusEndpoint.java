/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.annotations;

import java.lang.annotation.*;

/**
 * Marks field or parameter for endpoint injection.
 * @author Christoph Deppisch
 * @since 2.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface CitrusEndpoint {

    /**
     * Endpoint name usually referencing a Spring bean id.
     * @return
     */
    String name() default "";

    /**
     * Endpoint properties.
     * @return
     */
    CitrusEndpointProperty[] properties() default {};
}
