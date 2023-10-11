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
import java.util.Map;
import javax.xml.namespace.QName;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Mapping strategy uses the root element local name to find matching schema
 * instance.
 *
 * @author Christoph Deppisch
 */
public class RootQNameSchemaMappingStrategy extends AbstractSchemaMappingStrategy {

    /** Root element names mapping to schema instances */
    private Map<String, XsdSchema> mappings;

    @Override
    public XsdSchema getSchema(List<XsdSchema> schemas, String namespace, String elementName) {
        XsdSchema schema = null;
        QName rootQName = new QName(namespace, elementName, "");

        if (mappings.containsKey(rootQName.toString())) {
            schema = mappings.get(rootQName.toString());
        } else if (mappings.containsKey(elementName)) {
            schema = mappings.get(elementName);
        }

        if (schema!= null && !(StringUtils.hasText(schema.getTargetNamespace()) &&
                schema.getTargetNamespace().equals(namespace))) {
            throw new CitrusRuntimeException("Schema target namespace inconsitency " +
            		"for located XSD schema definition (" + schema.getTargetNamespace() + ")");
        }

        return schema;
    }

    /**
     * Sets the mappings.
     * @param mappings the mappings to set
     */
    public void setMappings(Map<String, XsdSchema> mappings) {
        this.mappings = mappings;
    }
}
