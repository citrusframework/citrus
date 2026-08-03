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

package org.citrusframework.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a named service implementation for the Citrus SPI mechanism. When the
 * {@link ResourcePathTypeResolver} is configured with a service type, it uses Java's
 * {@link java.util.ServiceLoader} as a fallback for discovering implementations. The
 * {@code name} attribute identifies the service so it can be looked up by name, similar
 * to the resource path file name used in the traditional resource path based lookup.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface NamedService {

    /**
     * The name of this service implementation. Used to match against the requested name in
     * {@code lookup(String name)} style calls on Citrus SPI interfaces.
     */
    String name();
}
