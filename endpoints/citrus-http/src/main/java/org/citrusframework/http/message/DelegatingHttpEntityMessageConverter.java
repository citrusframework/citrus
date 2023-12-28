/*
 * Copyright 2006-2024 the original author or authors.
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

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.citrusframework.util.TypeConversionUtils.convertIfNecessary;
import static org.springframework.http.MediaType.valueOf;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class DelegatingHttpEntityMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final List<HttpMessageConverter<?>> requestMessageConverters;
    private final List<HttpMessageConverter<?>> responseMessageConverters;

    private HttpMessageConverter<?> defaultRequestMessageConverter;
    private HttpMessageConverter<?> defaultResponseMessageConverter;

    /**
     * Default constructor initializing default delegate message converters.
     */
    public DelegatingHttpEntityMessageConverter() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructor using delegating message converters.
     * @param requestMessageConverters
     * @param responseMessageConverters
     */
    public DelegatingHttpEntityMessageConverter(List<HttpMessageConverter<?>> requestMessageConverters, List<HttpMessageConverter<?>> responseMessageConverters) {
        super(MediaType.ALL);

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        byteArrayHttpMessageConverter.setSupportedMediaTypes(
                asList(
                        MediaType.APPLICATION_OCTET_STREAM,
                        MediaType.APPLICATION_PDF,
                        MediaType.IMAGE_GIF,
                        MediaType.IMAGE_JPEG,
                        MediaType.IMAGE_PNG,
                        valueOf("application/zip")
                )
        );

        if (requestMessageConverters.isEmpty()) {
            requestMessageConverters.add(byteArrayHttpMessageConverter);
            requestMessageConverters.add(new StringHttpMessageConverter());
            requestMessageConverters.add(new AllEncompassingFormHttpMessageConverter());
        }

        if (responseMessageConverters.isEmpty()) {
            responseMessageConverters.add(byteArrayHttpMessageConverter);
            StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
            stringHttpMessageConverter.setWriteAcceptCharset(false);
            responseMessageConverters.add(stringHttpMessageConverter);
            responseMessageConverters.add(new AllEncompassingFormHttpMessageConverter());
        }

        this.requestMessageConverters = requestMessageConverters;
        this.responseMessageConverters = responseMessageConverters;

        this.defaultRequestMessageConverter = new StringHttpMessageConverter();
        this.defaultResponseMessageConverter = new StringHttpMessageConverter();
        ((StringHttpMessageConverter)this.defaultResponseMessageConverter).setWriteAcceptCharset(false);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        HttpMessageConverter<?> delegate = requestMessageConverters.stream()
                                .filter(converter -> converter.getSupportedMediaTypes()
                                                                .stream()
                                                                .filter(mediaType -> !mediaType.equals(MediaType.ALL))
                                                                .anyMatch(mediaType -> mediaType.equals(inputMessage.getHeaders().getContentType())))
                                .findFirst()
                                .orElse(defaultRequestMessageConverter);

        if (delegate instanceof ByteArrayHttpMessageConverter) {
            return ((ByteArrayHttpMessageConverter)delegate).read(byte[].class, inputMessage);
        } else if (delegate instanceof StringHttpMessageConverter) {
            return ((StringHttpMessageConverter)delegate).read(String.class, inputMessage);
        } else {
            return delegate.read(null, inputMessage);
        }
    }

    @Override
    protected void writeInternal(Object responseBody, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        HttpMessageConverter<?> delegate = responseMessageConverters.stream()
                .filter(converter -> converter.getSupportedMediaTypes()
                        .stream()
                        .filter(mediaType -> !mediaType.equals(MediaType.ALL))
                        .anyMatch(mediaType -> mediaType.equals(outputMessage.getHeaders().getContentType())))
                .findFirst()
                .orElse(defaultResponseMessageConverter);

        if (delegate instanceof ByteArrayHttpMessageConverter byteArrayHttpMessageConverter) {
            byteArrayHttpMessageConverter.write(convertIfNecessary(responseBody, byte[].class), outputMessage.getHeaders().getContentType(), outputMessage);
        } else if (delegate instanceof StringHttpMessageConverter stringHttpMessageConverter) {
            stringHttpMessageConverter.write(convertIfNecessary(responseBody, String.class), outputMessage.getHeaders().getContentType(), outputMessage);
        } else if (delegate instanceof FormHttpMessageConverter formHttpMessageConverter) {
            formHttpMessageConverter.write(convertIfNecessary(responseBody, MultiValueMap.class), outputMessage.getHeaders().getContentType(), outputMessage);
        } else {
            throw new HttpMessageNotWritableException(format("Failed to find proper message converter for contentType '%s'", outputMessage.getHeaders().getContentType()));
        }
    }

    /**
     * Sets the binaryMediaTypes.
     *
     * @param binaryMediaTypes
     */
    public void setBinaryMediaTypes(List<MediaType> binaryMediaTypes) {
        requestMessageConverters.stream()
                                .filter(converter -> converter instanceof ByteArrayHttpMessageConverter)
                                .map(ByteArrayHttpMessageConverter.class::cast)
                                .forEach(converter -> converter.setSupportedMediaTypes(binaryMediaTypes));

        responseMessageConverters.stream()
                                .filter(converter -> converter instanceof ByteArrayHttpMessageConverter)
                                .map(ByteArrayHttpMessageConverter.class::cast)
                                .forEach(converter -> converter.setSupportedMediaTypes(binaryMediaTypes));
    }
}
