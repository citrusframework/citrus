package com.consol.citrus.message;

import org.springframework.integration.core.Message;

import com.consol.citrus.exceptions.TestSuiteException;


/**
 * MessageHandler getting a request message that will be ransformed into a response message.
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public interface MessageHandler {
    Message handleMessage(Message message) throws TestSuiteException;
}
