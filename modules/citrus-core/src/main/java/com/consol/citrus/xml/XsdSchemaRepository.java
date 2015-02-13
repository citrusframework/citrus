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

import com.consol.citrus.xml.schema.*;
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

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema repository holding a set of XML schema resources known in the test scope.
 * 
 * @author Christoph Deppisch
 */
public class XsdSchemaRepository implements BeanNameAware, InitializingBean {
    /** This repositories name in the Spring application context */
    private String name = "schemaRepository";
    
    /** List of schema resources */
    private List<XsdSchema> schemas = new ArrayList<XsdSchema>();
    
    /** List of location patterns that will be translated to schema resources */
    private List<String> locations = new ArrayList<String>();

    /** Mapping strategy */
    private XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XsdSchemaRepository.class);
    
    /**
     * Find the matching schema for document using given schema mapping strategy.
     * @param doc the document instance to validate.
     * @return boolean flag marking matching schema instance found
     * @throws IOException
     * @throws SAXException
     */
    public boolean canValidate(Document doc) throws IOException, SAXException {
        XsdSchema schema = schemaMappingStrategy.getSchema(schemas, doc);
        return schema != null;
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
                    log.info("Loading XSD schema resource " + resource.getFilename());
                    SimpleXsdSchema schema = new SimpleXsdSchema(resource);
                    schema.afterPropertiesSet();
                    schemas.add(schema);
                } else if (resource.getFilename().endsWith(".wsdl")) {
                    log.info("Loading WSDL schema resource " + resource.getFilename());
                    WsdlXsdSchema wsdl = new WsdlXsdSchema(resource);
                    wsdl.afterPropertiesSet();
                    schemas.add(wsdl);
                } else {
                    log.warn("Skipped resource other than XSD schema for repository (" + resource.getFilename() + ")");
                }
            }
        }

        // Add default Citrus message schemas if available on classpath
        addCitrusSchema("citrus-mail-message");
        addCitrusSchema("citrus-ftp-message");
        addCitrusSchema("citrus-ssh-message");
    }

    /**
     * Adds Citrus message schema to repository if available on classpath.
     * @param schemaName
     */
    protected void addCitrusSchema(String schemaName) throws IOException, SAXException, ParserConfigurationException {
        Resource resource = new PathMatchingResourcePatternResolver().getResource("classpath:com/consol/citrus/schema/" + schemaName + ".xsd");
        if (resource.exists()) {
            log.info("Loading XSD schema resource " + resource.getFilename());
            SimpleXsdSchema schema = new SimpleXsdSchema(resource);
            schema.afterPropertiesSet();
            schemas.add(schema);
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
     * Gets the schema mapping strategy.
     * @return
     */
    public XsdSchemaMappingStrategy getSchemaMappingStrategy() {
        return schemaMappingStrategy;
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
