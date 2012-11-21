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

package com.consol.citrus.xml.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

/**
 * Wrapper implementation takes care of nested WSDL schema types. Exposes those WSDL schema types as
 * xsd schema instances for schema repository. WSDL may contain several schema types which get
 * exposed under a single target namespace (defined on WSDL level).
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class WsdlXsdSchema extends SimpleXsdSchema implements InitializingBean {

    /** WSDL file resource */
    private Resource wsdl;
    
    /** List of schemas that are loaded as single schema instance */
    private List<Resource> schemas = new ArrayList<Resource>();
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WsdlXsdSchema.class);

    /** Official xmlns namespace */
    private static final String WWW_W3_ORG_2000_XMLNS = "http://www.w3.org/2000/xmlns/";
    public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * Default constructor
     */
    public WsdlXsdSchema() {
        super();
    }
    
    /**
     * Constructor using wsdl resource.
     * @param wsdl
     */
    public WsdlXsdSchema(Resource wsdl) {
        super();
        this.wsdl = wsdl;
    }
    
    @Override
    public XmlValidator createValidator() throws IOException {
        return XmlValidatorFactory.createValidator(schemas.toArray(new Resource[] {}), W3C_XML_SCHEMA_NS_URI);
    }
    
    /**
     * Loads nested schema type definitions from wsdl.
     * @throws IOException 
     * @throws WSDLException 
     * @throws TransformerFactoryConfigurationError 
     * @throws TransformerException 
     * @throws TransformerConfigurationException 
     */
    private void loadSchemas() throws WSDLException, IOException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
        Definition definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdl.getFile().getAbsolutePath());
        
        Types types = definition.getTypes();
        List<?> schemaTypes = types.getExtensibilityElements();
        
        for (Object schemaObject : schemaTypes) {
            if (schemaObject instanceof SchemaImpl) {
                SchemaImpl schema = (SchemaImpl) schemaObject;

                inheritNamespaces(schema, definition);
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Source source = new DOMSource(schema.getElement());
                Result result = new StreamResult(bos);
                
                TransformerFactory.newInstance().newTransformer().transform(source, result);
                Resource schemaResource = new ByteArrayResource(bos.toByteArray());
                
                schemas.add(schemaResource);
                
                if (definition.getTargetNamespace().equals(schema.getElement().getAttribute("targetNamespace"))) {
                    setXsd(schemaResource);
                }
            } else {
                log.warn("Found unsupported schema type implementation " + schemaObject.getClass());
            }
        }
    }
    
    /**
     * Adds WSDL level namespaces to schema definition if necessary.
     * @param schema
     * @param wsdl
     */
    @SuppressWarnings("unchecked")
    private void inheritNamespaces(SchemaImpl schema, Definition wsdl) {
        Map<String, String> wsdlNamespaces = wsdl.getNamespaces();
        
        for (Entry<String, String> nsEntry: wsdlNamespaces.entrySet()) {
            if (StringUtils.hasText(nsEntry.getKey())) {
                if (!schema.getElement().hasAttributeNS(WWW_W3_ORG_2000_XMLNS, nsEntry.getKey())) {
                    schema.getElement().setAttributeNS(WWW_W3_ORG_2000_XMLNS, "xmlns:" + nsEntry.getKey(), nsEntry.getValue());
                }
            } else { // handle default namespace
                if (!schema.getElement().hasAttribute("xmlns")) {
                    schema.getElement().setAttributeNS(WWW_W3_ORG_2000_XMLNS, "xmlns" + nsEntry.getKey(), nsEntry.getValue());
                }
            }
            
        }
    }

    @Override
    public void afterPropertiesSet() throws ParserConfigurationException, IOException, SAXException {
        Assert.notNull(wsdl, "wsdl file resource is required");
        Assert.isTrue(wsdl.exists(), "wsdl file resource '" + wsdl + " does not exist");
        
        try {
            loadSchemas();
        } catch (WSDLException e) {
            throw new BeanCreationException("Failed to load schema types from WSDL file", e);
        } catch (TransformerException e) {
            throw new BeanCreationException("Failed to load schema types from WSDL file", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new BeanCreationException("Failed to load schema types from WSDL file", e);
        }
        
        Assert.isTrue(!schemas.isEmpty(), "no schema types found in wsdl file resource");
        
        super.afterPropertiesSet();
    }

    /**
     * Sets the wsdl.
     * @param wsdl the wsdl to set
     */
    public void setWsdl(Resource wsdl) {
        this.wsdl = wsdl;
    }

    /**
     * Gets the schemas.
     * @return the schemas the schemas to get.
     */
    public List<Resource> getSchemas() {
        return schemas;
    }
}
