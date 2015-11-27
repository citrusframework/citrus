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

package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action catches possible exceptions in nested test actions.
 * 
 * @author Christoph Deppisch
 */
public class Catch extends AbstractActionContainer {
    /** Exception type caught */
    private String exception = CitrusRuntimeException.class.getName();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Catch.class);

    /**
     * Default constructor.
     */
    public Catch() {
        setName("catch");
    }

    @Override
    public void doExecute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Catch container catching exceptions of type " + exception);
        }

        for (TestAction action: actions) {
            try {
                setLastExecutedAction(action);
                action.execute(context);
            } catch (Exception e) {
                if (exception != null && exception.equals(e.getClass().getName())) {
                    log.info("Caught exception " + e.getClass() + ": " + e.getLocalizedMessage());
                    continue;
                }
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Set the exception that is caught.
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Gets the exception.
     * @return the exception
     */
    public String getException() {
        return exception;
    }
}
