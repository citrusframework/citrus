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

package org.citrusframework.variable;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class GlobalVariablesPropertyLoaderTest extends UnitTestSupport {

    @Test
    public void testPropertyLoadingFromClasspath() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/variable/loadtest.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assert.assertEquals(globalVariables.getVariables().size(), 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
    }

    @Test
    public void testOverrideExistingVariables() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/variable/loadtest.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        globalVariables.getVariables().put("property.load.test", "InitialValue");
        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assert.assertEquals(globalVariables.getVariables().size(), 1);
        Assert.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
        Assert.assertNotEquals(globalVariables.getVariables().get("property.load.test"), "InitialValue");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testPropertyFileDoesNotExist() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:file_not_exists.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();
    }

    @Test
    public void testVariablesSupport() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/variable/global-variable.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assert.assertNotNull(globalVariables.getVariables().get("globalUserName"));
        Assert.assertEquals(globalVariables.getVariables().get("globalUserName"), "Citrus");
        Assert.assertNotNull(globalVariables.getVariables().get("globalWelcomeText"));
        Assert.assertEquals(globalVariables.getVariables().get("globalWelcomeText"), "Hello Citrus!");
    }

    @Test
    public void testFunctionSupport() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/variable/global-variable.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assert.assertNotNull(globalVariables.getVariables().get("globalDate"));
        Assert.assertEquals(globalVariables.getVariables().get("globalDate"),
                "Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + "!");
    }

    @Test
    public void testUnknownVariableDuringPropertyLoading() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/variable/global-variable-error.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        try {
            propertyLoader.afterPropertiesSet();
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(globalVariables.getVariables().isEmpty());
            Assert.assertEquals(e.getMessage(), "Unknown variable 'unknownVar'");
            return;
        }

        Assert.fail("Missing exception because of unknown variable in global variable property loader");
    }
}
