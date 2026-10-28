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

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RandomNumberFunctionTest extends UnitTestSupport {
    private final RandomNumberFunction function = new RandomNumberFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<>();
        params.add("3");

        Assertions.assertTrue(parseInt(function.execute(params, context)) < 1000);

        params = new ArrayList<>();
        params.add("3");
        params.add("false");

        String generated = function.execute(params, context);
        Assertions.assertTrue(generated.length() <= 3);
        Assertions.assertTrue(!generated.isEmpty());
    }

    @Test
    public void testLeadingZeroNumbers() {
        String generated = RandomNumberFunction.checkLeadingZeros("0001", true);
        Assertions.assertTrue(parseInt(generated.substring(0, 1)) > 0);

        generated = RandomNumberFunction.checkLeadingZeros("0009", true);
        Assertions.assertEquals(4, generated.length());

        generated = RandomNumberFunction.checkLeadingZeros("00000", true);
        Assertions.assertEquals(5, generated.length());
        Assertions.assertTrue(parseInt(generated.substring(0, 1)) > 0);
        Assertions.assertTrue(generated.endsWith("0000"));

        generated = RandomNumberFunction.checkLeadingZeros("009809", true);
        Assertions.assertEquals(6, generated.length());
        Assertions.assertTrue(parseInt(generated.substring(0, 1)) > 0);
        Assertions.assertTrue(generated.endsWith("09809"));

        generated = RandomNumberFunction.checkLeadingZeros("01209", true);
        Assertions.assertEquals(5, generated.length());
        Assertions.assertTrue(parseInt(generated.substring(0, 1)) > 0);
        Assertions.assertTrue(generated.endsWith("1209"));

        generated = RandomNumberFunction.checkLeadingZeros("1209", true);
        Assertions.assertEquals(4, generated.length());
        Assertions.assertEquals("1209", generated);

        generated = RandomNumberFunction.checkLeadingZeros("00000", false);
        Assertions.assertEquals(1, generated.length());
        Assertions.assertEquals("0", generated);

        generated = RandomNumberFunction.checkLeadingZeros("0009", false);
        Assertions.assertEquals(1, generated.length());
        Assertions.assertEquals("9", generated);

        generated = RandomNumberFunction.checkLeadingZeros("01209", false);
        Assertions.assertEquals(4, generated.length());
        Assertions.assertEquals("1209", generated);

        generated = RandomNumberFunction.checkLeadingZeros("1209", false);
        Assertions.assertEquals(4, generated.length());
        Assertions.assertEquals("1209", generated);
    }

    @Test
    public void testWrongParameterUsage() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(singletonList("-1"), context));
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(emptyList(), context));
    }

    @Test
    public void testTooManyParameters() {
        List<String> params = new ArrayList<>();
        params.add("3");
        params.add("true");
        params.add("too much");

        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(params, context));
    }
}
