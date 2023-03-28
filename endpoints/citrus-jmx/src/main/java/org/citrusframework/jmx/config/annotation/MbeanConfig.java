/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.jmx.config.annotation;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public @interface MbeanConfig {

    /**
     * Mbean type.
     * @return
     */
    Class type() default Object.class;

    /**
     * Mbean name.
     * @return
     */
    String name() default "";

    /**
     * Mbean object domain
     * @return
     */
    String objectDomain() default "";

    /**
     * Mbean object name
     * @return
     */
    String objectName() default "";

    /**
     * Mbean operations.
     * @return
     */
    MbeanOperation[] operations() default {};

    /**
     * Mbean attributes.
     * @return
     */
    MbeanAttribute[] attributes() default {};
}
