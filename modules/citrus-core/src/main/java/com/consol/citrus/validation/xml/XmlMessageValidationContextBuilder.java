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

package com.consol.citrus.validation.xml;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.AbstractValidationContextBuilder;

/**
 * Validation context builder specific for XML message validation. Constructs a 
 * validation context for XPath, namespoace, and XML Tree validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlMessageValidationContextBuilder extends AbstractValidationContextBuilder<XmlMessageValidationContext> {

    /**
     * Prepare the message validation context with XML validation specific information.
     * 
     * @param action the test action.
     * @param context the current test context.
     * @return the xml validation context.
     */
    public XmlMessageValidationContext prepareValidationContext(TestAction action, TestContext context) {
        XmlMessageValidationContext validationContext =  new XmlMessageValidationContext();
        
        if (action instanceof XmlMessageValidationAware) {
            XmlMessageValidationAware xmlMessageValidationAware = (XmlMessageValidationAware)action;
            
            validationContext.setPathValidationExpressions(xmlMessageValidationAware.getPathValidationExpressions());
            validationContext.setIgnoreMessageElements(xmlMessageValidationAware.getIgnoreExpressions());
            validationContext.setControlNamespaces(xmlMessageValidationAware.getControlNamespaces());
            validationContext.setSchemaValidation(xmlMessageValidationAware.isSchemaValidationEnabled());
            validationContext.setNamespaceContext(xmlMessageValidationAware.getNamespaceContext());
        }
        
        return validationContext;
    }
}
