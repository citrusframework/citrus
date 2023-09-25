/*
 * Copyright 2006-2019 the original author or authors.
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

package org.citrusframework.generate.provider.http;


import org.citrusframework.http.message.HttpMessage;
import com.squareup.javapoet.CodeBlock;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpCodeProviderTest {

    private final HttpCodeProvider httpCodeProvider = new HttpCodeProvider();

    private final HttpMessage message = new HttpMessage();
    private final CodeBlock.Builder code = CodeBlock.builder();

    @Test
    void testPathCodeIsGenerated() {

        //GIVEN
        message.path("foo");
        final String expectedCode = ".post(\"foo\")\n.message()\n.contentType(\"application/json\")\n";

        //WHEN
        httpCodeProvider.provideRequestConfiguration(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }

    @Test
    void testContentTypeCodeIsGenerated() {

        //GIVEN
        message.contentType("foo");
        final String expectedCode = ".post()\n.message()\n.header(\"Content-Type\", \"foo\")\n.contentType(\"foo\")\n";

        //WHEN
        httpCodeProvider.provideRequestConfiguration(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }

    @Test
    void testQueryParameterCodeIsGenerated() {

        //GIVEN
        message.queryParam("foo");
        final String expectedCode = ".post()\n.message()\n.contentType(\"application/json\")\n.queryParam(\"foo\", null)\n";

        //WHEN
        httpCodeProvider.provideRequestConfiguration(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }

    @Test
    void testResponseConfiguration() {

        //GIVEN
        message.status(HttpStatusCode.valueOf(HttpStatus.SC_NOT_FOUND));
        final String expectedCode = ".response(org.springframework.http.HttpStatus.NOT_FOUND)\n.message()\n";

        //WHEN
        httpCodeProvider.provideResponseConfiguration(code, message);

        //THEN
        assertEquals(expectedCode, code.build().toString());
    }
}
