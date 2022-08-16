/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.xml.actions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DelegatingPathExpressionProcessor;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.ScriptPayloadBuilder;
import com.consol.citrus.message.builder.MessageBuilderSupport;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.interceptor.BinaryMessageProcessor;
import com.consol.citrus.validation.interceptor.GzipMessageProcessor;
import com.consol.citrus.xml.util.PayloadElementParser;
import org.springframework.util.StringUtils;

import static com.consol.citrus.dsl.MessageSupport.MessageBodySupport.fromBody;
import static com.consol.citrus.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;

/**
 * @author Christoph Deppisch
 */
public final class MessageSupport {

    private MessageSupport() {
        //prevent instantiation of utility class
    }

    public static void configureMessage(MessageBuilderSupport.MessageActionBuilder<?, ?, ?> builder, Message message) {
        if (message.type != null) {
            if (StringUtils.hasText(message.type)) {
                if (message.type.equalsIgnoreCase(MessageType.GZIP.name())) {
                    builder.process(new GzipMessageProcessor());
                }

                if (message.type.equalsIgnoreCase(MessageType.BINARY.name())) {
                    builder.process(new BinaryMessageProcessor());
                }
            }

            builder.message().type(message.type);
        }

        if (message.body != null) {
            if (message.body.data != null) {
                builder.message().body(message.body.data.trim());
            }

            if (message.body.resource != null) {
                if (message.body.resource.charset != null) {
                    builder.message().body(FileUtils.getFileResource(message.body.resource.file + FileUtils.FILE_PATH_CHARSET_PARAMETER + message.body.resource.charset));
                } else {
                    builder.message().body(FileUtils.getFileResource(message.body.resource.file));
                }
            }

            if (message.body.payload != null && !message.body.payload.getAnies().isEmpty()) {
                builder.message().body(PayloadElementParser.parseMessagePayload(message.body.payload.anies.get(0)));
            }
        }

        configureScriptPayloadBuilder(builder, message);

        if (message.name != null) {
            builder.message().name(message.name);
        }

        Map<String, Object> pathExpressions = new HashMap<>();
        for (Message.Expression expression : message.getExpressions()) {
            String pathExpression = expression.path;
            pathExpressions.put(pathExpression, expression.value);
        }

        if (!pathExpressions.isEmpty()) {
            builder.message().process(new DelegatingPathExpressionProcessor(pathExpressions));
        }

        if (message.dataDictionary != null) {
            builder.message().dictionary(message.dataDictionary);
        }

        Message.Headers headers = message.getHeaders();
        if (headers != null) {
            headers.getHeaders().forEach(header -> {
                Object headerValue;
                if (StringUtils.hasText(header.type)) {
                    headerValue = MessageHeaderType.createTypedValue(header.type, header.value);
                } else {
                    headerValue = header.value;
                }

                builder.message().header(header.name, headerValue);
            });
            headers.getValues().forEach(builder.message()::header);

            headers.getFragments()
                    .stream()
                    .filter(fragment -> !fragment.getAnies().isEmpty())
                    .forEach(fragment -> builder.message().header(PayloadElementParser.parseMessagePayload(fragment.anies.get(0))));

            headers.getResources().forEach(resource -> {
                if (resource.charset != null) {
                    builder.message().header(FileUtils.getFileResource(resource.file + FileUtils.FILE_PATH_CHARSET_PARAMETER + resource.charset));
                } else {
                    builder.message().header(FileUtils.getFileResource(resource.file));
                }
            });
        }
    }

    private static void configureScriptPayloadBuilder(MessageBuilderSupport.MessageActionBuilder<?, ?, ?> builder, Message message) {
        if (message.body != null && message.body.builder != null) {
            String scriptType = Optional.ofNullable(message.body.builder.getType()).orElse("groovy");

            Optional<ScriptPayloadBuilder> scriptPayloadBuilder = ScriptPayloadBuilder.lookup(scriptType);

            if (scriptPayloadBuilder.isPresent()) {
                if (message.body.builder.getValue() != null) {
                    scriptPayloadBuilder.get().setScript(message.body.builder.getValue().trim());
                }

                if (message.body.builder.getFile() != null) {
                    if (message.body.builder.getCharset() != null) {
                        scriptPayloadBuilder.get().setFile(FileUtils.getFileResource(message.body.builder.getFile() + FileUtils.FILE_PATH_CHARSET_PARAMETER + message.body.builder.getCharset()));
                    } else {
                        scriptPayloadBuilder.get().setFile(FileUtils.getFileResource(message.body.builder.getFile()));
                    }
                }

                builder.message().body(scriptPayloadBuilder.get());
            } else {
                throw new CitrusRuntimeException(String.format("Failed to resolve script payload builder for type '%s'", scriptType));
            }
        }
    }

    public static void configureExtract(MessageBuilderSupport.MessageActionBuilder<?, ?, ?> builder, Message.Extract value) {
        if (!value.getHeaders().isEmpty()) {
            Map<String, Object> expressions = new LinkedHashMap<>();
            for (Message.Extract.Header extract : value.getHeaders()) {
                expressions.put(extract.name, extract.variable);
            }
            builder.message().extract(fromHeaders()
                    .expressions(expressions));
        }

        if (!value.getBodyExpressions().isEmpty()) {
            Map<String, Object> expressions = new LinkedHashMap<>();
            for (Message.Extract.Expression extract : value.getBodyExpressions()) {
                String pathExpression = extract.path;

                //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                if (extract.resultType != null) {
                    pathExpression = extract.resultType + ":" + pathExpression;
                }

                expressions.put(pathExpression, extract.variable);
            }
            builder.message().extract(fromBody()
                    .expressions(expressions));
        }
    }
}
