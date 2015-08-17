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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class CreateVariablesJavaITest extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void createVariablesAction() {
        variable("myVariable", "12345");
        variable("newValue", "54321");
        
        echo("Current variable value: ${myVariable}");
        
        createVariable("myVariable", "${newValue}");
        createVariable("new", "This is a test");
        
        echo("Current variable value: ${myVariable}");
        
        echo("New variable 'new' has the value: ${new}");
        
        groovy("assert ${myVariable} == 54321");

        createVariable("foo", "bar");

        echo("foo = '${foo}'");
    }
}