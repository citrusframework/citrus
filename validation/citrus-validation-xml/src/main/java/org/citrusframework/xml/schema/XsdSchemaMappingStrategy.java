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

package org.citrusframework.xml.schema;

import java.util.List;

import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;

/**
 * Interface for schema mapping strategies used in schema repository.
 * 
 * @author Christoph Deppisch
 */
public interface XsdSchemaMappingStrategy {
    
    /**
     * Gets the schema for given namespace or root element name.
     * 
     * @param schemas list of available schemas.
     * @param doc document instance to validate.
     * @return
     */
    XsdSchema getSchema(List<XsdSchema> schemas, Document doc);
}
