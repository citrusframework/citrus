/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.ws.message;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.util.StringUtils;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.xml.namespace.QNameEditor;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;

import javax.xml.namespace.QName;
import javax.xml.transform.*;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class SoapFault extends SoapMessage {

    /** Syntax decoration prefix/suffix */
    private static final String DECORATION_SUFFIX = "}";
    private static final String DECORATION_PREFIX = "{";

    /** Fault code as QName string */
    private String faultCode;

    /** Fault reason string describing the fault */
    private String faultString;

    /** Optional fault actor */
    private String faultActor;

    /** Locale used in fault */
    private Locale locale = Locale.ENGLISH;

    /** List of fault detail elements */
    private List<String> faultDetails = new ArrayList<String>();

    /**
     * Default constructor using message payload.
     */
    public SoapFault() {
        super("");
    }

    /**
     * Returns fault code as qualified name.
     * @return
     */
    public QName getFaultCodeQName() {
        return QNameUtils.parseQNameString(faultCode);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        QName faultCodeQName = getFaultCodeQName();
        if (StringUtils.hasLength(faultCodeQName.getNamespaceURI()) && StringUtils.hasLength(faultCodeQName.getPrefix())) {
            builder.append(decorate(DECORATION_PREFIX + faultCodeQName.getNamespaceURI() + DECORATION_SUFFIX + faultCodeQName.getPrefix() + ":" + faultCodeQName.getLocalPart()));
        } else if (StringUtils.hasLength(faultCodeQName.getNamespaceURI())) {
            builder.append(decorate(DECORATION_PREFIX + faultCodeQName.getNamespaceURI() + DECORATION_SUFFIX + faultCodeQName.getLocalPart()));
        } else {
            builder.append(decorate(faultCodeQName.getLocalPart()));
        }

        if (StringUtils.hasText(getFaultString())) {
            builder.append(decorate(getFaultString()));

            if (getLocale() != null) {
                builder.append(decorate(getLocale().toString()));
            }

            if (faultActor != null) {
                builder.append(decorate(faultActor));
            }
        }

        return super.toString() + String.format("[fault: %s]", builder.toString());
    }

    /**
     * Adds token value decoration according to syntax.
     * @return
     */
    private static String decorate(String value) {
        return DECORATION_PREFIX + value + DECORATION_SUFFIX;
    }

    /**
     * Sets the faultActor.
     * @param faultActor the faultActor to set
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
     * Gets the faultCode.
     * @return the faultCode
     */
    public String getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the fault code.
     * @param faultCode
     */
    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    /**
     * Gets the faultString.
     * @return the faultString
     */
    public String getFaultString() {
        return faultString;
    }

    /**
     * Sets the fault string or reason.
     * @param faultString
     */
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    /**
     * Gets the faultDetails.
     * @return the faultDetails the faultDetails to get.
     */
    public List<String> getFaultDetails() {
        return faultDetails;
    }

    /**
     * Sets the faultDetails.
     * @param faultDetails the faultDetails to set
     */
    public void setFaultDetails(List<String> faultDetails) {
        this.faultDetails = faultDetails;
    }

    /**
     * Adds a new fault detail in builder pattern style.
     * @param faultDetail
     * @return
     */
    public SoapFault addFaultDetail(String faultDetail) {
        this.faultDetails.add(faultDetail);
        return this;
    }

    /**
     * Gets the locale used in SOAP fault.
     * @return
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used in SOAP fault.
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the locale used in SOAP fault.
     * @param locale
     */
    public void setLocale(String locale) {
        LocaleEditor localeEditor = new LocaleEditor();
        localeEditor.setAsText(locale);
        this.locale = (Locale) localeEditor.getValue();
    }

    /**
     * Builder method from Spring WS SOAP fault object.
     * @param fault
     * @return
     */
    public static SoapFault from(org.springframework.ws.soap.SoapFault fault) {
        SoapFault soapFault = new SoapFault();

        QNameEditor qNameEditor = new QNameEditor();
        qNameEditor.setValue(fault.getFaultCode());

        soapFault.setFaultCode(qNameEditor.getAsText());
        soapFault.setFaultActor(fault.getFaultActorOrRole());
        soapFault.setFaultString(fault.getFaultStringOrReason());

        if (fault.getFaultDetail() != null) {
            Iterator<SoapFaultDetailElement> details = fault.getFaultDetail().getDetailEntries();
            while (details.hasNext()) {
                SoapFaultDetailElement soapFaultDetailElement = details.next();
                soapFault.addFaultDetail(extractFaultDetail(soapFaultDetailElement));
            }
        }

        return soapFault;
    }

    /**
     * Extracts fault detail string from soap fault detail instance. Transforms detail source
     * into string and takes care.
     *
     * @param detail
     * @return
     */
    private static String extractFaultDetail(SoapFaultDetailElement detail) {
        StringResult detailResult = new StringResult();

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            transformer.transform(detail.getSource(), detailResult);
        } catch (TransformerException e) {
            throw new CitrusRuntimeException(e);
        }

        return detailResult.toString();
    }
}
