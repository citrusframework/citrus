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

package org.citrusframework.integration.parameter;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.testng.CitrusParameters;
import org.testng.annotations.*;

/**
 * @author Christoph Deppisch
 * @since 2011
 */
public class DataProviderIT extends TestNGCitrusSpringSupport {

    @CitrusTestSource(type = TestLoader.SPRING)
    @CitrusParameters( "message" )
    @Test(dataProvider = "citrusDataProvider")
    public void DataProviderIT(String message) {
    }

    @DataProvider
    public Object[][] citrusDataProvider() {
        return new Object[][] {
            { "Hello World!" },
            { "Hallo Welt!" },
            { "Hallo Citrus!" },
        };
    }

    @CitrusTestSource(type = TestLoader.SPRING, name = "DataProviderIT")
    @CitrusParameters( "message" )
    @Test(dataProvider = "namedDataProvider")
    public void DataProviderNameIT(String message) {
    }

    @DataProvider(name = "namedDataProvider")
    public Object[][] dataProvider() {
        return new Object[][] {
            { "Hallo Citrus!" },
        };
    }

}
