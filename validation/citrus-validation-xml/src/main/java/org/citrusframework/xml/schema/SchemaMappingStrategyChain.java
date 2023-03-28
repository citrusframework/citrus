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

import java.util.ArrayList;
import java.util.List;

import org.springframework.xml.xsd.XsdSchema;
import org.w3c.dom.Document;

/**
 * Special schema mapping strategy delegating to several other strategies in
 * a mapping chain. The first mapping strategy finding a proper schema wins.
 * 
 * @author Christoph Deppisch
 */
public class SchemaMappingStrategyChain implements XsdSchemaMappingStrategy {
    
    /** List of strategies to use in this chain */
    private List<XsdSchemaMappingStrategy> strategies = new ArrayList<XsdSchemaMappingStrategy>();

    /**
     * {@inheritDoc}
     */
    public XsdSchema getSchema(List<XsdSchema> schemas, Document doc) {
        XsdSchema schema = null;
        
        for (XsdSchemaMappingStrategy strategy : strategies) {
            schema = strategy.getSchema(schemas, doc);
            
            if (schema != null) {
                return schema;
            }
        }
        
        return schema;
    }

    /**
     * Sets the strategies.
     * @param strategies the strategies to set
     */
    public void setStrategies(List<XsdSchemaMappingStrategy> strategies) {
        this.strategies = strategies;
    }

}
