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

package org.citrusframework.xml.schema;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.schema.locator.JarWSDLLocator;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

/**
 * Wrapper implementation takes care of nested WSDL schema types. Exposes those WSDL schema types as
 * xsd schema instances for schema repository. WSDL may contain several schema types which get
 * exposed under a single target namespace (defined on WSDL level).
 *
 * @author Christoph Deppisch
 * @since 1.3
 */
public class WsdlXsdSchema extends AbstractSchemaCollection {

    /** WSDL file resource */
    private Resource wsdl;

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(WsdlXsdSchema.class);

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
    public Resource loadSchemaResources() {
        Assert.notNull(wsdl, "wsdl file resource is required");
        Assert.isTrue(wsdl.exists(), "wsdl file resource '" + wsdl + " does not exist");

        try {
            return loadSchemas(getWsdlDefinition(wsdl));
        } catch (Exception e) {
            throw new BeanCreationException("Failed to load schema types from WSDL file", e);
        }
    }

    /**
     * Loads nested schema type definitions from wsdl.
     * @throws IOException
     * @throws WSDLException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     */
    private Resource loadSchemas(Definition definition) throws WSDLException, IOException, TransformerException, TransformerFactoryConfigurationError {
        Types types = definition.getTypes();
        Resource targetXsd = null;
        Resource firstSchemaInWSDL = null;

        if (types != null) {
            List<?> schemaTypes = types.getExtensibilityElements();
            for (Object schemaObject : schemaTypes) {
                if (schemaObject instanceof SchemaImpl) {
                    SchemaImpl schema = (SchemaImpl) schemaObject;
                    inheritNamespaces(schema, definition);

                    addImportedSchemas(schema);
                    addIncludedSchemas(schema);

                    if (!importedSchemas.contains(getTargetNamespace(schema))) {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Source source = new DOMSource(schema.getElement());
                        Result result = new StreamResult(bos);

                        TransformerFactory.newInstance().newTransformer().transform(source, result);
                        Resource schemaResource = new ByteArrayResource(bos.toByteArray());

                        importedSchemas.add(getTargetNamespace(schema));
                        schemaResources.add(schemaResource);

                        if (definition.getTargetNamespace().equals(getTargetNamespace(schema)) && targetXsd == null) {
                            targetXsd = schemaResource;
                        } else if (targetXsd == null && firstSchemaInWSDL == null) {
                            firstSchemaInWSDL = schemaResource;
                        }
                    }
                } else {
                    LOG.warn("Found unsupported schema type implementation " + schemaObject.getClass());
                }
            }
        }

        for (Object imports : definition.getImports().values()) {
            for (Import wsdlImport : (Vector<Import>)imports) {
                String schemaLocation;
                URI locationURI = URI.create(wsdlImport.getLocationURI());
                if (locationURI.isAbsolute()) {
                    schemaLocation = wsdlImport.getLocationURI();
                } else {
                    schemaLocation = definition.getDocumentBaseURI().substring(0, definition.getDocumentBaseURI().lastIndexOf('/') + 1) + wsdlImport.getLocationURI();
                }

                if (schemaLocation.startsWith("jar:")) {
                    loadSchemas(getWsdlDefinition(new UrlResource(schemaLocation)));
                } else {
                    loadSchemas(getWsdlDefinition(new FileSystemResource(schemaLocation)));
                }
            }
        }

        if (targetXsd == null) {
            // Obviously no schema resource in WSDL did match the targetNamespace, just use the first schema resource found as main schema
            if (firstSchemaInWSDL != null) {
                targetXsd = firstSchemaInWSDL;
            } else if (!CollectionUtils.isEmpty(schemaResources)) {
                targetXsd = schemaResources.get(0);
            }
        }

        return targetXsd;
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
                if (!schema.getElement().hasAttributeNS(AbstractSchemaCollection.WWW_W3_ORG_2000_XMLNS, nsEntry.getKey())) {
                    schema.getElement().setAttributeNS(AbstractSchemaCollection.WWW_W3_ORG_2000_XMLNS, "xmlns:" + nsEntry.getKey(), nsEntry.getValue());
                }
            } else { // handle default namespace
                if (!schema.getElement().hasAttribute("xmlns")) {
                    schema.getElement().setAttributeNS(AbstractSchemaCollection.WWW_W3_ORG_2000_XMLNS, "xmlns" + nsEntry.getKey(), nsEntry.getValue());
                }
            }
        }
    }

    /**
     * Reads WSDL definition from resource.
     * @param wsdl
     * @return
     * @throws IOException
     * @throws WSDLException
     */
    private Definition getWsdlDefinition(Resource wsdl) {
        try {
            Definition definition;
            if (wsdl.getURI().toString().startsWith("jar:")) {
                // Locate WSDL imports in Jar files
                definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(new JarWSDLLocator(wsdl));
            } else {
                definition = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdl.getURI().getPath(), new InputSource(wsdl.getInputStream()));
            }

            return definition;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read wsdl file resource", e);
        } catch (WSDLException e) {
            throw new CitrusRuntimeException("Failed to wsdl schema instance", e);
        }
    }

    /**
     * Reads target namespace from schema definition element.
     * @param schema
     * @return
     */
    private String getTargetNamespace(Schema schema) {
        return schema.getElement().getAttribute("targetNamespace");
    }

    /**
     * Sets the wsdl.
     * @param wsdl the wsdl to set
     */
    public void setWsdl(Resource wsdl) {
        this.wsdl = wsdl;
    }

}
