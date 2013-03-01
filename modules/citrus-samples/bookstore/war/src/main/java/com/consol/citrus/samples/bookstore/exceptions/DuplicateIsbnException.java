/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.samples.bookstore.exceptions;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;

import com.consol.citrus.samples.bookstore.model.AddBookRequestMessage;

/**
 * @author Christoph Deppisch
 */
public class DuplicateIsbnException extends MessageHandlingException {

    private static final long serialVersionUID = 1L;

    /**
     * @param failedMessage
     */
    public DuplicateIsbnException(Message<AddBookRequestMessage> failedMessage) {
        super(failedMessage, "Duplicate ISBN '" + failedMessage.getPayload().getBook().getIsbn() + "'! Book already exists in registry!");
    }
}
