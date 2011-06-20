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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.FailAction;
import com.consol.citrus.container.Catch;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class CatchParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testCatchParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 1);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), Catch.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "catch");
        
        Assert.assertEquals(((Catch)getTestCase().getActions().get(0)).getException(), "com.consol.citrus.exceptions.CitrusRuntimeException");
        Assert.assertEquals(((Catch)getTestCase().getActions().get(0)).getActionCount(), 1);
        Assert.assertEquals(((Catch)getTestCase().getActions().get(0)).getActions().get(0).getClass(), FailAction.class);
    }
}
