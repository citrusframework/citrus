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
 * Marks a {@code static final String} field as a Citrus configuration property key. The field's
 * compile-time constant value is used as the property name in the generated
 * {@code META-INF/spring-configuration-metadata.json}.
 *
 * <p>Example usage:</p>
 * <pre>
 * &#64;CitrusConfigProperty(
 *     description = "Enable message payload pretty printing",
 *     type = "java.lang.Boolean",
 *     defaultValue = "true"
 * )
 * public static final String PRETTY_PRINT_PROPERTY = "citrus.message.pretty.print";
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface CitrusConfigProperty {

    /**
     * Human-readable description of this configuration property.
     */
    String description() default "";

    /**
     * Fully-qualified Java type of the property value. Defaults to {@code "java.lang.String"}.
     */
    String type() default "java.lang.String";

    /**
     * Default value as a string. An empty string means no default.
     */
    String defaultValue() default "";

    /**
     * Whether this property is deprecated.
     */
    boolean deprecated() default false;

    /**
     * The replacement property key when this property is deprecated.
     */
    String replacement() default "";
}
