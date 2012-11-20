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

package com.consol.citrus.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.schema.*;

/**
 * Schema repository holding a set of XML schema resources known in the test scope.
 * 
 * @author Christoph Deppisch
 */
public class XsdSchemaRepository implements BeanNameAware, InitializingBean {
    /** The default repository name */
    public static final String DEFAULT_REPOSITORY_NAME = "schemaRepository";
    
    /** This repositories name in the Spring application context */
    private String name = DEFAULT_REPOSITORY_NAME;
    
    /** List of schema resources */
    private List<XsdSchema> schemas = new ArrayList<XsdSchema>();
    
    /** List of location patterns that will be translated to schema resources */
    private List<String> locations = new ArrayList<String>();
    
    /** Mapping strategy */
    private XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XsdSchemaRepository.class);
    
    /**
     * Find the matching schema for a given message namespace or root element
     * name.
     * @param doc the document instance to validate.
     * @return the matching schema instance
     * @throws IOException
     * @throws SAXException
     */
    public XsdSchema findSchema(Document doc) throws IOException, SAXException {
        XsdSchema schema = schemaMappingStrategy.getSchema(schemas, doc);
        
        if (schema == null) {
            throw new CitrusRuntimeException("Unable to find proper XML schema definition for element " + 
                        doc.getFirstChild().getLocalName() + "(" + doc.getFirstChild().getNamespaceURI() + ") " +
                        "add schema to schema repository or disable schema validation for this message");
        }
        
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        
        for (String location : locations) {
            Resource[] findings = resourcePatternResolver.getResources(location);
            
            for (Resource resource : findings) {
                if (resource.getFilename().endsWith(".xsd")) {
                    schemas.add(new SimpleXsdSchema(resource));
                } else if (resource.getFilename().endsWith(".wsdl")) {
                    schemas.add(new WsdlXsdSchema(resource));
                } else {
                    log.warn("Skipped resource other than XSD schema for repository (" + resource.getFilename() + ")");
                }
            }
        }
    }

    /**
     * Get the list of known schemas.
     * @return the schemaSources
     */
    public List<XsdSchema> getSchemas() {
        return schemas;
    }

    /**
     * Set the list of known schemas.
     * @param schemas the schemas to set
     */
    public void setSchemas(List<XsdSchema> schemas) {
        this.schemas = schemas;
    }

    /**
     * Set the schema mapping strategy.
     * @param schemaMappingStrategy the schemaMappingStrategy to set
     */
    public void setSchemaMappingStrategy(XsdSchemaMappingStrategy schemaMappingStrategy) {
        this.schemaMappingStrategy = schemaMappingStrategy;
    }

    /**
     * {@inheritDoc}
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     * @return the name the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the locations.
     * @return the locations the locations to get.
     */
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Sets the locations.
     * @param locations the locations to set
     */
    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
    
}
