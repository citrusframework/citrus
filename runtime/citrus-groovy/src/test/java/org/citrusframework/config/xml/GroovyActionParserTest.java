/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.config.xml;

import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.script.GroovyAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GroovyActionParserTest extends AbstractActionParserTest<GroovyAction> {

    @Test
    public void testActionParser() {
        assertActionCount(4);
        assertActionClassAndName(GroovyAction.class, "groovy");

        GroovyAction action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:org/citrusframework/script/script-template.groovy");
        Assert.assertEquals(action.getScript().trim(), "println 'Hello Citrus'");

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertNotNull(action.getScript());
        Assert.assertFalse(action.isUseScriptTemplate());

        action = getNextTestActionFromTest();
        Assert.assertNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptTemplatePath(), "classpath:org/citrusframework/script/custom-script-template.groovy");
        Assert.assertNotNull(action.getScript());

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getScriptResourcePath());
        Assert.assertEquals(action.getScriptResourcePath(), "classpath:org/citrusframework/script/example.groovy");
        Assert.assertNull(action.getScript());
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assert.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("groovy"));
        Assert.assertEquals(CitrusNamespaceParserRegistry.lookupBeanParser().get("groovy").getClass(), GroovyActionParser.class);

        Assert.assertEquals(CitrusNamespaceParserRegistry.getBeanParser("groovy").getClass(), GroovyActionParser.class);
    }
}
