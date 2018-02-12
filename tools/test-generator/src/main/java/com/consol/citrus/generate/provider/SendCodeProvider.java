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

package com.consol.citrus.generate.provider;

import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.squareup.javapoet.CodeBlock;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SendCodeProvider implements CodeProvider<Message> {

    @Override
    public CodeBlock getCode(String endpoint, Message message) {
        CodeBlock.Builder code = CodeBlock.builder();

        code.add("send(action -> action.endpoint($S)\n", endpoint);
        code.indent();
        code.add(".payload($S)\n", message.getPayload(String.class));

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> {
                        code.add(".header($S, $S)\n", entry.getKey(), Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));
                    });
        }
        code.unindent();
        code.add(");");

        return code.build();
    }
}
