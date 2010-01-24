/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.xml.xsd.XsdSchema;
import org.xml.sax.SAXException;

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
     * Retrieve the schema for a given namespace.
     * 
     * @param namespace
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public XsdSchema getSchemaByNamespace(String namespace) throws IOException, SAXException {
        return schemaMappingStrategy.getSchema(schemas, namespace);
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
