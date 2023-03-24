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

package org.citrusframework.groovy.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadGroovy() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/xml/groovy-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "GroovyTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);
        Assert.assertEquals(result.getTestAction(0).getClass(), GroovyAction.class);

        int actionIndex = 0;

        GroovyAction action = (GroovyAction) result.getTestAction(actionIndex++);
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:org/citrusframework/script/script-template.groovy");
        Assert.assertEquals(action.getScript().trim(), "println 'Hello Citrus'");

        action = (GroovyAction) result.getTestAction(actionIndex++);
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertNotNull(action.getScript());
        Assert.assertFalse(action.isUseScriptTemplate());

        action = (GroovyAction) result.getTestAction(actionIndex++);
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:org/citrusframework/script/custom-script-template.groovy");
        Assert.assertNotNull(action.getScript());

        action = (GroovyAction) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptResourcePath(), "classpath:org/citrusframework/script/example.groovy");
        Assert.assertNull(action.getScript());
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(XmlTestActionBuilder.lookup("groovy").isPresent());
        Assert.assertEquals(XmlTestActionBuilder.lookup("groovy").get().getClass(), Groovy.class);
    }

}
