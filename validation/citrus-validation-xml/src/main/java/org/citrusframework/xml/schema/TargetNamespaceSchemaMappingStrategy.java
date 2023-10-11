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

import org.citrusframework.util.StringUtils;
import org.springframework.xml.xsd.XsdSchema;


/**
 * Mapping strategy checks on target namespaces in schemas to find matching schema
 * instance.
 *
 * @author Christoph Deppisch
 */
public class TargetNamespaceSchemaMappingStrategy extends AbstractSchemaMappingStrategy {

    @Override
    public XsdSchema getSchema(List<XsdSchema> schemas, String namespace, String elementName) {
        for (XsdSchema schema : schemas) {
            if (StringUtils.hasText(schema.getTargetNamespace()) &&
                    schema.getTargetNamespace().equals(namespace)) {
                return schema;
            }
        }

        return null;
    }
}
