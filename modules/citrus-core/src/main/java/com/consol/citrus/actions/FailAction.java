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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action fails the test explicitly. User can specify a cause message.
 * 
 * @author Christoph Deppisch
 */
public class FailAction extends AbstractTestAction {
    /** User defined cause message to explain the error */
    private String message = "Generated error to interrupt test execution";

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            throw new CitrusRuntimeException(context.replaceDynamicContentInString(message));
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Setter for user defined cause message.
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
