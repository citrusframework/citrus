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

package org.citrusframework.groovy.util;

import org.codehaus.groovy.runtime.GStringImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroovyTypeConverterTest {

    private final GroovyTypeConverter converter = GroovyTypeConverter.INSTANCE;

    @Test
    public void testConverter() {
        Assertions.assertEquals("foo", converter.convertIfNecessary(new GStringImpl(new Object[]{ "foo" }, new String[]{ "", "" }), String.class));
        Assertions.assertEquals("the message is: foo!", converter.convertIfNecessary(new GStringImpl(new Object[]{ "foo" }, new String[]{ "the message is: ", "!" }), String.class));
    }
}
