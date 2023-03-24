/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.config.xml;

import org.apache.tools.ant.DefaultLogger;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.citrusframework.actions.AntRunAction;
import org.citrusframework.testng.AbstractActionParserTest;

/**
 * @author Christoph Deppisch
 */
public class AntRunActionParserTest extends AbstractActionParserTest<AntRunAction> {

    @Test
    public void testAntRunActionParser() {
        assertActionCount(4);
        assertActionClassAndName(AntRunAction.class, "antrun");
        
        AntRunAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/actions/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/actions/build.xml");
        Assert.assertNull(action.getTarget());
        Assert.assertEquals(action.getTargets(), "sayHello,sayGoodbye");
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/actions/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 2L);
        Assert.assertEquals(action.getProperties().get("welcomeText"), "Hello World!");
        Assert.assertEquals(action.getProperties().get("goodbyeText"), "Goodbye!");
        Assert.assertNull(action.getPropertyFilePath());
        Assert.assertNull(action.getBuildListener());
        
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getBuildFilePath(), "classpath:org/citrusframework/actions/build.xml");
        Assert.assertEquals(action.getTarget(), "sayHello");
        Assert.assertNull(action.getTargets());
        Assert.assertEquals(action.getProperties().size(), 0L);
        Assert.assertEquals(action.getPropertyFilePath(), "classpath:org/citrusframework/actions/build.properties");
        Assert.assertNotNull(action.getBuildListener());
        Assert.assertEquals(action.getBuildListener().getClass(), DefaultLogger.class);
    }
}
