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

import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.schema.TargetNamespaceSchemaMappingStrategy;
import com.consol.citrus.xml.schema.XsdSchemaMappingStrategy;

/**
 * Schema repository holding a set of XML schema resources known in the test scope.
 * 
 * @author Christoph Deppisch
 */
public class XsdSchemaRepository {
    /** List of schema resources */
    private List<XsdSchema> schemas = new ArrayList<XsdSchema>();
    
    /** Mapping strategy */
    private XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();
    
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
}
