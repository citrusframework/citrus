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

package org.citrusframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Citrus test case annotation used in Java DSL test cases to execute several tests within one single
 * test builder class. Each method annotated with this annotation will result in a separate test execution.
 *
 * @since 1.3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface CitrusTest {
    /** Test name optional -  by default method name is used as test name */
    String name() default "";
}
