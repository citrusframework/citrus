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

package org.citrusframework.xml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.Named;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.schema.TargetNamespaceSchemaMappingStrategy;
import org.citrusframework.xml.schema.WsdlXsdSchema;
import org.citrusframework.xml.schema.XsdSchemaMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Schema repository holding a set of XML schema resources known in the test scope.
 *
 * @author Christoph Deppisch
 */
@SuppressWarnings("unused")
public class XsdSchemaRepository implements Named, InitializingPhase {
    /** The name of the repository */
    private String name = "schemaRepository";

    /** List of schema resources */
    private List<XsdSchema> schemas = new ArrayList<>();

    /** List of location patterns that will be translated to schema resources */
    private List<String> locations = new ArrayList<>();

    /** Mapping strategy */
    private XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XsdSchemaRepository.class);

    /**
     * Find the matching schema for document using given schema mapping strategy.
     * @param doc the document instance to validate.
     * @return boolean flag marking matching schema instance found
     */
    public boolean canValidate(Document doc) {
        XsdSchema schema = schemaMappingStrategy.getSchema(schemas, doc);
        return schema != null;
    }

    @Override
    public void initialize() {
        try {
            ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
            for (String location : locations) {
                Resource found = Resources.create(location);
                if (found.exists()) {
                    addSchemas(found);
                } else {
                    Set<Path> findings;
                    if (StringUtils.hasText(FileUtils.getFileExtension(location))) {
                        String fileNamePattern = FileUtils.getFileName(location).replace(".", "\\.").replace("*", ".*");
                        String basePath = FileUtils.getBasePath(location);
                        findings = resourceResolver.getResources(basePath, fileNamePattern);
                    } else {
                        findings = resourceResolver.getResources(location);
                    }

                    for (Path resource : findings) {
                        addSchemas(Resources.fromClasspath(resource.toString()));
                    }
                }
            }

            // Add default Citrus message schemas if available on classpath
            addCitrusSchema("citrus-http-message");
            addCitrusSchema("citrus-mail-message");
            addCitrusSchema("citrus-ftp-message");
            addCitrusSchema("citrus-ssh-message");
            addCitrusSchema("citrus-rmi-message");
            addCitrusSchema("citrus-jmx-message");
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new CitrusRuntimeException("Failed to initialize Xsd schema repository", e);
        }
    }

    /**
     * Adds Citrus message schema to repository if available on classpath.
     * @param schemaName The name of the schema within the citrus schema package
     */
    protected void addCitrusSchema(String schemaName) throws IOException, SAXException, ParserConfigurationException {
        Resource resource = Resources.fromClasspath("classpath:org/citrusframework/schema/" + schemaName + ".xsd");
        if (resource.exists()) {
            addXsdSchema(resource);
        }
    }

    private void addSchemas(Resource resource) {
        if (resource.getLocation().endsWith(".xsd")) {
            addXsdSchema(resource);
        } else if (resource.getLocation().endsWith(".wsdl")) {
            addWsdlSchema(resource);
        } else {
            logger.warn("Skipped resource other than XSD schema for repository (" + resource.getLocation() + ")");
        }
    }

    private void addWsdlSchema(Resource resource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading WSDL schema resource " + resource.getLocation());
        }

        WsdlXsdSchema wsdl = new WsdlXsdSchema(resource);
        wsdl.initialize();
        schemas.add(wsdl);
    }

    private void addXsdSchema(Resource resource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Loading XSD schema resource " + resource.getLocation());
        }

        SimpleXsdSchema schema = new SimpleXsdSchema(new ByteArrayResource(FileUtils.copyToByteArray(resource)));
        try {
            schema.afterPropertiesSet();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new CitrusRuntimeException("Failed to initialize xsd schema", e);
        }
        schemas.add(schema);
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
     * @return The current XsdSchemaMappingStrategy
     */
    public XsdSchemaMappingStrategy getSchemaMappingStrategy() {
        return schemaMappingStrategy;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     * @return the name to get.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the locations.
     * @return the locations to get.
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
