/*
 * Copyright 2006-2018 the original author or authors.
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
 * Citrus test case annotation used for XML test case definition inside a unit test class.
 * Each method annotated with this annotation will result in a separate test execution.
 *
 * @deprecated in favor of using generic {@link CitrusTestSource} annotation
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CitrusXmlTest {

    /** Test name optional - by default method name is used as test name */
    String[] name() default {};

    /** Test package name optional - by default package of declaring test class is used */
    String packageName() default "";

    /** Test packages to scan for XML test case definitions */
    String[] packageScan() default {};

    /** Test sources optional list of file resources to load */
    String[] sources() default {};

}
