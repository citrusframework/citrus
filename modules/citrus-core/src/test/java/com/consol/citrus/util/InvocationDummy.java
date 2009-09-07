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
