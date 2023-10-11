/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.xml.schema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.xml.sax.SAXException;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class AbstractSchemaCollection extends SimpleXsdSchema implements InitializingPhase {

    /** List of schema resources */
    protected List<Resource> schemaResources = new ArrayList<>();

    /** Imported schemas */
    protected List<String> importedSchemas = new ArrayList<>();

    /** Official xmlns namespace */
    public static final String WWW_W3_ORG_2000_XMLNS = "http://www.w3.org/2000/xmlns/";
    public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

    @Override
    public XmlValidator createValidator() {
        try {
            return XmlValidatorFactory.createValidator(schemaResources
                    .stream()
                    .map(AbstractSchemaCollection::toSpringResource)
                    .toList()
                    .toArray(new org.springframework.core.io.Resource[]{}), W3C_XML_SCHEMA_NS_URI);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create validator from multi resource schema files", e);
        }
    }

    public static org.springframework.core.io.Resource toSpringResource(Resource resource) {
        if (resource instanceof Resources.ClasspathResource) {
            return new ClassPathResource(resource.getLocation());
        } else if (resource instanceof Resources.FileSystemResource) {
            return new FileSystemResource(resource.getLocation());
        }

        return new ByteArrayResource(FileUtils.copyToByteArray(resource));
    }

    /**
     * Recursively add all imported schemas as schema resource.
     * This is necessary when schema import are located in jar files. If they are not added immediately the reference to them is lost.
     *
     * @param schema
     */
    protected void addImportedSchemas(Schema schema) throws WSDLException, IOException, TransformerException, TransformerFactoryConfigurationError {
        for (Object imports : schema.getImports().values()) {
            for (SchemaImport schemaImport : (Vector<SchemaImport>)imports) {
                // Prevent duplicate imports
                if (!importedSchemas.contains(schemaImport.getNamespaceURI())) {
                    importedSchemas.add(schemaImport.getNamespaceURI());
                    Schema referencedSchema = schemaImport.getReferencedSchema();

                    if (referencedSchema != null) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Source source = new DOMSource(referencedSchema.getElement());
                        Result result = new StreamResult(bos);

                        TransformerFactory.newInstance().newTransformer().transform(source, result);
                        Resource schemaResource = Resources.create(bos.toByteArray());

                        addImportedSchemas(referencedSchema);
                        schemaResources.add(schemaResource);
                    }
                }
            }
        }
    }

    /**
     * Recursively add all included schemas as schema resource.
     */
    protected void addIncludedSchemas(Schema schema) throws WSDLException, IOException, TransformerException, TransformerFactoryConfigurationError {
        List<SchemaReference> includes = schema.getIncludes();
        for (SchemaReference schemaReference : includes) {
            String schemaLocation;
            URI locationURI = URI.create(schemaReference.getSchemaLocationURI());
            if (locationURI.isAbsolute()) {
                schemaLocation = schemaReference.getSchemaLocationURI();
            } else {
                schemaLocation = schema.getDocumentBaseURI().substring(0, schema.getDocumentBaseURI().lastIndexOf('/') + 1) + schemaReference.getSchemaLocationURI();
            }

            schemaResources.add(Resources.create(schemaLocation));
        }
    }

    @Override
    public void initialize() {
        Resource targetXsd = loadSchemaResources();
        if (targetXsd == null) {
            throw new CitrusRuntimeException("Failed to find target schema xsd file resource");
        }

        if (schemaResources.isEmpty()) {
            throw new CitrusRuntimeException("At least one schema xsd file resource is required");
        }

        setXsd(new ByteArrayResource(FileUtils.copyToByteArray(targetXsd)));

        try {
            super.afterPropertiesSet();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new CitrusRuntimeException("Failed to initialize schema collection", e);
        }
    }

    /**
     * Loads all schema resource files from schema locations.
     */
    protected abstract Resource loadSchemaResources();

    /**
     * Gets the schema resources.
     * @return
     */
    public List<Resource> getSchemaResources() {
        return schemaResources;
    }
}
