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

package org.citrusframework.yaml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema property annotation, helps to generate Json schema for the full Citrus YAML DSL.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SchemaProperty {

    /**
     * The property kind.
     */
    Kind kind() default Kind.PROPERTY;

    /**
     * Schema group reference.
     */
    String group() default "";

    /**
     * Name of the property
     */
    String name() default "";

    /**
     * Title of the property
     */
    String title() default "";

    /**
     * Description of the property
     */
    String description() default "";

    /**
     * Format for String properties
     */
    TypeFormat format() default TypeFormat.NONE;

    /**
     * Default value of the property
     */
    String defaultValue() default "";

    /**
     * MultipleOf value for number properties
     */
    double multipleOf() default 0;

    /**
     * Minimum value for number properties
     */
    double min() default Double.MIN_VALUE;

    /**
     * Exclusive minimum value for number properties
     */
    boolean exclusiveMin() default false;

    /**
     * Maximum value for number properties
     */
    double max() default Double.MAX_VALUE;

    /**
     * Exclusive maximum value for number properties
     */
    boolean exclusiveMax() default false;

    /**
     * Minimum length for String properties
     */
    int minLength() default 0;

    /**
     * Maximum length for String properties
     */
    int maxLength() default Integer.MAX_VALUE;

    /**
     * Pattern for String properties
     */
    String pattern() default "";

    /**
     * Required properties
     */
    boolean required() default false;

    /**
     * Ignore field/ method during the process of generation of json schema
     */
    boolean ignore() default false;

    /**
     * Marks property as an advanced setting. Advanced settings may get displayed in a designer only on demand.
     */
    boolean advanced() default false;

    /**
     * Additional meta data (key-value pairs) for properties
     */
    MetaData[] metadata() default {};

    @interface MetaData {
        String key() default "";
        String value() default "";
    }

    enum Kind {
        PROPERTY,
        ACTION,
        CONTAINER,
        ENDPOINT,
        FUNCTION,
        VALIDATION_MATCHER,
        GROUP;

        public String getCatalogKind() {
            return switch (this) {
                case PROPERTY -> "";
                case ACTION -> "testAction";
                case CONTAINER -> "testContainer";
                case ENDPOINT -> "testEndpoint";
                case FUNCTION -> "testFunction";
                case VALIDATION_MATCHER -> "testValidationMatcher";
                case GROUP -> "testActionGroup";
            };
        }
    }

    enum TypeFormat {
        NONE,
        STRING,
        NUMBER,
        BOOLEAN,
    }
}
