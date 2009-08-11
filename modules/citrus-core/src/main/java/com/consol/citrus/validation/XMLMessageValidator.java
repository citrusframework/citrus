package com.consol.citrus.validation;

import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;

public interface XMLMessageValidator extends MessageValidator {
    public boolean validateXMLSchema(Message receivedMessage);

    public boolean validateDTD(Resource dtdResource, Message receivedMessage);

    public boolean validateNamespaces(Map expectedNamespaces, Message receivedMessage);
    
    public boolean validateMessageElements(Map<String, String> validateElements, Message receivedMessage, NamespaceContext nsContext, TestContext context);
}
