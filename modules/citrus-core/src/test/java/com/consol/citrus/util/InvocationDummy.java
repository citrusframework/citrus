/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class only used to explain the usage of java reflection in test examples
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class InvocationDummy {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(InvocationDummy.class);

    public InvocationDummy() {
        if (log.isDebugEnabled()) {
            log.debug("Constructor without argument");
        }
    }

    public InvocationDummy(String arg) {
        if (log.isDebugEnabled()) {
            log.debug("Constructor with argument: " + arg);
        }
    }
    
    public InvocationDummy(int arg1, String arg2, boolean arg3) {
        if (log.isDebugEnabled()) {
        	if (log.isDebugEnabled()) {
                log.debug("Constructor with arguments:");
                log.debug("arg1: " + arg1);
                log.debug("arg2: " + arg2);
                log.debug("arg3: " + arg3);
            }
        }
    }

    public void invoke() {
    	if (log.isDebugEnabled()) {
            log.debug("Methode invoke no arguments");
        }
    }
    
    public void invoke(String text) {
    	if (log.isDebugEnabled()) {
            log.debug("Methode invoke with string argument: '" + text + "'");
        }
    }
    
    public void invoke(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("Methode invoke with argument: " + args[i]);
            }
        }
    }

    public void invoke(int arg1, String arg2, boolean arg3) {
        if (log.isDebugEnabled()) {
            log.debug("Method invoke with arguments:");
            log.debug("arg1: " + arg1);
            log.debug("arg2: " + arg2);
            log.debug("arg3: " + arg3);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (log.isDebugEnabled()) {
                log.debug("arg" + i + ": " + args[i]);
            }
        }
    }
}
