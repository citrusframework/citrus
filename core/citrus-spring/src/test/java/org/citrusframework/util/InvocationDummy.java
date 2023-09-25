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

package org.citrusframework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class InvocationDummy {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(InvocationDummy.class);

    public InvocationDummy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Constructor without argument");
        }
    }

    public InvocationDummy(String arg) {
        if (logger.isDebugEnabled()) {
            logger.debug("Constructor with argument: " + arg);
        }
    }
    
    public InvocationDummy(Integer arg1, String arg2, Boolean arg3) {
        if (logger.isDebugEnabled()) {
        	if (logger.isDebugEnabled()) {
                logger.debug("Constructor with arguments:");
                logger.debug("arg1: " + arg1);
                logger.debug("arg2: " + arg2);
                logger.debug("arg3: " + arg3);
            }
        }
    }

    public void invoke() {
    	if (logger.isDebugEnabled()) {
            logger.debug("Methode invoke no arguments");
        }
    }
    
    public void invoke(String text) {
    	if (logger.isDebugEnabled()) {
            logger.debug("Methode invoke with string argument: '" + text + "'");
        }
    }
    
    public void invoke(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Methode invoke with argument: " + args[i]);
            }
        }
    }

    public void invoke(Integer arg1, String arg2, Boolean arg3) {
        if (logger.isDebugEnabled()) {
            logger.debug("Method invoke with arguments:");
            logger.debug("arg1: " + arg1);
            logger.debug("arg2: " + arg2);
            logger.debug("arg3: " + arg3);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("arg" + i + ": " + args[i]);
            }
        }
    }
}
