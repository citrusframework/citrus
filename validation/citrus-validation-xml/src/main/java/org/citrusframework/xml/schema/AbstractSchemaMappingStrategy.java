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

import java.util.List;

import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;

/**
 * Abstract schema mapping strategy extracts target namespace and root element name
 * for subclasses.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractSchemaMappingStrategy implements XsdSchemaMappingStrategy {

    /**
     * {@inheritDoc}
     */
    public XsdSchema getSchema(List<XsdSchema> schemas, Document doc) {
        return getSchema(schemas, doc.getFirstChild().getNamespaceURI(), 
                doc.getFirstChild().getLocalName());
    }
    
    /**
     * Subclasses must override this method in order to detect schema for
     * target namespace and/or root element name.
     * 
     * @param schemas
     * @param namespace
     * @param elementName
     * @return
     */
    public abstract XsdSchema getSchema(List<XsdSchema> schemas, String namespace, String elementName);
}
