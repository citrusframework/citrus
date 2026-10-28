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

package org.citrusframework.base.functions.core;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubstringFunctionTest extends UnitTestSupport {
    SubstringFunction function = new SubstringFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<>();
        params.add("Hallo,TestFramework");
        params.add("6");
        Assertions.assertEquals("TestFramework", function.execute(params, context));

        params.clear();
        params.add("This is a test");
        params.add("0");
        Assertions.assertEquals("This is a test", function.execute(params, context));
    }

    @Test
    public void testEndIndex() {
        List<String> params = new ArrayList<>();
        params.add("Hallo,TestFramework");
        params.add("6");
        params.add("10");
        Assertions.assertEquals("Test", function.execute(params, context));

        params.clear();
        params.add("This is a test");
        params.add("0");
        params.add("4");
        Assertions.assertEquals("This", function.execute(params, context));
    }

    @Test
    public void testIndexOutOfBounds() {
        List<String> params = new ArrayList<>();
        params.add("Test");
        params.add("-1");
        assertThrows(StringIndexOutOfBoundsException.class, () -> function.execute(params, context));
    }

    @Test
    public void testMissingBeginIndex() {
        assertThrows(InvalidFunctionUsageException.class, () -> {
            function.execute(singletonList("This is a test"), context);
        });
    }

    @Test
    public void testNotANumber() {
        List<String> params = new ArrayList<>();
        params.add("Hallo,TestFramework");
        params.add("one");
        assertThrows(NumberFormatException.class, () -> function.execute(params, context));
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(emptyList(), context));
    }
}
