/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.spi;

import java.util.List;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public interface ReferenceResolver {

    /**
     * Resolve all references of type and given names. When no names are provided method resolves all
     * available references of given type regardless of their names.
     * @param type
     * @param names
     * @param <T>
     * @return
     */
    <T> List<T> resolve(Class<T> type, String... names);

    /**
     * Resolve reference of type.
     * @param type
     * @param <T>
     * @return
     */
    <T> T resolve(Class<T> type);

    /**
     * Resolve reference of type and name.
     * @param name
     * @param type
     * @param <T>
     * @return
     */
    <T> T resolve(String name, Class<T> type);

    /**
     * Resolve all references of type and given names.
     * @param names
     * @param type
     * @param <T>
     * @return
     */
    <T> List<T> resolve(String[] names, Class<T> type);

    /**
     * Resolves all references of given type returning a map of names and type instances.
     * @param type
     * @param <T>
     * @return
     */
    <T> Map<String, T> resolveAll(Class<T> type);

    /**
     * Checks if this reference name is resolvable.
     * @param name
     * @return
     */
    boolean isResolvable(String name);

    /**
     * Resolves reference by given name to any object.
     * @param name
     * @return
     */
    Object resolve(String name);
}
