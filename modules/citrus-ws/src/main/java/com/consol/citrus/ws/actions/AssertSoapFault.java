/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ws.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinitionEditor;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.transform.StringSource;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.validation.SoapFaultValidator;

/**
 * Asserting SOAP fault exception in embedded test action.
 * 
 * Class constructs a control soap fault detail with given expeceted information (faultCode, faultString and faultDetail)
 * and delegates validation to {@link SoapFaultValidator} instance.
 * 
 * @author Christoph Deppisch 
 * @since 2009
 */
public class AssertSoapFault extends AbstractActionContainer {
    /** TestAction to be executed */
    private TestAction action;

    /** Localized fault string */
    private String faultString = null;
    
    /** OName representing fault code */
    private String faultCode = null;
    
    /** File resource describing fault detail */
    private Resource faultDetailResource;
    
    /** Fault detail as inline CDATA */
    private String faultDetail;
    
    /** Soap fault validator implementaiton */
    private SoapFaultValidator validator;
    
    /** Message factory creating new soap messages */
    private SoapMessageFactory messageFactory;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AssertSoapFault.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Asserting SOAP fault ...");

        try {
            action.execute(context);
        } catch (Exception e) {
            log.info("Validating SOAP fault ...");
            if (SoapFaultClientException.class.isAssignableFrom(e.getClass())) {
                SoapFaultClientException soapFaultException = (SoapFaultClientException)e;

                SoapFault controlFault = constructControlFault(context);
                
                validator.validateSoapFault(soapFaultException.getSoapFault(), controlFault);
                
                log.info("SOAP fault as expected: " + soapFaultException.getFaultCode() + ": " + soapFaultException.getFaultStringOrReason());
                log.info("SOAP fault validation successful");
                return;
            } else {
                throw new ValidationException("SOAP fault validation failed for asserted exception type - expected: '" + 
                        SoapFaultClientException.class + "' but was: '" + e.getClass().getName() + "'", e);
            }
        }

        throw new ValidationException("SOAP fault validation failed! Missing asserted SOAP fault exception");
    }

    /**
     * Constructs the control soap fault holding all expected fault information
     * like faultCode, faultString and faultDetail.
     * 
     * @return the constructed SoapFault instance.
     */
    private SoapFault constructControlFault(TestContext context) {
        SoapFault controlFault = null;
        
        try {
            SoapFaultDefinitionEditor definitionEditor = new SoapFaultDefinitionEditor();
        
            if(StringUtils.hasText(faultString)) {
                definitionEditor.setAsText(context.replaceDynamicContentInString(faultCode) + "," + context.replaceDynamicContentInString(faultString));
            } else {
                definitionEditor.setAsText(context.replaceDynamicContentInString(faultCode));
            }
            
            SoapFaultDefinition definition = (SoapFaultDefinition)definitionEditor.getValue();
            
            SoapBody soapBody = ((SoapMessage)messageFactory.createWebServiceMessage()).getSoapBody();
        
            if (SoapFaultDefinition.SERVER.equals(definition.getFaultCode()) ||
                    SoapFaultDefinition.RECEIVER.equals(definition.getFaultCode())) {
                controlFault = soapBody.addServerOrReceiverFault(definition.getFaultStringOrReason(), 
                        definition.getLocale());
            } else if (SoapFaultDefinition.CLIENT.equals(definition.getFaultCode()) ||
                    SoapFaultDefinition.SENDER.equals(definition.getFaultCode())) {
                controlFault = soapBody.addClientOrSenderFault(definition.getFaultStringOrReason(), 
                        definition.getLocale());
            } else if (soapBody instanceof Soap11Body) {
                Soap11Body soap11Body = (Soap11Body) soapBody;
                controlFault = soap11Body.addFault(definition.getFaultCode(), 
                        definition.getFaultStringOrReason(), 
                        definition.getLocale());
            } else if (soapBody instanceof Soap12Body) {
                Soap12Body soap12Body = (Soap12Body) soapBody;
                Soap12Fault soap12Fault =
                        (Soap12Fault) soap12Body.addServerOrReceiverFault(definition.getFaultStringOrReason(), 
                                definition.getLocale());
                soap12Fault.addFaultSubcode(definition.getFaultCode());
                
                controlFault = soap12Fault;
            } else {
                    throw new CitrusRuntimeException("Found unsupported SOAP implementation. Use SOAP 1.1 or SOAP 1.2.");
            }
        
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            if (faultDetailResource != null) {
                transformer.transform(new StringSource(
                        context.replaceDynamicContentInString(FileUtils.readToString(faultDetailResource))), controlFault.addFaultDetail().getResult());
            } else if (faultDetail != null){
                transformer.transform(new StringSource(
                        context.replaceDynamicContentInString(faultDetail)), controlFault.addFaultDetail().getResult());
            }
        } catch (ParseException ex) {
            throw new CitrusRuntimeException("Error during SOAP fault validation", ex);
        } catch (IOException ex) {
            throw new CitrusRuntimeException("Error during SOAP fault validation", ex);
        } catch (TransformerException ex) {
            throw new CitrusRuntimeException("Error during SOAP fault validation", ex);
        }
        
        return controlFault;
    }

    /**
     * Set the nested test action.
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
    }

	/**
	 * Set the fault code.
	 * @param faultCode the faultCode to set
	 */
	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	/**
	 * Set the fault string.
	 * @param faultString the faultString to set
	 */
	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

    /**
     * Set the fault detail from external file resource.
     * @param faultDetailResource the faultDetailResource to set
     */
    public void setFaultDetailResource(Resource faultDetailResource) {
        this.faultDetailResource = faultDetailResource;
    }

    /**
     * Set the fault string from inline CDATA.
     * @param faultDetail the faultDetail to set
     */
    public void setFaultDetail(String faultDetail) {
        this.faultDetail = faultDetail;
    }

    /**
     * @param validator the validator to set
     */
    public void setValidator(SoapFaultValidator validator) {
        this.validator = validator;
    }

    /**
     * @param messageFactory the messageFactory to set
     */
    public void setMessageFactory(SoapMessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
    
    /**
     * @see com.consol.citrus.container.TestActionContainer#addTestAction(com.consol.citrus.TestAction)
     */
    public void addTestAction(TestAction action) {
        this.action = action;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionCount()
     */
    public long getActionCount() {
        return 1;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionIndex(com.consol.citrus.TestAction)
     */
    public int getActionIndex(TestAction action) {
        return 0;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActions()
     */
    public List<TestAction> getActions() {
        return Collections.singletonList(action);
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getTestAction(int)
     */
    public TestAction getTestAction(int index) {
        if(index == 0) {
            return action;
        } else {
            throw new IndexOutOfBoundsException("Illegal index in action list:" + index);
        }
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#setActions(java.util.List)
     */
    public void setActions(List<TestAction> actions) {
        if(!CollectionUtils.isEmpty(actions)) {
            action = actions.get(0); 
        }
    }
}
