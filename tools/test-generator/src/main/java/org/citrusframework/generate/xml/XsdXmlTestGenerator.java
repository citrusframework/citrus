/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.xml;

import java.util.Collections;
import java.util.Map;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.XsdTestGenerator;
import org.citrusframework.generate.dictionary.InboundXmlDataDictionary;
import org.citrusframework.generate.dictionary.OutboundXmlDataDictionary;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.xml.XmlConfigurer;

/**
 * Test generator creates one to many test cases based on operations defined in a XML schema XSD.
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class XsdXmlTestGenerator extends MessagingXmlTestGenerator<XsdXmlTestGenerator> implements XsdTestGenerator<XsdXmlTestGenerator> {

    private String xsd;

    private String requestMessage;
    private String responseMessage;

    private String nameSuffix = "IT";

    private final InboundXmlDataDictionary inboundDataDictionary = new InboundXmlDataDictionary();
    private final OutboundXmlDataDictionary outboundDataDictionary = new OutboundXmlDataDictionary();

    @Override
    public void create() {
        SchemaTypeSystem schemaTypeSystem = compileXsd(xsd);
        SchemaType[] globalElems = schemaTypeSystem.documentTypes();

        SchemaType requestElem = null;
        SchemaType responseElem = null;

        if (!StringUtils.hasText(getName())) {
            withName(getTestNameSuggestion());
        }

        if (!StringUtils.hasText(responseMessage)) {
            responseMessage = getResponseMessageSuggestion();
        }

        for (SchemaType elem : globalElems) {
            if (elem.getContentModel().getName().getLocalPart().equals(requestMessage)) {
                requestElem = elem;
                break;
            }
        }

        for (SchemaType elem : globalElems) {
            if (elem.getContentModel().getName().getLocalPart().equals(responseMessage)) {
                responseElem = elem;
                break;
            }
        }

        if (requestElem != null) {
            withRequest(new DefaultMessage(SampleXmlUtil.createSampleForType(requestElem)));
        } else {
            throw new CitrusRuntimeException(String.format("Unable to find element with name '%s' in XSD %s", requestMessage, xsd));
        }

        if (responseElem != null) {
            withResponse(new DefaultMessage(SampleXmlUtil.createSampleForType(responseElem)));
        } else {
            withResponse(null);
        }

        XmlConfigurer configurer = new XmlConfigurer();
        configurer.initialize();
        configurer.setSerializeSettings(Collections.singletonMap(XmlConfigurer.XML_DECLARATION, false));
        XMLUtils.initialize(configurer);

        super.create();
    }

    @Override
    protected Message generateInboundMessage(Message message) {
        inboundDataDictionary.process(message, new TestContext());
        return super.generateInboundMessage(message);
    }

    @Override
    protected Message generateOutboundMessage(Message message) {
        outboundDataDictionary.process(message, new TestContext());
        return super.generateOutboundMessage(message);
    }

    /**
     * Suggest name of response element based on request message element name.
     * @return
     */
    public String getResponseMessageSuggestion() {
        String suggestion;
        if (requestMessage.endsWith("Req")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("Req")) + "Res";
        } else if (requestMessage.endsWith("Request")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("Request")) + "Response";
        } else if (requestMessage.endsWith("RequestMessage")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("RequestMessage")) + "ResponseMessage";
        } else {
            suggestion = "";
        }

        return suggestion;
    }

    /**
     * Suggest name of test based on request message element name.
     * @return
     */
    public String getTestNameSuggestion() {
        String suggestion;
        if (requestMessage.endsWith("Req")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("Req")) + nameSuffix;
        } else if (requestMessage.endsWith("Request")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("Request")) + nameSuffix;
        } else if (requestMessage.endsWith("RequestMessage")) {
            suggestion = requestMessage.substring(0, requestMessage.indexOf("RequestMessage")) + nameSuffix;
        } else {
            suggestion = requestMessage + nameSuffix;
        }

        return suggestion;
    }

    /**
     * Finds nested XML schema definition and compiles it to a schema type system instance
     * @param xsd
     * @return
     */
    private SchemaTypeSystem compileXsd(String xsd) {
        Resource xsdFile = Resources.create(xsd);
        if (!xsdFile.exists()) {
            throw new CitrusRuntimeException("Unable to read XSD - does not exist in " + xsdFile.getLocation());
        }

        XmlObject xsdObject;
        try {
            xsdObject = XmlObject.Factory.parse(xsdFile.getFile(), (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to parse XSD schema", e);
        }
        XmlObject[] schemas = new XmlObject[] { xsdObject };
        try {
            return XmlBeans.compileXsd(schemas, XmlBeans.getContextTypeLoader(), new XmlOptions());
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to compile XSD schema", e);
        }
    }

    /**
     * Set the xsd schema resource to use.
     * @param xsdResource
     * @return
     */
    public XsdXmlTestGenerator withXsd(String xsdResource) {
        this.xsd = xsdResource;
        return this;
    }

    /**
     * Set the request element name in xsd resource to use.
     * @param requestMessage
     * @return
     */
    public XsdXmlTestGenerator withRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
        return this;
    }

    /**
     * Set the response element name in xsd resource to use.
     * @param responseMessage
     * @return
     */
    public XsdXmlTestGenerator withResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
        return this;
    }

    /**
     * Set the test name suffix to use.
     * @param suffix
     * @return
     */
    public XsdXmlTestGenerator withNameSuffix(String suffix) {
        this.nameSuffix = suffix;
        return this;
    }

    /**
     * Add inbound XPath expression mappings to manipulate inbound message content.
     * @param mappings
     * @return
     */
    public XsdXmlTestGenerator withInboundMappings(Map<String, String> mappings) {
        this.inboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add outbound XPath expression mappings to manipulate outbound message content.
     * @param mappings
     * @return
     */
    public XsdXmlTestGenerator withOutboundMappings(Map<String, String> mappings) {
        this.outboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add inbound XPath expression mappings file to manipulate inbound message content.
     * @param mappingFile
     * @return
     */
    public XsdXmlTestGenerator withInboundMappingFile(String mappingFile) {
        this.inboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.inboundDataDictionary.initialize();
        return this;
    }

    /**
     * Add outbound XPath expression mappings file to manipulate outbound message content.
     * @param mappingFile
     * @return
     */
    public XsdXmlTestGenerator withOutboundMappingFile(String mappingFile) {
        this.outboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.outboundDataDictionary.initialize();
        return this;
    }



    /**
     * Sets the xsd.
     *
     * @param xsd
     */
    public void setXsd(String xsd) {
        this.xsd = xsd;
    }

    /**
     * Gets the xsd.
     *
     * @return
     */
    public String getXsd() {
        return xsd;
    }

    /**
     * Sets the requestMessage.
     *
     * @param requestMessage
     */
    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    /**
     * Gets the requestMessage.
     *
     * @return
     */
    public String getRequestMessage() {
        return requestMessage;
    }

    /**
     * Sets the responseMessage.
     *
     * @param responseMessage
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * Gets the responseMessage.
     *
     * @return
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the nameSuffix.
     *
     * @param nameSuffix
     */
    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    /**
     * Gets the nameSuffix.
     *
     * @return
     */
    public String getNameSuffix() {
        return nameSuffix;
    }
}
