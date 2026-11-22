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

package org.citrusframework.testng.integration.parameter;

import org.citrusframework.annotations.CitrusParameters;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.api.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @since 5.3
 */
public class XmlDataProviderIT extends TestNGCitrusSpringSupport {

    @CitrusParameters("message")
    @CitrusTestSource(type = TestLoader.XML, sources = "classpath:org/citrusframework/testng/integration/parameter/data-provider.citrus.it.xml")
    @Test(dataProvider = "xmlDataProvider")
    public void xmlDataProviderIT(String message) {
    }

    @DataProvider
    public Object[][] xmlDataProvider() {
        return new Object[][] {
            { "Hello World!" },
            { "Hallo Welt!" },
        };
    }
}
