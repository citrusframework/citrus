package com.consol.citrus.actions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import javax.xml.namespace.NamespaceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.NoRessourceException;
import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSelectorBuilder;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.XMLMessageValidator;
import com.consol.citrus.xml.NamespaceContextImpl;

/**
 * This bean recieves messages from a service destination. The received message is validated
 * through a validator in its body and header.
 *
 * @author deppisch Christoph Deppisch Consol*GmbH 2008
 */
public class ReceiveMessageBean extends AbstractTestAction {
    /** Map holding message elements to be overwritten before sending */
    private Map messageElements = new HashMap();

    /** Map containing header values. The received message must fit this header */
    private Map headerValues = new HashMap();

    /** Map extracting message elements to variables */
    private Map extractMessageElements = new HashMap();

    /** Map extracting header valdues to variables */
    private Map extractHeaderValues = new HashMap();

    /** Map holding message elements that will be ignored in validation */
    private Set ignoreMessageElements = new HashSet();

    /** Map for namespace validation */
    private Map expectedNamespaces = new HashMap();

    /** Select messages to receive */
    private Map messageSelector = new HashMap();

    /** Select messages to receive by string configuration */
    private String messageSelectorString;

    /** The service to be used for receiving the message */
    private MessageReceiver messageReceiver;
    
    private long receiveTimeout = 5000L;

    /** Message ressource as a file */
    private Resource messageResource;

    /** Inline message resource definition as string */
    private String messageData;

    /** Validator doing all message validation tasks */
    @Autowired
    private MessageValidator validator;
    
    private boolean schemaValidation = true;
    
    /** XML namespace declaration used for xpath expression evaluation*/
    private Map<String, String> namespaces = new HashMap<String, String>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(ReceiveMessageBean.class);

    /**
     * Following actions will be executed:
     * 1. The message is received
     * 2. Validation of the received header values
     * 3. XML schema validation
     * 4. Expected XML resource is parsed and prepared for comparison
     * 5. The received message is validated against the source message
     * 6. Explicit validation of message values
     * 7. Extract message elements and header values to variables
     *
     * @return boolean success flag
     * @throws TestSuiteException
     */
    @Override
    public void execute(TestContext context) throws TestSuiteException {
        boolean isSuccess = true;
        
        Message receivedMessage;
        
        try {
            if (StringUtils.hasText(messageSelectorString)) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting JMS message selector to value " + messageSelectorString);
                }

                receivedMessage = messageReceiver.receiveSelected(context.replaceDynamicContentInString(messageSelectorString));
            } else if (CollectionUtils.isEmpty(messageSelector) == false) {
                receivedMessage = messageReceiver
                        .receiveSelected(MessageSelectorBuilder.fromKeyValueMap(
                                context.replaceVariablesInMap(messageSelector))
                                .build(), receiveTimeout);
            } else {
                receivedMessage = messageReceiver.receive(receiveTimeout); //TODO set this timeout from outside
            }

            if (receivedMessage == null)
                throw new TestSuiteException("Received message is null!");

            context.createVariablesFromHeaderValues(extractHeaderValues, receivedMessage.getHeaders());

            /** 2. Validation of the received header values.*/
            if (hasHeaderValues()) {
                log.info("Now validating message header values");

                if (validator.validateMessageHeader(headerValues, receivedMessage.getHeaders(), context) == false) {
                    isSuccess = false;
                }
            }

            if (receivedMessage.getPayload() == null || receivedMessage.getPayload().toString().length() == 0) {
                if (messageResource == null && (messageData == null || messageData.length() == 0)) {
                    log.info("Received message body is empty as expected - therefore no message validation");
                    return;
                } else {
                    throw new TestSuiteException("Validation error: Received message body is empty");
                }
            }

            /** 3. If a XML validation schema is defined, is received message
             * schema is validated.
             */
            if (schemaValidation) {
                if(validator instanceof XMLMessageValidator) {
                    ((XMLMessageValidator)validator).validateXMLSchema(receivedMessage);
                } else {
                    throw new TestSuiteException("XML schema validation is not valid for validators other than XMLMessageValidator");
                }
            }

            if(validator instanceof XMLMessageValidator) {
                ((XMLMessageValidator)validator).validateNamespaces(expectedNamespaces, receivedMessage);
            } else {
                throw new TestSuiteException("XML namespace validation is not valid for validators other than XMLMessageValidator");
            }

            String expectedMessagePayload = null;
            
            if (messageResource != null) {
                BufferedInputStream reader = new BufferedInputStream(messageResource.getInputStream());
                StringBuffer contentBuffer = new StringBuffer();
                
                byte[] contents = new byte[1024];
                int bytesRead=0;
                while( (bytesRead = reader.read(contents)) != -1){
                    contentBuffer.append(new String(contents, 0, bytesRead));
                }
                
                expectedMessagePayload = contentBuffer.toString();
            } else if (messageData != null){
                expectedMessagePayload = context.replaceDynamicContentInString(messageData);
            } else if (messageElements.isEmpty() == false){
                expectedMessagePayload = "";
            } else {
                throw new NoRessourceException("No validation elements specifyed. You need to declare at least one element to be validated");
            }

            if (StringUtils.hasText(expectedMessagePayload)) {
                /** and for each key within setMessageValues the value is set
                 * within the source.
                 */
                expectedMessagePayload = context.replaceMessageValues(messageElements, expectedMessagePayload);

                Message<String> expectedMessage = MessageBuilder.withPayload(expectedMessagePayload).build();
                
                /** 4.2. The received message is validated against the source message,
                 * but elements ignoreValues will not be validated.
                 */
                if (validator.validateMessage(expectedMessage, receivedMessage, ignoreMessageElements, context) == false)
                    isSuccess = false;
            }
            
            NamespaceContext nsContext = null;
            if(namespaces.isEmpty() == false) {
                namespaces.putAll(XMLUtils.lookupNamespaces(XMLUtils.parseMessagePayload(receivedMessage.getPayload().toString()).getFirstChild()));
                nsContext = new NamespaceContextImpl(namespaces);
            }

            /** 5. Validation of the received message values. */
            if (messageData == null && messageResource == null &&
                    ((XMLMessageValidator)validator).validateMessageElements(messageElements, receivedMessage, nsContext, context) == false) {
                    isSuccess = false;
            }

            if (isSuccess == true) {
                /* call getMessageValues() only if no error has occured until now,
			 	   otherwise we may read false values */
                /** 6. The received message element values for each key within
                 * getMessageValues are read into the corresponding variables.
                 */
                context.createVariablesFromMessageValues(extractMessageElements, receivedMessage);
            }
        } catch (ParseException e) {
            throw new TestSuiteException(e);
        } catch (IOException e) {
            throw new TestSuiteException(e);
        }

        if (!isSuccess) {
            throw new TestSuiteException("Validation failed for received message");
        }
    }

    /**
     * Check if validate header values are present
     * @return boolean flag to mark existence
     */
    public boolean hasHeaderValues() {
        return (this.headerValues != null && !this.headerValues.isEmpty());
    }

    /**
     * Spring property setter.
     * @param setMessageValues
     */
    public void setMessageElements(HashMap messageElements) {
        this.messageElements = messageElements;
    }

    /**
     * Spring property setter.
     * @param ignoreMessageElements
     */
    public void setIgnoreMessageElements(Set ignoreMessageElements) {
        this.ignoreMessageElements = ignoreMessageElements;
    }

    /**
     * @param expectedNamespaces the expectedNamespaces to set
     */
    public void setExpectedNamespaces(Map expectedNamespaces) {
        this.expectedNamespaces = expectedNamespaces;
    }

    /**
     * @param getHeaderValues the getHeaderValues to set
     */
    public void setExtractHeaderValues(HashMap extractHeaderValues) {
        this.extractHeaderValues = extractHeaderValues;
    }

    /**
     * @param extractMessageElements the extractMessageElements to set
     */
    public void setExtractMessageElements(HashMap extractMessageElements) {
        this.extractMessageElements = extractMessageElements;
    }

    /**
     * @param headerValues the headerValues to set
     */
    public void setHeaderValues(Map headerValues) {
        this.headerValues = headerValues;
    }

    /**
     * @param setMessageValues the setMessageValues to set
     */
    public void setSetMessageValues(HashMap setMessageValues) {
        this.messageElements = setMessageValues;
    }

    /**
     * @param messageData the messageData to set
     */
    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    /**
     * @param messageResource the messageResource to set
     */
    public void setMessageResource(Resource messageResource) {
        this.messageResource = messageResource;
    }

    /**
     * Check if header values for extraction are present
     * @return boolean flag to mark existence
     */
    public boolean hasExtractHeaderValues() {
        return (this.extractHeaderValues != null && !this.extractHeaderValues.isEmpty());
    }

    /**
     * Setter for messageSelector
     * @param messageSelector
     */
    public void setMessageSelector(Map messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * @param messageSelectorString
     */
    public void setMessageSelectorString(String messageSelectorString) {
        this.messageSelectorString = messageSelectorString;
    }

    /**
     * @param validator the validator to set
     */
    public void setValidator(XMLMessageValidator validator) {
        this.validator = validator;
    }

    /**
     * @param validateMessageElements the validateMessageElements to set
     */
    public void setValidateMessageElements(Map validateMessageElements) {
        this.messageElements = validateMessageElements;
    }

    /**
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * @param messageReceiver the messageReceiver to set
     */
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    /**
     * @return the messageReceiver
     */
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    /**
     * @param enableSchemaValidation the schemaValidation to set
     */
    public void setSchemaValidation(boolean enableSchemaValidation) {
        this.schemaValidation = enableSchemaValidation;
    }
}
