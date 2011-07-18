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

import com.consol.citrus.actions.InputAction;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;

/**
 * @author Christoph Deppisch
 */
public class InputActionParserTest extends AbstractBeanDefinitionParserBaseTest {

    @Test
    public void testInputActionParser() {
        Assert.assertEquals(getTestCase().getActions().size(), 4);

        Assert.assertEquals(getTestCase().getActions().get(0).getClass(), InputAction.class);
        Assert.assertEquals(getTestCase().getActions().get(0).getName(), "input");
        
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(0)).getMessage(), "Press return key to continue ...");
        Assert.assertNull(((InputAction)getTestCase().getActions().get(0)).getValidAnswers());
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(0)).getVariable(), "userinput");
        
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(1)).getMessage(), "Do you want to continue?");
        Assert.assertNull(((InputAction)getTestCase().getActions().get(1)).getValidAnswers());
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(1)).getVariable(), "userinput");
        
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(2)).getMessage(), "Do you want to continue?");
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(2)).getValidAnswers(), "yes/no");
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(2)).getVariable(), "userinput");
        
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(3)).getMessage(), "Do you want to continue?");
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(3)).getValidAnswers(), "y/n");
        Assert.assertEquals(((InputAction)getTestCase().getActions().get(3)).getVariable(), "inputVar");
    }
}
