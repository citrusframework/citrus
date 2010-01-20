/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Prints messages to the console/logger
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class EchoAction extends AbstractTestAction {

    /** Text to be printed */
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
