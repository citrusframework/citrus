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

package org.citrusframework.xml.functions.core;

import java.util.Collections;

import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.base.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CreateCDataSectionFunctionTest extends AbstractUnitTest {
    CreateCDataSectionFunction function = new CreateCDataSectionFunction();

    @Test
    public void testFunction() {
        String xmlFragment = "<foo><bar>I like Citrus!</bar></foo>";
        String resultXml = "<![CDATA[<foo><bar>I like Citrus!</bar></foo>]]>";
        Assertions.assertEquals(resultXml, function.execute(Collections.singletonList(xmlFragment), context));
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(Collections.<String>emptyList(), context));
    }

    @Test
    public void shouldLookupFunction() {
        Assertions.assertTrue(Function.lookup().containsKey("cdataSection"));
        Assertions.assertEquals(CreateCDataSectionFunction.class, Function.lookup().get("cdataSection").getClass());

        Assertions.assertEquals(CreateCDataSectionFunction.class, new DefaultFunctionLibrary().getFunction("cdataSection").getClass());
    }
}
