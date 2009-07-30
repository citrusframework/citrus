package com.consol.citrus.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.xml.xsd.XsdSchema;
import org.xml.sax.SAXException;

public class XsdSchemaRepository {
    List<XsdSchema> schemas = new ArrayList<XsdSchema>();
    
    XsdSchemaMappingStrategy schemaMappingStrategy = new TargetNamespaceSchemaMappingStrategy();
    
    public XsdSchema getSchemaByNamespace(String namespace) throws IOException, SAXException {
        return schemaMappingStrategy.getSchema(schemas, namespace);
    }

    /**
     * @return the schemaSources
     */
    public List<XsdSchema> getSchemas() {
        return schemas;
    }

    /**
     * @param schemas the schemas to set
     */
    public void setSchemas(List<XsdSchema> schemas) {
        this.schemas = schemas;
    }

    /**
     * @param schemaMappingStrategy the schemaMappingStrategy to set
     */
    public void setSchemaMappingStrategy(XsdSchemaMappingStrategy schemaMappingStrategy) {
        this.schemaMappingStrategy = schemaMappingStrategy;
    }
    
    
}
