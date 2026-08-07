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

package org.citrusframework.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a settings class as a source of Citrus configuration properties. Used together with
 * {@link CitrusConfigProperty} on individual fields to generate IDE-consumable metadata
 * ({@code META-INF/spring-configuration-metadata.json}) at build time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface CitrusConfigProperties {

    /**
     * Property key prefix shared by all properties in this class (e.g. {@code "citrus.openapi"}).
     */
    String prefix();

    /**
     * Human-readable description of this property group.
     */
    String description() default "";
}
