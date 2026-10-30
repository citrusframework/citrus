/*
 * Copyright the original author or authors.
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

package org.citrusframework.spring.variable;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.citrusframework.spring.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.variable.GlobalVariables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GlobalVariablesPropertyLoaderTest extends UnitTestSupport {

    @Test
    public void testPropertyLoadingFromClasspath() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/spring/variable/loadtest.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assertions.assertEquals(1, globalVariables.getVariables().size());
        Assertions.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
    }

    @Test
    public void testOverrideExistingVariables() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/spring/variable/loadtest.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        globalVariables.getVariables().put("property.load.test", "InitialValue");
        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assertions.assertEquals(1, globalVariables.getVariables().size());
        Assertions.assertTrue(globalVariables.getVariables().containsKey("property.load.test"));
        Assertions.assertNotEquals("InitialValue", globalVariables.getVariables().get("property.load.test"));
    }

    @Test
    public void testPropertyFileDoesNotExist() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:file_not_exists.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        assertThrows(CitrusRuntimeException.class, propertyLoader::afterPropertiesSet);
    }

    @Test
    public void testVariablesSupport() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/spring/variable/global-variable.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assertions.assertNotNull(globalVariables.getVariables().get("globalUserName"));
        Assertions.assertEquals("Citrus", globalVariables.getVariables().get("globalUserName"));
        Assertions.assertNotNull(globalVariables.getVariables().get("globalWelcomeText"));
        Assertions.assertEquals("Hello Citrus!", globalVariables.getVariables().get("globalWelcomeText"));
    }

    @Test
    public void testFunctionSupport() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/spring/variable/global-variable.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        propertyLoader.afterPropertiesSet();

        Assertions.assertNotNull(globalVariables.getVariables().get("globalDate"));
        Assertions.assertEquals("Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())) + "!",
                globalVariables.getVariables().get("globalDate"));
    }

    @Test
    public void testUnknownVariableDuringPropertyLoading() {
        GlobalVariablesPropertyLoader propertyLoader = new GlobalVariablesPropertyLoader();
        propertyLoader.setPropertyFiles(Collections.singletonList("classpath:org/citrusframework/spring/variable/global-variable-error.properties"));

        GlobalVariables globalVariables = new GlobalVariables();

        propertyLoader.setGlobalVariables(globalVariables);
        propertyLoader.setFunctionRegistry(testContextFactory.getFunctionRegistry());

        try {
            propertyLoader.afterPropertiesSet();
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(globalVariables.getVariables().isEmpty());
            Assertions.assertEquals("Unknown variable 'unknownVar'", e.getMessage());
            return;
        }

        Assertions.fail("Missing exception because of unknown variable in global variable property loader");
    }
}
