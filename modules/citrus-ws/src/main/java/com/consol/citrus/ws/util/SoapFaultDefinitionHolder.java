/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ws.util;

import java.util.Locale;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.xml.namespace.QNameUtils;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * SOAP fault definition consists of fault code as well as optional values for fault reason string, fault actor and locale. This holder
 * constructs a proper {@link SoapFaultDefinition} from String representation and back. The String representation follows this syntax:
 * 
 * {faultCode}{faultString}{locale}{faultActor}
 *  
 * @author Christoph Deppisch
 * @since 1.3
 */
public class SoapFaultDefinitionHolder {

    /** Syntax decoration prefix/suffix */
    private static final String DECORATION_SUFFIX = "}";
    private static final String DECORATION_PREFIX = "{";
    
    /** Soap fault definition */
    private SoapFaultDefinition soapFaultDefinition = new SoapFaultDefinition();
    
    /** Optional fault actor */
    private String faultActor;
    
    /**
     * Construct instance from String expression.
     * @param faultExpression
     * @return
     */
    public static SoapFaultDefinitionHolder fromString(String faultExpression) {
        SoapFaultDefinitionHolder holder = new SoapFaultDefinitionHolder();
        
        if (!isValid(faultExpression)) {
            throw new CitrusRuntimeException("Invalid SOAP fault definition expression (" + faultExpression + ") - " +
            		"please follow syntax " + decorate("faultCode") + decorate("faultString") + decorate("faultActor") + decorate("locale"));
        }
        
        String[] tokens = faultExpression.substring(1, faultExpression.length() - 1).split("\\}\\{");
        
        holder.setFaultCode(tokens[0].trim());
        
        if (tokens.length > 1) {
            holder.setFaultStringOrReason(tokens[1].trim());
        }
        
        if (tokens.length > 2 && StringUtils.hasText(tokens[2].trim())) {
            holder.setLocale(tokens[2].trim());
        }
        
        if (tokens.length > 3) {
            holder.setFaultActor(tokens[3].trim());
        }
        
        return holder;
    }
    
    /**
     * Validates the fault expression.
     * @param faultExpression
     * @return false if invalid otherwise true
     */
    private static boolean isValid(String faultExpression) {
        if (!StringUtils.hasText(faultExpression)) {
            return false;
        }
        
        if (!(faultExpression.startsWith(DECORATION_PREFIX) && faultExpression.endsWith(DECORATION_SUFFIX))) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        String prefix = QNameUtils.getPrefix(soapFaultDefinition.getFaultCode());
        if (StringUtils.hasLength(soapFaultDefinition.getFaultCode().getNamespaceURI()) && StringUtils.hasLength(prefix)) {
            builder.append(decorate(DECORATION_PREFIX + soapFaultDefinition.getFaultCode().getNamespaceURI() + DECORATION_SUFFIX + prefix + ":" + soapFaultDefinition.getFaultCode().getLocalPart()));
        } else if (StringUtils.hasLength(soapFaultDefinition.getFaultCode().getNamespaceURI())) {
            builder.append(decorate(DECORATION_PREFIX + soapFaultDefinition.getFaultCode().getNamespaceURI() + DECORATION_SUFFIX + soapFaultDefinition.getFaultCode().getLocalPart()));
        } else {
            builder.append(decorate(soapFaultDefinition.getFaultCode().getLocalPart()));
        }
        
        if (StringUtils.hasText(soapFaultDefinition.getFaultStringOrReason())) {
            builder.append(decorate(soapFaultDefinition.getFaultStringOrReason()));
            
            if (soapFaultDefinition.getLocale() != null) {
                builder.append(decorate(soapFaultDefinition.getLocale().toString()));
            }
            
            if (faultActor != null) {
                builder.append(decorate(faultActor));
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Adds token value decoration according to syntax.
     * @return
     */
    private static String decorate(String value) {
        return DECORATION_PREFIX + value + DECORATION_SUFFIX;
    }
    
    /**
     * Sets fault code from String representation.
     * @param code
     */
    public void setFaultCode(String code) {
        soapFaultDefinition.setFaultCode(QNameUtils.parseQNameString(code));
    }
    
    /**
     * Sets the locale from String.
     * @param locale
     */
    public void setLocale(String locale) {
        LocaleEditor localeEditor = new LocaleEditor();
        localeEditor.setAsText(locale);
        soapFaultDefinition.setLocale((Locale) localeEditor.getValue());
    }
    
    /**
     * Set fault reason string.
     * @param trim
     */
    public void setFaultStringOrReason(String faultStringOrReason) {
        soapFaultDefinition.setFaultStringOrReason(faultStringOrReason);
    }
    
    /**
     * Sets the fault actor on the SOAP fault definition.
     * @param faultActor
     */
    public void setFaultActor(String faultActor) {
        this.faultActor = faultActor;
    }
    
    /**
     * Gets the faultActor.
     * @return the faultActor the faultActor to get.
     */
    public String getFaultActor() {
        return faultActor;
    }

    /**
     * Gets the soapFaultDefinition.
     * @return the soapFaultDefinition the soapFaultDefinition to get.
     */
    public SoapFaultDefinition getSoapFaultDefinition() {
        return soapFaultDefinition;
    }

    /**
     * Sets the soapFaultDefinition.
     * @param soapFaultDefinition the soapFaultDefinition to set
     */
    public void setSoapFaultDefinition(SoapFaultDefinition soapFaultDefinition) {
        this.soapFaultDefinition = soapFaultDefinition;
    }
}
