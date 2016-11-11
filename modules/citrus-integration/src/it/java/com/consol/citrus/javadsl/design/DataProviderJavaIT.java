/*
 * Copyright 2006-2013 the original author or authors.
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
import com.consol.citrus.testng.CitrusParameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DataProviderJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    @CitrusParameters( {"message", "delay"} )
    @Test(dataProvider = "sampleDataProvider")
    public void dataProvider(String message, Long sleep) {
        echo(message);
        sleep(sleep);

        echo("${message}");
        echo("${delay}");
    }

    @CitrusTest
    @CitrusParameters( {"message", "delay"} )
    @Test(dataProvider = "namedDataProvider")
    public void dataProviderName(String message, Long sleep) {
        echo(message);
        sleep(sleep);

        echo("${message}");
        echo("${delay}");
    }

    @DataProvider
    public Object[][] sampleDataProvider() {
        return new Object[][] {
                { "Hello World!", 300L },
                { "Hallo Welt!", 1000L },
                { "Hallo Citrus!", 500L },
        };
    }

    @DataProvider(name = "namedDataProvider")
    public Object[][] dataProvider() {
        return new Object[][] {
                { "Hallo Citrus!", 500L },
        };
    }
}
