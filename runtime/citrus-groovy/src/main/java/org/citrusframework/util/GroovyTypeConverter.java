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

package org.citrusframework.util;

import java.util.Optional;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.GStringImpl;

public final class GroovyTypeConverter extends DefaultTypeConverter {

    public static GroovyTypeConverter INSTANCE = new GroovyTypeConverter();

    /**
     * Private default constructor. Prevent instantiation users should use INSTANCE
     */
    private GroovyTypeConverter() {
    }

    @Override
    protected <T> Optional<T> convertBefore(Object target, Class<T> type) {
        if (GString.class.isAssignableFrom(type)) {
            return (Optional<T>) Optional.of(new GStringImpl(new Object[]{ target }, new String[] {"", ""}));
        } else if (GString.class.isAssignableFrom(target.getClass())) {
            return Optional.ofNullable(super.convertIfNecessary(((GString) target).toString(), type));
        }

        return Optional.empty();
    }

    @Override
    public <T> T convertStringToType(String value, Class<T> type) {
        if (GString.class.isAssignableFrom(type)) {
            return (T) new GStringImpl(new Object[]{ value }, new String[] {"", ""});
        }

        return super.convertStringToType(value, type);
    }
}
