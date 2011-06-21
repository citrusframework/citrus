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

import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testPLSQLActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 2);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "plsql:testDataSource");
        
        Assert.assertEquals(getTestCase().getActions().get(1).getClass(), ExecutePLSQLAction.class);
        Assert.assertEquals(getTestCase().getActions().get(1).getName(), "plsql:testDataSource");
        
        Assert.assertNotNull(((ExecutePLSQLAction)getTestCase().getActions().get(0)).getDataSource());
        Assert.assertNotNull(((ExecutePLSQLAction)getTestCase().getActions().get(1)).getDataSource());
        
        Assert.assertNotNull(((ExecutePLSQLAction)getTestCase().getActions().get(0)).getSqlResource());
        Assert.assertNull(((ExecutePLSQLAction)getTestCase().getActions().get(0)).getScript());
        Assert.assertEquals(((ExecutePLSQLAction)getTestCase().getActions().get(0)).isIgnoreErrors(), false);
        
        Assert.assertNull(((ExecutePLSQLAction)getTestCase().getActions().get(1)).getSqlResource());
        Assert.assertTrue(((ExecutePLSQLAction)getTestCase().getActions().get(1)).getScript().length() > 0);
        Assert.assertEquals(((ExecutePLSQLAction)getTestCase().getActions().get(1)).isIgnoreErrors(), true);
    }
}
