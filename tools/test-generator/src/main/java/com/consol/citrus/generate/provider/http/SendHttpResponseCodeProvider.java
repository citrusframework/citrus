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

package com.consol.citrus.generate.provider.http;

import com.consol.citrus.generate.provider.CodeProvider;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.MessageHeaders;
import com.squareup.javapoet.CodeBlock;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SendHttpResponseCodeProvider implements CodeProvider<HttpMessage> {

    @Override
    public CodeBlock getCode(String endpoint, HttpMessage message) {
        CodeBlock.Builder code = CodeBlock.builder();

        code.add("http(action -> action.server($S)\n", endpoint);
        code.indent();
        code.add(".send()\n");

        code.add(".response($T.$L)\n", HttpStatus.class, message.getStatusCode().name());

        if (StringUtils.hasText(message.getPayload(String.class))) {
            code.add(".payload($S)\n", message.getPayload(String.class));
        }

        message.getHeaders().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                .forEach(entry -> {
                    code.add(".header($S, $S)\n", entry.getKey(), Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));
                });

        code.unindent();
        code.add(");");

        return code.build();
    }
}
