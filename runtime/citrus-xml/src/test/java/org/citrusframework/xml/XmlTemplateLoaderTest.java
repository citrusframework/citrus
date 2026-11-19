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

public class XmlTemplateLoaderTest {

    @Test
    public void shouldApplyNamespace() {
        Assertions.assertEquals("", XmlTemplateLoader.applyNamespace(""));
        Assertions.assertEquals("<template name=\"foo\" xmlns=\"foo\"></template>", XmlTemplateLoader.applyNamespace("<template name=\"foo\" xmlns=\"foo\"></template>"));
        Assertions.assertEquals("<ns:template name=\"foo\" xmlns:ns=\"foo\"></ns:template>", XmlTemplateLoader.applyNamespace("<ns:template name=\"foo\" xmlns:ns=\"foo\"></ns:template>"));
        Assertions.assertEquals("<template " + System.lineSeparator() + "    name=\"foo\" " + System.lineSeparator() + "    xmlns=\"foo\"></template>", XmlTemplateLoader.applyNamespace("<template " + System.lineSeparator() + "    name=\"foo\" " + System.lineSeparator() + "    xmlns=\"foo\"></template>"));
        Assertions.assertEquals(String.format("<template xmlns=\"%s\" name=\"foo\"></template>", XmlTestLoader.TEST_NS), XmlTemplateLoader.applyNamespace("<template name=\"foo\"></template>"));
    }

    @Test
    public void shouldNotApplyNamespaceTwiceWhenTemplateHasXmlDeclaration() {
        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<template xmlns=\"foo\" name=\"foo\"></template>";
        Assertions.assertEquals(template, XmlTemplateLoader.applyNamespace(template));
    }

    @Test
    public void shouldNotApplyNamespaceTwiceWhenTemplateHasLeadingComment() {
        String template = "<!-- License header -->" + System.lineSeparator() +
                "<template xmlns=\"foo\" name=\"foo\"></template>";
        Assertions.assertEquals(template, XmlTemplateLoader.applyNamespace(template));
    }

    @Test
    public void shouldApplyNamespaceWhenOnlyNestedElementsDeclareNamespaces() {
        String template = "<!-- License header -->" + System.lineSeparator() +
                "<template name=\"foo\"><actions><send endpoint=\"foo\"><message><body><payload>" +
                "<Hello xmlns=\"http://citrusframework.org/hello\"/></payload></body></message></send></actions></template>";
        Assertions.assertEquals(template.replace("<template ", String.format("<template xmlns=\"%s\" ", XmlTestLoader.TEST_NS)),
                XmlTemplateLoader.applyNamespace(template));
    }

    @Test
    public void shouldApplyNamespaceWhenTemplateHasXmlDeclaration() {
        Assertions.assertEquals(String.format("<?xml version=\"1.0\"?><template xmlns=\"%s\" name=\"foo\"></template>", XmlTestLoader.TEST_NS),
                XmlTemplateLoader.applyNamespace("<?xml version=\"1.0\"?><template name=\"foo\"></template>"));
    }
}
