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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EncodeBase64FunctionTest extends UnitTestSupport {
    private final EncodeBase64Function function = new EncodeBase64Function();

    @Test
    public void testFunction() {
        Assertions.assertEquals("Zm9v", function.execute(Collections.singletonList("foo"), context));
    }

    @Test
    public void testCustomCharset() {
        Assertions.assertEquals("Zm9v", function.execute(Arrays.asList("foo", "UTF-8"), context));
    }

    @Test
    public void testUnsupportedCharset() {
        try {
            function.execute(Arrays.asList("foo", "UNKNOWN"), context);
            Assertions.fail("Missing exception due to unsupported charset encoding");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getCause().getClass().equals(UnsupportedEncodingException.class));
        }
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(Collections.emptyList(), context));
    }
}
