/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.integration.parameter;

import org.citrusframework.annotations.CitrusXmlTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.testng.CitrusParameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParameterIT extends TestNGCitrusSpringSupport {

    @DataProvider(name = "someDataProvider")
    public Object[][] someDataProvider() {
        return new Object[][]{
                new Object[]{"some_value"},
        };
    }

    @Test(dataProvider = "someDataProvider")
    @CitrusParameters({"someVariable"})
    @CitrusXmlTest(name = "TestParameterIT")
    @SuppressWarnings("unused")
    public void test(String someVariable) {
    }
}
