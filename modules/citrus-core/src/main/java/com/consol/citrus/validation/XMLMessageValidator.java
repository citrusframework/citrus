package com.consol.citrus.validation;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;

public interface XMLMessageValidator extends MessageValidator {
    public boolean validateXMLSchema(Resource schemaResource, Message receivedMessage) throws TestSuiteException;

    public boolean validateDTD(Resource dtdResource, Message receivedMessage) throws TestSuiteException;

    public boolean validateNamespaces(Map expectedNamespaces, Message receivedMessage) throws TestSuiteException;
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message receivedMessage, NamespaceContext nsContext, TestContext context) throws TestSuiteException;
}
