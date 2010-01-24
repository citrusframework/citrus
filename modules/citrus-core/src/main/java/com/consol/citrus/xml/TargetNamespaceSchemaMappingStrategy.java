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

import java.util.List;

import org.springframework.xml.xsd.XsdSchema;

/**
 * Class defining how to map schemas to namespace values.
 * 
 * @author Christoph Deppisch
 */
public class TargetNamespaceSchemaMappingStrategy implements XsdSchemaMappingStrategy {

    /**
     * @see com.consol.citrus.xml.XsdSchemaMappingStrategy#getSchema(java.util.List, java.lang.String)
     */
    public XsdSchema getSchema(List<XsdSchema> schemas, String namespace) {
        for (XsdSchema schema : schemas) {
            if(schema.getTargetNamespace().equals(namespace)) {
                return schema;
            }
        }
        
        return null;
    }
    
}
