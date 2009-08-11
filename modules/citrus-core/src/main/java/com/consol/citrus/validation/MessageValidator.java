package com.consol.citrus.validation;

import java.util.Map;
import java.util.Set;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;

import com.consol.citrus.context.TestContext;

public interface MessageValidator {
    public boolean validateMessage(Message expectedMessage, Message receivedMessage, Set<String> ignoreElements, TestContext context);

    public boolean validateMessageHeader(Map<String, String> expectedHeaderValues, MessageHeaders receivedHeaderValues, TestContext context);
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message receivedMessage, TestContext context);
}