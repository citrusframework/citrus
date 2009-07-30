package com.consol.citrus.xml;

import java.util.List;

import org.springframework.xml.xsd.XsdSchema;

public interface XsdSchemaMappingStrategy {
    public XsdSchema getSchema(List<XsdSchema> schemas, String namespace);
}
