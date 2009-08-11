package com.consol.citrus.actions;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * EchoBean enables to print messages to the console/logger
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class EchoBean extends AbstractTestAction {

    /** Text to be printed */
    private String message;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(EchoBean.class);

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
