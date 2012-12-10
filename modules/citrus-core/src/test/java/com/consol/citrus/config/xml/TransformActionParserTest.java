/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class TransformActionParserTest extends AbstractActionParserTest<TransformAction> {

    @Test
    public void testTransformActionParser() {
        assertActionCount(2);
        assertActionClassAndName(TransformAction.class, "transform");
        
        TransformAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTargetVariable(), "result");
        Assert.assertTrue(StringUtils.hasText(action.getXmlData()));
        Assert.assertNull(action.getXmlResource());
        Assert.assertTrue(StringUtils.hasText(action.getXsltData()));
        Assert.assertNull(action.getXsltResource());
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getTargetVariable(), "result");
        Assert.assertFalse(StringUtils.hasText(action.getXmlData()));
        Assert.assertNotNull(action.getXmlResource());
        Assert.assertEquals(action.getXmlResource(), "classpath:com/consol/citrus/actions/transform-source.xml");
        Assert.assertFalse(StringUtils.hasText(action.getXsltData()));
        Assert.assertNotNull(action.getXsltResource());
        Assert.assertEquals(action.getXsltResource(), "classpath:com/consol/citrus/actions/transform.xslt");
    }
}
