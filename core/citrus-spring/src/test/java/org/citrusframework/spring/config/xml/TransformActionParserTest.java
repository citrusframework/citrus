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

package org.citrusframework.spring.config.xml;

import org.citrusframework.actions.TransformAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransformActionParserTest extends AbstractActionParserTest<TransformAction> {

    @Test
    public void testTransformActionParser() {
        assertActionCount(2);
        assertActionClassAndName(TransformAction.class, "transform");

        TransformAction action = getNextTestActionFromTest();
        Assertions.assertEquals("result", action.getTargetVariable());
        Assertions.assertTrue(StringUtils.hasText(action.getXmlData()));
        Assertions.assertNull(action.getXmlResourcePath());
        Assertions.assertTrue(StringUtils.hasText(action.getXsltData()));
        Assertions.assertNull(action.getXsltResourcePath());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("result", action.getTargetVariable());
        Assertions.assertFalse(StringUtils.hasText(action.getXmlData()));
        Assertions.assertNotNull(action.getXmlResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/spring/actions/transform-source.xml", action.getXmlResourcePath());
        Assertions.assertFalse(StringUtils.hasText(action.getXsltData()));
        Assertions.assertNotNull(action.getXsltResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/spring/actions/transform.xslt", action.getXsltResourcePath());
    }
}
