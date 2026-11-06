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

package org.citrusframework.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class XmlTestLoaderTest {

    @Test
    public void shouldApplyNamespace() {
        Assertions.assertEquals("", XmlTestLoader.applyNamespace(""));
        Assertions.assertEquals("<test></test>", XmlTestLoader.applyNamespace("<test></test>"));
        Assertions.assertEquals("<test xmlns=\"foo\"></test>", XmlTestLoader.applyNamespace("<test xmlns=\"foo\"></test>"));
        Assertions.assertEquals("<test name=\"FooTest\" xmlns=\"foo\"></test>", XmlTestLoader.applyNamespace("<test name=\"FooTest\" xmlns=\"foo\"></test>"));
        Assertions.assertEquals("<test " + System.lineSeparator() + "    name=\"FooTest\" " + System.lineSeparator() + "    xmlns=\"foo\"></test>", XmlTestLoader.applyNamespace("<test " + System.lineSeparator() + "    name=\"FooTest\" " + System.lineSeparator() + "    xmlns=\"foo\"></test>"));
        Assertions.assertEquals("<test name=\"FooTest\" xmlns=\"foo\" status=\"FINAL\"></test>", XmlTestLoader.applyNamespace("<test name=\"FooTest\" xmlns=\"foo\" status=\"FINAL\"></test>"));
        Assertions.assertEquals("<ns:test xmlns:ns=\"foo\"></ns:test>", XmlTestLoader.applyNamespace("<ns:test xmlns:ns=\"foo\"></ns:test>"));
        Assertions.assertEquals("<ns1:test xmlns:ns1=\"foo\"></ns1:test>", XmlTestLoader.applyNamespace("<ns1:test xmlns:ns1=\"foo\"></ns1:test>"));
        Assertions.assertEquals("<ns-1:test xmlns:ns-1=\"foo\"></ns-1:test>", XmlTestLoader.applyNamespace("<ns-1:test xmlns:ns-1=\"foo\"></ns-1:test>"));
        Assertions.assertEquals("<ns:test " + System.lineSeparator() + "xmlns:ns=\"foo\"></ns:test>", XmlTestLoader.applyNamespace("<ns:test " + System.lineSeparator() + "xmlns:ns=\"foo\"></ns:test>"));
        Assertions.assertEquals(String.format("<test xmlns=\"%s\" name=\"FooTest\"></test>", XmlTestLoader.TEST_NS), XmlTestLoader.applyNamespace("<test name=\"FooTest\"></test>"));
        Assertions.assertEquals(String.format("<test xmlns=\"%s\" " + System.lineSeparator() + "name=\"FooTest\"></test>", XmlTestLoader.TEST_NS), XmlTestLoader.applyNamespace("<test " + System.lineSeparator() + "name=\"FooTest\"></test>"));
    }
}
