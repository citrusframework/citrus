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

package org.citrusframework.spring.boot.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;

/**
 * Composed annotation for Spring Boot based Citrus tests. Combines {@link SpringBootTest} with
 * {@link CitrusSpringSupport}, so a single annotation bootstraps the Spring Boot application context
 * and enables the Citrus JUnit Jupiter Spring extension.
 * <p>
 * Using {@link SpringBootTest} and {@link CitrusSpringSupport} separately remains fully supported.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@CitrusSpringSupport
public @interface CitrusSpringBootTest {

    /**
     * Alias for {@link #properties()}.
     * @return the properties to apply
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] value() default {};

    /**
     * Properties in form {@literal key=value} that should be added to the Spring
     * {@link org.springframework.core.env.Environment} before the test runs.
     * @return the properties to add
     * @see SpringBootTest#properties()
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
    String[] properties() default {};

    /**
     * Application arguments that should be passed to the application under test.
     * @return the application arguments
     * @see SpringBootTest#args()
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "args")
    String[] args() default {};

    /**
     * The component classes to use for loading the application context.
     * @return the component classes used to load the application context
     * @see SpringBootTest#classes()
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "classes")
    Class<?>[] classes() default {};

    /**
     * The type of web environment to create when applicable.
     * @return the type of web environment
     * @see SpringBootTest#webEnvironment()
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.MOCK;

    /**
     * The type of main method usage to employ when creating the application context.
     * @return the type of main method usage
     * @see SpringBootTest#useMainMethod()
     */
    @AliasFor(annotation = SpringBootTest.class, attribute = "useMainMethod")
    SpringBootTest.UseMainMethod useMainMethod() default SpringBootTest.UseMainMethod.NEVER;
}
