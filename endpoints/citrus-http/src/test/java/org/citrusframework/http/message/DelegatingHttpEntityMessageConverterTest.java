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

package org.citrusframework.http.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;

/**
 * @since 2.7.5
 */
public class DelegatingHttpEntityMessageConverterTest {

    private DelegatingHttpEntityMessageConverter messageConverter = new DelegatingHttpEntityMessageConverter();

    @ParameterizedTest
    @MethodSource("readProvider")
    public void testRead(InputStream bodyInput, Object expected, MediaType contentType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);

        HttpInputMessage message = Mockito.mock(HttpInputMessage.class);

        when(message.getHeaders()).thenReturn(headers);
        when(message.getBody()).thenReturn(bodyInput);

        Object converted = messageConverter.read(Object.class, message);

        if (expected instanceof byte[] expectedBytes) {
            Assertions.assertArrayEquals(expectedBytes, (byte[]) converted);
        } else {
            Assertions.assertEquals(expected.getClass(), converted.getClass());
            Assertions.assertEquals(expected, converted);
        }
    }

    static Stream<Arguments> readProvider() {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("message", "Hello Citrus!");
        formData.add("user", "Leonard");

        byte[] imageData = new byte[25];
        new Random().nextBytes(imageData);
        byte[] pdfData = new byte[25];
        new Random().nextBytes(pdfData);

        return Stream.of(
                Arguments.of(new ByteArrayInputStream("Hello Citrus!".getBytes()), "Hello Citrus!", MediaType.TEXT_PLAIN),
                Arguments.of(new ByteArrayInputStream("{ \"message\": \"Hello Citrus!\" }".getBytes()), "{ \"message\": \"Hello Citrus!\" }", MediaType.APPLICATION_JSON),
                Arguments.of(new ByteArrayInputStream("{ \"message\": \"Hello Citrus!\" }".getBytes()), "{ \"message\": \"Hello Citrus!\" }", new MediaType("application", "json", StandardCharsets.UTF_8)),
                Arguments.of(new ByteArrayInputStream("<message>Hello Citrus!</message>".getBytes()), "<message>Hello Citrus!</message>", MediaType.APPLICATION_XML),
                Arguments.of(new ByteArrayInputStream("message=Hello+Citrus%21&user=Leonard".getBytes()), formData, MediaType.APPLICATION_FORM_URLENCODED),
                Arguments.of(new ByteArrayInputStream(pdfData), pdfData, MediaType.APPLICATION_PDF),
                Arguments.of(new ByteArrayInputStream(imageData), imageData, MediaType.APPLICATION_OCTET_STREAM),
                Arguments.of(new ByteArrayInputStream(imageData), imageData, MediaType.IMAGE_PNG)
        );
    }

    @ParameterizedTest
    @MethodSource("writeProvider")
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

    static Stream<Arguments> writeProvider() {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("message", "Hello Citrus!");
        formData.add("user", "Leonard");

        byte[] imageData = new byte[25];
        new Random().nextBytes(imageData);
        byte[] pdfData = new byte[25];
        new Random().nextBytes(pdfData);

        return Stream.of(
                Arguments.of("Hello Citrus!", (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertEquals("Hello Citrus!", new String(outStream.toByteArray())), MediaType.TEXT_PLAIN),
                Arguments.of("{ \"message\": \"Hello Citrus!\" }", (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertEquals("{ \"message\": \"Hello Citrus!\" }", new String(outStream.toByteArray())), MediaType.APPLICATION_JSON),
                Arguments.of("{ \"message\": \"Hello Citrus!\" }", (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertEquals("{ \"message\": \"Hello Citrus!\" }", new String(outStream.toByteArray())), new MediaType("application", "json", StandardCharsets.UTF_8)),
                Arguments.of("<message>Hello Citrus!</message>", (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertEquals("<message>Hello Citrus!</message>", new String(outStream.toByteArray())), MediaType.APPLICATION_XML),
                Arguments.of(formData, (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertEquals("message=Hello+Citrus%21&user=Leonard", new String(outStream.toByteArray())), MediaType.APPLICATION_FORM_URLENCODED),
                Arguments.of(pdfData, (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertArrayEquals(pdfData, outStream.toByteArray()), MediaType.APPLICATION_PDF),
                Arguments.of(imageData, (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertArrayEquals(imageData, outStream.toByteArray()), MediaType.APPLICATION_OCTET_STREAM),
                Arguments.of(imageData, (Consumer<ByteArrayOutputStream>) outStream -> Assertions.assertArrayEquals(imageData, outStream.toByteArray()), MediaType.IMAGE_PNG)
        );
    }
}
