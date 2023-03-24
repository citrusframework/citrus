/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XmlTestLoaderTest {

    @Test
    public void shouldApplyNamespace() {
        Assert.assertEquals(XmlTestLoader.applyNamespace(""), "");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test></test>"), "<test></test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test xmlns=\"foo\"></test>"), "<test xmlns=\"foo\"></test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test name=\"FooTest\" xmlns=\"foo\"></test>"), "<test name=\"FooTest\" xmlns=\"foo\"></test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test " + System.lineSeparator() + "    name=\"FooTest\" " + System.lineSeparator() + "    xmlns=\"foo\"></test>"),
                "<test " + System.lineSeparator() + "    name=\"FooTest\" " + System.lineSeparator() + "    xmlns=\"foo\"></test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test name=\"FooTest\" xmlns=\"foo\" status=\"FINAL\"></test>"), "<test name=\"FooTest\" xmlns=\"foo\" status=\"FINAL\"></test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<ns:test xmlns:ns=\"foo\"></ns:test>"), "<ns:test xmlns:ns=\"foo\"></ns:test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<ns1:test xmlns:ns1=\"foo\"></ns1:test>"), "<ns1:test xmlns:ns1=\"foo\"></ns1:test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<ns-1:test xmlns:ns-1=\"foo\"></ns-1:test>"), "<ns-1:test xmlns:ns-1=\"foo\"></ns-1:test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<ns:test " + System.lineSeparator() + "xmlns:ns=\"foo\"></ns:test>"), "<ns:test " + System.lineSeparator() + "xmlns:ns=\"foo\"></ns:test>");
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test name=\"FooTest\"></test>"), String.format("<test xmlns=\"%s\" name=\"FooTest\"></test>", XmlTestLoader.TEST_NS));
        Assert.assertEquals(XmlTestLoader.applyNamespace("<test " + System.lineSeparator() + "name=\"FooTest\"></test>"),
                String.format("<test xmlns=\"%s\" " + System.lineSeparator() + "name=\"FooTest\"></test>", XmlTestLoader.TEST_NS));
    }
}
