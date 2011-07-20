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

import com.consol.citrus.container.Iterate;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class IterateParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testFailActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 3);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), Iterate.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "iterate");
        
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getCondition(), "i lt 3");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getIndexName(), "i");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getIndex(), 1);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getStep(), 1);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getActionCount(), 1);
        
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(1)).getCondition(), "index lt= 2");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(1)).getIndexName(), "index");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(1)).getIndex(), 1);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(1)).getStep(), 1);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(0)).getActionCount(), 1);
        
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(2)).getCondition(), "i lt= 10");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(2)).getIndexName(), "i");
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(2)).getIndex(), 0);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(2)).getStep(), 5);
        Assert.assertEquals(((Iterate)getTestCase().getActions().get(2)).getActionCount(), 2);
    }
}
