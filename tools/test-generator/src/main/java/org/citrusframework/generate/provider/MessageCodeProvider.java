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

package org.citrusframework.generate.provider;

import java.util.Optional;

import com.squareup.javapoet.CodeBlock;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.util.StringUtils;

public class MessageCodeProvider {

    public void provideHeaderAndPayload(final CodeBlock.Builder code, final Message message) {
        provideMessage(code, message);
        provideHeader(code, message);
        providePayload(code, message);
    }

    private void provideMessage(final CodeBlock.Builder code, final Message message) {
        if (StringUtils.hasText(message.getPayload(String.class)) || !message.getHeaders().isEmpty()) {
            code.add(".message()\n", message.getPayload(String.class));
        }
    }

    private void provideHeader(final CodeBlock.Builder code, final Message message) {
        if (!message.getHeaders().isEmpty()) {
            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> code.add(
                            ".header($S, $S)\n",
                            entry.getKey(),
                            Optional.ofNullable(entry.getValue()).map(Object::toString).orElse("")));
        }
    }

    private void providePayload(final CodeBlock.Builder code, final Message message) {
        if (StringUtils.hasText(message.getPayload(String.class))) {
            code.add(".body($S)\n", message.getPayload(String.class));
        }
    }
}
