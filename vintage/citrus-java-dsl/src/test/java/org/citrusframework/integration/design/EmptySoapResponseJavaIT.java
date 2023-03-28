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

package org.citrusframework.integration.design;

import org.citrusframework.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class EmptySoapResponseJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void test() {
        send("webServiceHelloClient")
            .payload("<ns0:SoapProbingRequest xmlns:ns0=\"http://citrusframework.org/schemas/samples/sayHello.xsd\">" +
                                "<ns0:Timestamp>citrus:currentDate(\"yyyy-MM-dd'T'hh:mm:ss\")</ns0:Timestamp>" +
                            "</ns0:SoapProbingRequest>");

        receiveTimeout("webServiceHelloClient").timeout(1000L);
    }
}
