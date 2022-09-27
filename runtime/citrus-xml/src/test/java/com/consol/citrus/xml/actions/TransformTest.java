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

package com.consol.citrus.xml.actions;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.TransformAction;
import com.consol.citrus.xml.XmlTestLoader;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TransformTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTransform() {
        XmlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/xml/actions/transform-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "TransformTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), TransformAction.class);

        int actionIndex = 0;

        TransformAction action = (TransformAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTargetVariable(), "result");
        Assert.assertTrue(StringUtils.hasText(action.getXmlData()));
        Assert.assertNull(action.getXmlResourcePath());
        Assert.assertTrue(StringUtils.hasText(action.getXsltData()));
        Assert.assertNull(action.getXsltResourcePath());

        Assert.assertTrue(context.getVariable("result").contains("<p>Message: Hello World!</p>"));

        action = (TransformAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getTargetVariable(), "transform-result");
        Assert.assertFalse(StringUtils.hasText(action.getXmlData()));
        Assert.assertNotNull(action.getXmlResourcePath());
        Assert.assertEquals(action.getXmlResourcePath(), "classpath:com/consol/citrus/xml/transform-source.xml");
        Assert.assertFalse(StringUtils.hasText(action.getXsltData()));
        Assert.assertNotNull(action.getXsltResourcePath());
        Assert.assertEquals(action.getXsltResourcePath(), "classpath:com/consol/citrus/xml/transform.xslt");

        Assert.assertTrue(context.getVariable("transform-result").contains("<p>Message: Hello World!</p>"));
    }

}
