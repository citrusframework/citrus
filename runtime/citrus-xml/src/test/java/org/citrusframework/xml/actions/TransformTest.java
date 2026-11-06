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

package org.citrusframework.xml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.TransformAction;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransformTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTransform() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/transform.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("TransformTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(TransformAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        TransformAction action = (TransformAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("result", action.getTargetVariable());
        Assertions.assertTrue(StringUtils.hasText(action.getXmlData()));
        Assertions.assertNull(action.getXmlResourcePath());
        Assertions.assertTrue(StringUtils.hasText(action.getXsltData()));
        Assertions.assertNull(action.getXsltResourcePath());

        Assertions.assertTrue(context.getVariable("result").contains("<p>Message: Hello World!</p>"));

        action = (TransformAction) result.getTestAction(actionIndex);
        Assertions.assertEquals("transform-result", action.getTargetVariable());
        Assertions.assertFalse(StringUtils.hasText(action.getXmlData()));
        Assertions.assertNotNull(action.getXmlResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/xml/transform-source.xml", action.getXmlResourcePath());
        Assertions.assertFalse(StringUtils.hasText(action.getXsltData()));
        Assertions.assertNotNull(action.getXsltResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/xml/transform.xslt", action.getXsltResourcePath());

        Assertions.assertTrue(context.getVariable("transform-result").contains("<p>Message: Hello World!</p>"));
    }
}
