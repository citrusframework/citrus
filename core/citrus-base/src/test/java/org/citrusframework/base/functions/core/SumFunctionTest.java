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

public class SumFunctionTest extends UnitTestSupport {
    SumFunction function = new SumFunction();

    @Test
    public void testFunction() {
        List<String> params = new ArrayList<>();
        params.add("3");
        params.add("5.3");
        params.add("4.7");
        params.add("0");

        Assertions.assertEquals("13.0", function.execute(params, context));
    }

    @Test
    public void testWrongParameterUsage() {
        assertThrows(NumberFormatException.class, () -> function.execute(singletonList("no digit"), context));
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(emptyList(), context));
    }
}
