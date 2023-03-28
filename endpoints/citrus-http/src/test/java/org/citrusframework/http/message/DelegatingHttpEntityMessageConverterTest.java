/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.http.message;

import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.util.Random;
import java.util.function.Consumer;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class DelegatingHttpEntityMessageConverterTest {

    private DelegatingHttpEntityMessageConverter messageConverter = new DelegatingHttpEntityMessageConverter();

    private MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
    byte[] imageData = new byte[25];
    byte[] pdfData = new byte[25];

    @BeforeClass
    public void setup() {
        formData.add("message", "Hello Citrus!");
        formData.add("user", "Leonard");

        new Random().nextBytes(imageData);
        new Random().nextBytes(pdfData);
    }

    @Test(dataProvider = "readProvider")
    public void testRead(InputStream bodyInput, Object expected, MediaType contentType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpInputMessage message = Mockito.mock(HttpInputMessage.class);

        when(message.getHeaders()).thenReturn(headers);
        when(message.getBody()).thenReturn(bodyInput);

        Object converted = messageConverter.read(Object.class, message);

        Assert.assertEquals(converted.getClass(), expected.getClass());
        Assert.assertEquals(converted, expected);
    }

    @DataProvider
    public Object[][] readProvider() {
        return new Object[][] {
                new Object[] { new ByteArrayInputStream("Hello Citrus!".getBytes()), "Hello Citrus!", MediaType.TEXT_PLAIN },
                new Object[] { new ByteArrayInputStream("{ \"message\": \"Hello Citrus!\" }".getBytes()), "{ \"message\": \"Hello Citrus!\" }", MediaType.APPLICATION_JSON },
                new Object[] { new ByteArrayInputStream("{ \"message\": \"Hello Citrus!\" }".getBytes()), "{ \"message\": \"Hello Citrus!\" }", MediaType.APPLICATION_JSON_UTF8 },
                new Object[] { new ByteArrayInputStream("<message>Hello Citrus!</message>".getBytes()), "<message>Hello Citrus!</message>", MediaType.APPLICATION_XML },
                new Object[] { new ByteArrayInputStream("message=Hello+Citrus%21&user=Leonard".getBytes()), formData, MediaType.APPLICATION_FORM_URLENCODED },
                new Object[] { new ByteArrayInputStream(pdfData), pdfData, MediaType.APPLICATION_PDF },
                new Object[] { new ByteArrayInputStream(imageData), imageData, MediaType.APPLICATION_OCTET_STREAM },
                new Object[] { new ByteArrayInputStream(imageData), imageData, MediaType.IMAGE_PNG }
        };
    }

    @Test(dataProvider = "writeProvider")
    public void testWrite(Object bodyOutput, Consumer<ByteArrayOutputStream> verify, MediaType contentType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpOutputMessage message = Mockito.mock(HttpOutputMessage.class);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(message.getHeaders()).thenReturn(headers);
        when(message.getBody()).thenReturn(outputStream);

        messageConverter.write(bodyOutput, contentType, message);

        verify.accept(outputStream);
    }

    @DataProvider
    public Object[][] writeProvider() {
        return new Object[][] {
                new Object[] { "Hello Citrus!", (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(new String(outStream.toByteArray()), "Hello Citrus!");
                }, MediaType.TEXT_PLAIN },
                new Object[] { "{ \"message\": \"Hello Citrus!\" }", (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(new String(outStream.toByteArray()), "{ \"message\": \"Hello Citrus!\" }");
                }, MediaType.APPLICATION_JSON },
                new Object[] { "{ \"message\": \"Hello Citrus!\" }", (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(new String(outStream.toByteArray()), "{ \"message\": \"Hello Citrus!\" }");
                }, MediaType.APPLICATION_JSON_UTF8 },
                new Object[] { "<message>Hello Citrus!</message>", (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(new String(outStream.toByteArray()), "<message>Hello Citrus!</message>");
                }, MediaType.APPLICATION_XML },
                new Object[] { formData, (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(new String(outStream.toByteArray()), "message=Hello+Citrus%21&user=Leonard");
                }, MediaType.APPLICATION_FORM_URLENCODED },
                new Object[] { pdfData, (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(outStream.toByteArray(), pdfData);
                }, MediaType.APPLICATION_PDF },
                new Object[] { imageData, (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(outStream.toByteArray(), imageData);
                }, MediaType.APPLICATION_OCTET_STREAM },
                new Object[] { imageData, (Consumer<ByteArrayOutputStream>) outStream -> {
                    Assert.assertEquals(outStream.toByteArray(), imageData);
                }, MediaType.IMAGE_PNG }
        };
    }
}
