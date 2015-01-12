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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

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
    
    /** List of schema sources */
    private List<Source> schemaSources = new ArrayList<Source>();
    
    /** Combined WSDL schema */
    private javax.xml.validation.Schema combinedSchema = null;
    
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
    public XmlValidator createValidator() {
        try {
            return XmlValidatorFactory.createValidator(schemas.toArray(new Resource[schemas.size()]), W3C_XML_SCHEMA_NS_URI);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create validation for WSDL schema files", e);
        }
    }
    
    /**
     * Loads nested schema type definitions from wsdl.
     * @throws IOException 
     * @throws WSDLException 
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    private void loadSchemas() throws WSDLException, IOException, TransformerException, TransformerFactoryConfigurationError {
        Definition definition = null;
        if (wsdl.getURI().toString().startsWith("jar:")) {
            // Locate WSDL imports in Jar files
            definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(new JarWSDLLocator(wsdl));
        } else {
            definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdl.getURI().getPath(), new InputSource(wsdl.getInputStream()));
        }
        
        Types types = definition.getTypes();
        List<?> schemaTypes = types.getExtensibilityElements();
        Resource thisSchemaResource = null;

        Map<String, Object> importedSchemas = new HashMap<String, Object>();
        for (Object schemaObject : schemaTypes) {
            if (schemaObject instanceof SchemaImpl) {
                SchemaImpl schema = (SchemaImpl) schemaObject;

                inheritNamespaces(schema, definition);
                
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Source source = new DOMSource(schema.getElement());
                Result result = new StreamResult(bos);
                
                TransformerFactory.newInstance().newTransformer().transform(source, result);
                Resource schemaResource = new ByteArrayResource(bos.toByteArray());
                
                addImportedSchemas(schema, importedSchemas);
                schemas.add(schemaResource);
                schemaSources.add(source);
                
                // Use either the targetNamespace matching schema resource or if there is no matching target, use the first schema resource
                if (definition.getTargetNamespace().equals(schema.getElement().getAttribute("targetNamespace"))) {
                    thisSchemaResource = schemaResource;
                } else if (thisSchemaResource == null) {
                    thisSchemaResource = schemaResource;
                }
            } else {
                log.warn("Found unsupported schema type implementation " + schemaObject.getClass());
            }
        }
        setXsd(thisSchemaResource);
    }
    
    /**
     * Recursively add all imported schemas as schema resource.
     * This is necessary when schema import are located in jar files. If they are not added immediately the reference to them is lost.
     */
    void addImportedSchemas(Schema schema, Map<String, Object> importedSchemas) throws WSDLException, IOException, TransformerException, TransformerFactoryConfigurationError {
        Map imports = schema.getImports();
        for (Object schemaObjects : imports.values()) {
            Vector<SchemaImport> schemaObjectsVector = (Vector<SchemaImport>)schemaObjects;
            for (SchemaImport schemaObject : schemaObjectsVector) {
                if (schemaObject instanceof SchemaImport) {
                    SchemaImport schemaImport = (SchemaImport)schemaObject;
                    
                    // Prevent duplicate imports
                    if (!importedSchemas.containsKey(schemaImport.getNamespaceURI())) {
                        importedSchemas.put(schemaImport.getNamespaceURI(), null);
                        Schema referencedSchema = schemaImport.getReferencedSchema();

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Source source = new DOMSource(referencedSchema.getElement());
                        Result result = new StreamResult(bos);

                        TransformerFactory.newInstance().newTransformer().transform(source, result);
                        Resource schemaResource = new ByteArrayResource(bos.toByteArray());

                        addImportedSchemas(referencedSchema, importedSchemas);
                        schemas.add(schemaResource);
                        schemaSources.add(source);
                    }
                }
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
    
    /**
     * Get a single schema combining all WSDL schemas and import schemas
     * @return the WSDL schema
     */
    public javax.xml.validation.Schema getCombinedSchema()
    {
        if (combinedSchema == null) {
            Source[] sources = schemaSources.toArray(new Source[schemaSources.size()]);
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                combinedSchema = sf.newSchema(sources);
            }
            catch(SAXException e) {
                throw new CitrusRuntimeException("Failed to combine schema", e);
            }
        }
        return combinedSchema;
    }
}
