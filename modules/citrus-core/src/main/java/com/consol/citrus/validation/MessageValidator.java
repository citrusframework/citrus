package com.consol.citrus.validation;

import java.util.Map;
import java.util.Set;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;

public interface MessageValidator {
    public boolean validateMessage(Message expectedMessage, Message receivedMessage, Set<String> ignoreElements, TestContext context) throws TestSuiteException;

    public boolean validateMessageHeader(Map<String, String> expectedHeaderValues, Map<String, String> receivedHeaderValues, TestContext context) throws TestSuiteException;
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message receivedMessage, TestContext context) throws TestSuiteException;
}