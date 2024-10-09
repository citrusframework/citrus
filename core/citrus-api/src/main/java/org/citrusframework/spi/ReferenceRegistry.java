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

/**
 * Bind objects to registry for later reference. Objects declared in registry can be injected in various ways (e.g. annotations).
 * Usually used in combination with {@link org.citrusframework.spi.ReferenceResolver}.
 *
 */
@FunctionalInterface
public interface ReferenceRegistry {

    void bind(String name, Object value);

    /**
     * Get proper bean name for future bind operation on registry.
     * @param bindAnnotation
     * @param defaultName
     * @return
     */
    static String getName(BindToRegistry bindAnnotation, String defaultName) {
        if (bindAnnotation.name() != null && !bindAnnotation.name().isBlank()) {
            return bindAnnotation.name();
        }

        return defaultName;
    }
}
