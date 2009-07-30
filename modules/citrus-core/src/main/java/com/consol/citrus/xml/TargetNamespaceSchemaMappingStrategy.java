package com.consol.citrus.xml;

import java.util.List;

import org.springframework.xml.xsd.XsdSchema;

public class TargetNamespaceSchemaMappingStrategy implements XsdSchemaMappingStrategy {

    public XsdSchema getSchema(List<XsdSchema> schemas, String namespace) {
        for (XsdSchema schema : schemas) {
            if(schema.getTargetNamespace().equals(namespace)) {
                return schema;
            }
        }
        
        return null;
    }
    
}
