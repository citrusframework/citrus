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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Prints messages to the console/logger during test execution.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class EchoAction extends AbstractTestAction {

    /** Log message */
    private String message;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(EchoAction.class);

    /**
     * Default constructor.
     */
    public EchoAction() {
        setName("echo");
    }

    @Override
    public void doExecute(TestContext context) {
        if (message == null) {
            log.info("Citrus test " + new Date(System.currentTimeMillis()));
        } else {
            log.info(context.replaceDynamicContentInString(message));

        }
    }

    /**
     * Setter for message
     * @param message
     */
    public EchoAction setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
