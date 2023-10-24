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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xsd2inst.SampleXmlUtil;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.generate.WsdlTestGenerator;
import org.citrusframework.generate.dictionary.InboundXmlDataDictionary;
import org.citrusframework.generate.dictionary.OutboundXmlDataDictionary;
import org.citrusframework.message.Message;
import org.citrusframework.model.testcase.ws.ObjectFactory;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.xml.XmlConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test generator creates one to many test cases based on operations defined in a XML schema XSD.
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class WsdlXmlTestGenerator extends MessagingXmlTestGenerator<WsdlXmlTestGenerator> implements WsdlTestGenerator<WsdlXmlTestGenerator> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(WsdlXmlTestGenerator.class);

    private final static Pattern COUNT_NS = Pattern.compile("xmlns:");
    private String wsdl;

    private String operation;
    private String namePrefix;
    private String nameSuffix = "_IT";

    private final InboundXmlDataDictionary inboundDataDictionary = new InboundXmlDataDictionary();
    private final OutboundXmlDataDictionary outboundDataDictionary = new OutboundXmlDataDictionary();

    @Override
    public void create() {
        String wsdlNsDelaration = "declare namespace wsdl='http://schemas.xmlsoap.org/wsdl/' ";
        String soapNsDelaration = "declare namespace soap='http://schemas.xmlsoap.org/wsdl/soap/' ";

        // compile wsdl and xsds right now, otherwise later input is useless:
        XmlObject wsdlObject = compileWsdl(wsdl);
        SchemaTypeSystem schemaTypeSystem = compileXsd(wsdlObject);

        logger.info("WSDL compilation successful");
        String serviceName = evaluateAsString(wsdlObject, wsdlNsDelaration + ".//wsdl:portType/@name");
        logger.info("Found service: " + serviceName);

        if (!StringUtils.hasText(namePrefix)) {
            withNamePrefix(serviceName + "_");
        }

        logger.info("Found service operations:");
        XmlObject[] messages = wsdlObject.selectPath(wsdlNsDelaration + ".//wsdl:message");
        XmlObject[] operations = wsdlObject.selectPath(wsdlNsDelaration + ".//wsdl:portType/wsdl:operation");
        for (XmlObject operation : operations) {
            logger.info(evaluateAsString(operation, wsdlNsDelaration + "./@name"));
        }
        logger.info("Generating test cases for service operations ...");

        for (XmlObject operation : operations) {
            SoapMessage request = new SoapMessage();
            SoapMessage response = new SoapMessage();

            String operationName = evaluateAsString(operation, wsdlNsDelaration + "./@name");
            if (StringUtils.hasText(this.operation) && !operationName.equals(this.operation)) {
                continue;
            }

            XmlObject[] bindingOperations = wsdlObject.selectPath(wsdlNsDelaration + ".//wsdl:binding/wsdl:operation");
            for (XmlObject bindingOperation : bindingOperations) {
                String bindingOperationName = evaluateAsString(bindingOperation, wsdlNsDelaration + "./@name");

                if (bindingOperationName.equals(operationName)) {
                    String soapAction = removeNsPrefix(evaluateAsString(bindingOperation, soapNsDelaration + "./soap:operation/@soapAction"));
                    request.soapAction(soapAction);
                    break;
                }
            }

            String inputMessage = removeNsPrefix(evaluateAsString(operation, wsdlNsDelaration + "./wsdl:input/@message"));
            String outputMessage = removeNsPrefix(evaluateAsString(operation, wsdlNsDelaration + "./wsdl:output/@message"));

            String inputElement = null;
            String outputElement = null;
            for (XmlObject message : messages) {
                String messageName = evaluateAsString(message, wsdlNsDelaration + "./@name");

                if (messageName.equals(inputMessage)) {
                    inputElement = removeNsPrefix(evaluateAsString(message, wsdlNsDelaration + "./wsdl:part/@element"));
                }

                if (messageName.equals(outputMessage)) {
                    outputElement = removeNsPrefix(evaluateAsString(message, wsdlNsDelaration + "./wsdl:part/@element"));
                }
            }

            // Now generate it
            withName(namePrefix + operationName + nameSuffix);

            SchemaType requestElem = getSchemaType(schemaTypeSystem, operationName, inputElement);
            request.setPayload(SampleXmlUtil.createSampleForType(requestElem));
            withRequest(request);

            SchemaType responseElem = getSchemaType(schemaTypeSystem, operationName, outputElement);
            response.setPayload(SampleXmlUtil.createSampleForType(responseElem));
            withResponse(response);

            XmlConfigurer configurer = new XmlConfigurer();
            configurer.initialize();
            configurer.setSerializeSettings(Collections.singletonMap(XmlConfigurer.XML_DECLARATION, false));
            XMLUtils.initialize(configurer);

            super.create();

            logger.info("Successfully created new test case " + getTargetPackage() + "." + getName());
        }
    }

    @Override
    protected List<String> getMarshallerContextPaths() {
        List<String> contextPaths = super.getMarshallerContextPaths();
        contextPaths.add(ObjectFactory.class.getPackage().getName());
        return contextPaths;
    }

    @Override
    protected List<Resource> getMarshallerSchemas() {
        List<Resource> schemas = super.getMarshallerSchemas();
        schemas.add(Resources.fromClasspath("org/citrusframework/schema/citrus-http-testcase.xsd"));
        schemas.add(Resources.fromClasspath("org/citrusframework/schema/citrus-ws-testcase.xsd"));
        return schemas;
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
     * Finds nested XML schema definition and compiles it to a schema type system instance
     * @param wsdl
     * @return
     */
    private XmlObject compileWsdl(String wsdl) {
        File wsdlFile;
        try {
            wsdlFile = Resources.create(wsdl).getFile();
        } catch (Exception e) {
            wsdlFile = new File(wsdl);
        }

        if (!wsdlFile.exists()) {
            throw new CitrusRuntimeException("Unable to read WSDL - does not exist in " + wsdlFile.getAbsolutePath());
        }

        if (!wsdlFile.canRead()) {
            throw new CitrusRuntimeException("Unable to read WSDL - could not open in read mode");
        }

        try {
            return XmlObject.Factory.parse(wsdlFile, (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
        } catch (XmlException e) {
            for (Object error : e.getErrors()) {
                logger.error(((XmlError)error).getLine() + "" + error.toString());
            }
            throw new CitrusRuntimeException("WSDL could not be parsed", e);
        } catch (Exception e) {
            throw new CitrusRuntimeException("WSDL could not be parsed", e);
        }
    }

    /**
     * Finds nested XML schema definition and compiles it to a schema type system instance.
     * @param wsdl
     * @return
     */
    private SchemaTypeSystem compileXsd(XmlObject wsdl) {
        // extract namespaces defined on wsdl-level:
        String[] namespacesWsdl = extractNamespacesOnWsdlLevel(wsdl);

        // calc the namespace-prefix of the schema-tag, default ""
        String schemaNsPrefix = extractSchemaNamespacePrefix(wsdl);

        // extract each schema-element and add missing namespaces defined on wsdl-level
        String[] schemas = getNestedSchemas(wsdl, namespacesWsdl, schemaNsPrefix);

        XmlObject[] xsd = new XmlObject[schemas.length];
        try {
            for (int i=0; i < schemas.length; i++) {
                xsd[i] = XmlObject.Factory.parse(schemas[i], (new XmlOptions()).setLoadLineNumbers().setLoadMessageDigest().setCompileDownloadUrls());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to parse XSD schema", e);
        }

        SchemaTypeSystem schemaTypeSystem = null;
        try {
            schemaTypeSystem = XmlBeans.compileXsd(xsd, XmlBeans.getContextTypeLoader(), new XmlOptions());
        } catch (XmlException e) {
            for (Object error : e.getErrors()) {
                logger.error("Line " + ((XmlError)error).getLine() + ": " + error.toString());
            }
            throw new CitrusRuntimeException("Failed to compile XSD schema", e);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to compile XSD schema", e);
        }
        return schemaTypeSystem;
    }

    /**
     * @param schemaTypeSystem
     * @param operation
     * @param elementName
     * @return
     */
    private SchemaType getSchemaType(SchemaTypeSystem schemaTypeSystem, String operation, String elementName) {

        for (SchemaType elem : schemaTypeSystem.documentTypes()) {
            if (elem.getContentModel().getName().getLocalPart().equals(elementName)) {
                return elem;
            }
        }

        throw new CitrusRuntimeException("Unable to find schema type declaration '" + elementName + "'" +
                " for WSDL operation '" + operation + "'");
    }

    /**
     * Removes namespace prefix if present.
     * @param elementName
     * @return
     */
    private String removeNsPrefix(String elementName) {
        return elementName.indexOf(':') != -1 ? elementName.substring(elementName.indexOf(':') + 1) : elementName;
    }

    /**
     * Finds nested schema definitions and puts globally WSDL defined namespaces to schema level.
     *
     * @param wsdl
     * @param namespacesWsdl
     * @param schemaNsPrefix
     */
    private String[] getNestedSchemas(XmlObject wsdl, String[] namespacesWsdl, String schemaNsPrefix) {
        List<String> schemas = new ArrayList<>();
        String openedStartTag = "<" + schemaNsPrefix + "schema";
        String endTag = "</" + schemaNsPrefix + "schema>";

        int cursor = 0;
        while (wsdl.xmlText().indexOf(openedStartTag, cursor) != -1) {
            int begin = wsdl.xmlText().indexOf(openedStartTag, cursor);
            int end = wsdl.xmlText().indexOf(endTag, begin) + endTag.length();
            int insertPointNamespacesWsdl = wsdl.xmlText().indexOf(" ", begin);

            StringBuilder builder = new StringBuilder();
            builder.append(wsdl.xmlText().substring(begin, insertPointNamespacesWsdl)).append(" ");

            for (String nsWsdl : namespacesWsdl) {
                String nsPrefix = nsWsdl.substring(0, nsWsdl.indexOf("="));
                if (!wsdl.xmlText().substring(begin, end).contains(nsPrefix)) {
                    builder.append(nsWsdl).append(" ");
                }
            }

            builder.append(wsdl.xmlText().substring(insertPointNamespacesWsdl, end));
            schemas.add(builder.toString());
            cursor = end;
        }

        return schemas.toArray(new String[] {});
    }

    /**
     * Finds schema tag and extracts the namespace prefix.
     * @param wsdl
     * @return
     */
    private String extractSchemaNamespacePrefix(XmlObject wsdl) {
        String schemaNsPrefix = "";
        if (wsdl.xmlText().contains(":schema")) {
            int cursor = wsdl.xmlText().indexOf(":schema");
            for (int i = cursor; i > cursor - 100; i--) {
                schemaNsPrefix = wsdl.xmlText().substring(i, cursor);
                if (schemaNsPrefix.startsWith("<")) {
                    return schemaNsPrefix.substring(1) + ":";
                }
            }
        }
        return schemaNsPrefix;
    }

    /**
     * Returns an array of all namespace declarations, found on wsdl-level.
     *
     * @param wsdl
     * @return
     */
    private String[] extractNamespacesOnWsdlLevel(XmlObject wsdl) {
        int cursor = wsdl.xmlText().indexOf(":") + ":definitions ".length();
        String nsWsdlOrig = wsdl.xmlText().substring(cursor, wsdl.xmlText().indexOf(">", cursor));
        int noNs = (int) COUNT_NS.matcher(nsWsdlOrig).results().count();
        String[] namespacesWsdl = new String[noNs];
        cursor = 0;
        for (int i=0; i<noNs; i++) {
            int begin = nsWsdlOrig.indexOf("xmlns:", cursor);
            int end = nsWsdlOrig.indexOf("\"", begin + 20);
            namespacesWsdl[i] = nsWsdlOrig.substring(begin, end) + "\"";
            cursor = end;
        }
        return namespacesWsdl;
    }

    /**
     * Returns the value of an xml-attribute
     *
     * @param rootObject
     * @param pathToAttribute
     * @return
     */
    private String evaluateAsString(XmlObject rootObject, String pathToAttribute) {
        XmlObject[] xmlObject = rootObject.selectPath(pathToAttribute);

        if (xmlObject.length == 0) {
            throw new CitrusRuntimeException("Unable to find element attribute " + pathToAttribute);
        }

        int begin = xmlObject[0].xmlText().indexOf(">") + 1;
        int end = xmlObject[0].xmlText().lastIndexOf("</");
        return xmlObject[0].xmlText().substring(begin, end);
    }

    /**
     * Set the wsdl schema resource to use.
     * @param wsdlResource
     * @return
     */
    public WsdlXmlTestGenerator withWsdl(String wsdlResource) {
        this.wsdl = wsdlResource;
        return this;
    }

    /**
     * Set the test name suffix to use.
     * @param suffix
     * @return
     */
    public WsdlXmlTestGenerator withNameSuffix(String suffix) {
        this.nameSuffix = suffix;
        return this;
    }

    /**
     * Set the test name prefix to use.
     * @param prefix
     * @return
     */
    public WsdlXmlTestGenerator withNamePrefix(String prefix) {
        this.namePrefix = prefix;
        return this;
    }

    /**
     * Set the wsdl operation to use.
     * @param operation
     * @return
     */
    public WsdlXmlTestGenerator withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Add inbound XPath expression mappings to manipulate inbound message content.
     * @param mappings
     * @return
     */
    public WsdlXmlTestGenerator withInboundMappings(Map<String, String> mappings) {
        this.inboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add outbound XPath expression mappings to manipulate outbound message content.
     * @param mappings
     * @return
     */
    public WsdlXmlTestGenerator withOutboundMappings(Map<String, String> mappings) {
        this.outboundDataDictionary.getMappings().putAll(mappings);
        return this;
    }

    /**
     * Add inbound XPath expression mappings file to manipulate inbound message content.
     * @param mappingFile
     * @return
     */
    public WsdlXmlTestGenerator withInboundMappingFile(String mappingFile) {
        this.inboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.inboundDataDictionary.initialize();
        return this;
    }

    /**
     * Add outbound XPath expression mappings file to manipulate outbound message content.
     * @param mappingFile
     * @return
     */
    public WsdlXmlTestGenerator withOutboundMappingFile(String mappingFile) {
        this.outboundDataDictionary.setMappingFile(Resources.create(mappingFile));
        this.outboundDataDictionary.initialize();
        return this;
    }

    /**
     * Sets the wsdl.
     *
     * @param wsdl
     */
    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    /**
     * Gets the wsdl.
     *
     * @return
     */
    public String getWsdl() {
        return wsdl;
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

    /**
     * Sets the namePrefix.
     *
     * @param namePrefix
     */
    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    /**
     * Gets the namePrefix.
     *
     * @return
     */
    public String getNamePrefix() {
        return namePrefix;
    }

    /**
     * Sets the operation.
     *
     * @param operation
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the operation.
     *
     * @return
     */
    public String getOperation() {
        return operation;
    }
}
