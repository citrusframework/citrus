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
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerStandaloneJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void httpServerStandalone() {
        variable("custom_header_id", "123456789");
        
        http().client("httpStandaloneClient")
            .post()
            .payload("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}");
        
        http().client("httpStandaloneClient")
            .response(HttpStatus.OK)
            .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .version("HTTP/1.1");
        
        http().client("httpStandaloneClient")
            .post()
            .payload("<moreRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</moreRequestMessage>")
            .header("CustomHeaderId", "${custom_header_id}");
        
        http().client("httpStandaloneClient")
            .response(HttpStatus.OK)
            .payload("<testResponseMessage>" +
                        "<text>Hello TestFramework</text>" +
                    "</testResponseMessage>")
            .version("HTTP/1.1");
    }
}