/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.functions.core;

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.Function;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;

/**
 * Function loads message from test context message store. Incoming and sent messages get automatically
 * stored to the message store. Messages are identified by their name.
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class LoadMessageFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList == null || parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty");
        }

        String messageName = parameterList.get(0);
        String messageHeader = null;
        if (messageName.endsWith(".body()")) {
            messageName = messageName.substring(0, messageName.indexOf(".body()"));
        } else if (messageName.contains(".header(") && messageName.endsWith(")")) {
            messageHeader = messageName.substring(messageName.indexOf(".header(") + 8, messageName.length() - 1);
            if (messageHeader.startsWith("'") && messageHeader.endsWith("'")) {
                messageHeader = messageHeader.substring(1, messageHeader.length() - 1);
            }

            if (!StringUtils.hasText(messageHeader)) {
                throw new CitrusRuntimeException("Missing header name in function parameter");
            }

            messageName = messageName.substring(0, messageName.indexOf(".header("));
        }

        Message stored = context.getMessageStore().getMessage(messageName);
        if (stored == null) {
            throw new CitrusRuntimeException(String.format("Failed to find stored message of name: '%s'", messageName));
        }

        if (StringUtils.hasText(messageHeader)) {
            Object headerValue = stored.getHeader(messageHeader);
            if (headerValue == null) {
                throw new CitrusRuntimeException(String.format("Failed to find header '%s' in stored message", messageHeader));
            }

            return headerValue.toString();
        } else {
            return stored.getPayload(String.class);
        }
    }
}
