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

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Parallel;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ParallelParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testFailActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 2);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), Parallel.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "parallel");
        
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(0)).getActionCount(), 2);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(0)).getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(0)).getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(1)).getActionCount(), 3);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(1)).getActions().get(0).getClass(), Parallel.class);
        Assert.assertEquals(((Parallel)((Parallel)getTestCase().getActions().get(1)).getActions().get(0)).getActionCount(), 2);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(1)).getActions().get(1).getClass(), EchoAction.class);
        Assert.assertEquals(((Parallel)getTestCase().getActions().get(1)).getActions().get(2).getClass(), EchoAction.class);
    }
}
