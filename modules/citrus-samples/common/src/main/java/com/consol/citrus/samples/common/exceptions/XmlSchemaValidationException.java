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

package com.consol.citrus.samples.common.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;

/**
 * @author Christoph Deppisch
 */
public class XmlSchemaValidationException extends MessageHandlingException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(XmlSchemaValidationException.class);
    
    /**
     * @param failedMessage
     */
    public XmlSchemaValidationException(Message<?> failedMessage, Throwable cause) {
        super(failedMessage);
        
        log.error("Schema validation error", cause);
    }
}
