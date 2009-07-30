package com.consol.citrus.validation;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

public interface XMLMessageValidator extends MessageValidator {
    public boolean validateXMLSchema(Message receivedMessage) throws TestSuiteException;

    public boolean validateDTD(Resource dtdResource, Message receivedMessage) throws TestSuiteException;

    public boolean validateNamespaces(Map expectedNamespaces, Message receivedMessage) throws TestSuiteException;
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message receivedMessage, NamespaceContext nsContext, TestContext context) throws TestSuiteException;
}
