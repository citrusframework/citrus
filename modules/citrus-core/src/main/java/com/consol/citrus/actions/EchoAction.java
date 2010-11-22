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

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Prints messages to the console/logger during test execution.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class EchoAction extends AbstractTestAction {

    /** Log message */
    private String message;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(EchoAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        if (message == null) {
            log.info("TestSuite " + new Date(System.currentTimeMillis()));
        } else {
            try {
                log.info("echo " + context.replaceDynamicContentInString(message));
            } catch (ParseException e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Setter for message
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
