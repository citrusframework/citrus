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

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.xml.XsdSchemaRepository;
import com.consol.citrus.xml.schema.TargetNamespaceSchemaMappingStrategy;
import com.consol.citrus.xml.schema.WsdlXsdSchema;
import com.consol.citrus.xml.schema.XsdSchemaMappingStrategy;
import org.springframework.util.StringUtils;
import org.springframework.xml.xsd.XsdSchema;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Message send action able to add SOAP attachment support to normal message sending action.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction {

    private static Logger log = LoggerFactory.getLogger(SendSoapMessageAction.class);
    
    /** SOAP attachment */
    private List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();

    /** enable/disable mtom attachments */
    private Boolean mtomEnabled = false;
    
    /** Explicit schema repository to use for this validation */
    private String schemaRepository = "schemaRepository";
    
    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        Message message = super.createMessage(context, getMessageType());
        List<SoapAttachment> soapAttachments = new ArrayList<SoapAttachment>();

        try {
            if (!attachments.isEmpty()) {
                String messagePayload = message.getPayload().toString();
                
                for (SoapAttachment attachment : attachments) {
                    // handle variables in content id
                    if (attachment.getContentId() != null) {
                        attachment.setContentId(context.replaceDynamicContentInString(attachment.getContentId()));
                    }

                    // handle variables in content type
                    if (attachment.getContentType() != null) {
                        attachment.setContentType(context.replaceDynamicContentInString(attachment.getContentType()));
                    }

                    if (StringUtils.hasText(attachment.getContent())) {
                        attachment.setContent(context.replaceDynamicContentInString(attachment.getContent()));
                    } else if (attachment.getContentResourcePath() != null) {
                        if (attachment.getContentType().startsWith("text/"))
                            attachment.setContent(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(attachment.getContentResourcePath(), context))));
                        else
                            attachment.setContentResourcePath(context.replaceDynamicContentInString(attachment.getContentResourcePath()));
                    }

                    if (mtomEnabled) {
                        String cid = "cid:" + attachment.getContentId();

                        if (attachment.getMtomInline()) {
                            if (messagePayload.contains(cid) && attachment.getInputStream().available() > 0) {
                                String xsiType = getAttachmentXsiType(context, messagePayload, cid);

                                if (xsiType.equals("base64Binary")) {
                                    messagePayload = messagePayload.replaceAll(cid, Base64.encodeBase64String(IOUtils.toByteArray(attachment.getInputStream())));
                                } else if (xsiType.equals("hexBinary")) {
                                    messagePayload = messagePayload.replaceAll(cid, Hex.encodeHexString(IOUtils.toByteArray(attachment.getInputStream())).toUpperCase());
                                } else {
                                    throw new CitrusRuntimeException("Unsupported xsiType<" + xsiType + "> for attachment " + cid);
                                }
                                attachment = null;
                            }
                        } else {
                            messagePayload = messagePayload.replaceAll(cid, "<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"" + cid + "\"/>");
                            soapAttachments.add(attachment);
                        }
                    } else {
                        soapAttachments.add(attachment);
                    }
                }
                
                if (mtomEnabled)
                    message.setPayload(messagePayload);
            }

        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        final SoapMessage soapMessage = new SoapMessage(message);
        soapMessage.setMtomEnabled(mtomEnabled);
        
        for (SoapAttachment attachment : soapAttachments) {
            soapMessage.addAttachment(attachment);
        }

        return soapMessage;
    }

    /**
     * Gets the attachments.
     * @return the attachments
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the control attachments.
     * @param attachments the control attachments
     */
    public void setAttachments(List<SoapAttachment> attachments) {
        this.attachments = attachments;
    }
    
    /**
     * Enable or disable mtom attachments
     * @param mtomEnabled
     */
    public void setMtomEnabled(Boolean enable) {
        this.mtomEnabled = enable;
    }

    /**
     * Gets mtom attachments enabled
     * @return 
     */
    public Boolean getMtomEnabled() {
        return this.mtomEnabled;
    }

    /**
     * Gets the schemaRepository.
     * @return the schemaRepository the schemaRepository to get.
     */
    public String getSchemaRepository() {
        return schemaRepository;
    }

    /**
     * Sets the schemaRepository.
     * @param schemaRepository the schemaRepository to set
     */
    public void setSchemaRepository(String schemaRepository) {
        this.schemaRepository = schemaRepository;
    }
    
    /**
     * Get data type from XML node. Supported data types are "base64binary" and "hexBinary"
     * @param context
     * @param xmlMessage
     * @param cid
     * @return 
     */
    private String getAttachmentXsiType(TestContext context, String xmlMessage, String cid) {
        String xsiType = "base64Binary";
        XsdSchemaRepository schemaRepo = context.getApplicationContext().getBean(schemaRepository, XsdSchemaRepository.class);
        if (schemaRepo != null) {
            XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();
            XsdSchema schema = schemaMappingStrategy.getSchema(
                    schemaRepo.getSchemas(), XMLUtils.parseMessagePayload(xmlMessage));
            if (schema == null) {
                log.error("No matching schema found to parse the attachment xml element for cid: " + cid);
            } else {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    dbf.setValidating(false);

                    if (schema instanceof WsdlXsdSchema) {
                        dbf.setSchema(((WsdlXsdSchema)schema).getCombinedSchema());
                    } else {
                        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        dbf.setSchema(sf.newSchema(schema.getSource()));
                    }

                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new InputSource(new StringReader(xmlMessage)));
                    doc.getDocumentElement().normalize();

                    XPath xPath = XPathFactory.newInstance().newXPath();
                    String expression = "//*[contains(normalize-space(text()), '" + cid + "')]";
                    Node node = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);
                    if (node instanceof Element) {
                        xsiType = ((Element) node).getSchemaTypeInfo().getTypeName();
                    } else {
                        log.warn("parent element of cid: " + cid + " not found in xml payload.");
                    }
                } catch (SAXException | ParserConfigurationException | IOException | XPathExpressionException e) {
                    log.warn(e.getLocalizedMessage(), e);
                }
            }
        }

        return xsiType;
    }
}
