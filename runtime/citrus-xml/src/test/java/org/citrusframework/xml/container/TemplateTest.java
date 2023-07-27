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

package org.citrusframework.xml.container;

import org.citrusframework.container.Template;
import org.citrusframework.xml.XmlTemplateLoader;
import org.citrusframework.xml.actions.AbstractXmlActionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TemplateTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadTemplate() {
        Template template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/xml/container/template-test.xml");

        Assert.assertEquals(template.getTemplateName(), "myTemplate");
        Assert.assertEquals(template.getName(), "template:myTemplate");
        Assert.assertEquals(template.getParameter().size(), 0);
        Assert.assertEquals(template.getActions().size(), 1);
        Assert.assertTrue(template.isGlobalContext());

        template = new XmlTemplateLoader()
                .withReferenceResolver(context.getReferenceResolver())
                .load("classpath:org/citrusframework/xml/container/template-parameters-test.xml");
        Assert.assertEquals(template.getTemplateName(), "myTemplate");
        Assert.assertEquals(template.getName(), "template:myTemplate");
        Assert.assertEquals(template.getParameter().size(), 3);
        Assert.assertEquals(template.getParameter().get("foo"), "");
        Assert.assertEquals(template.getParameter().get("bar"), "barValue");
        Assert.assertEquals(template.getParameter().get("baz"),
                "\n" +
                        "        foo\n" +
                        "        bar\n" +
                        "        baz\n" +
                        "      ");
        Assert.assertEquals(template.getActions().size(), 2);
        Assert.assertFalse(template.isGlobalContext());
    }
}
