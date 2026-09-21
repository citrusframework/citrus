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

package org.citrusframework.playwright.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HTTP basic auth credential scoped to a request origin.
 *
 * <p>Several credentials can be declared on a browser endpoint. The first entry whose origin
 * matches the request is used; an entry without an origin matches any request and therefore acts
 * as a fallback.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface HttpCredential {

    /**
     * Request origin this credential applies to, for example {@code https://api.example.com}.
     * An empty origin matches any request.
     * @return request origin
     */
    String origin() default "";

    /**
     * Basic auth user name.
     * @return user name
     */
    String username();

    /**
     * Basic auth password.
     * @return password
     */
    String password();
}
