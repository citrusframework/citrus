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
package com.consol.citrus.xml.schema;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.validation.XmlValidatorFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.xml.sax.SAXException;

/**
 * Schema combines multiple file resources usually with exactly the same target namespace to
 * one single schema instance.
 * 
 * @author Christoph Deppisch
 */
public class MultiResourceXsdSchema extends SimpleXsdSchema implements InitializingBean {

    /** List of schemas that are loaded as single schema instance */
    private Resource[] schemas = new Resource[] {};
    
    public static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    
    @Override
    public XmlValidator createValidator() throws IOException {
        return XmlValidatorFactory.createValidator(schemas, W3C_XML_SCHEMA_NS_URI);
    }
    
    @Override
    public void afterPropertiesSet() throws ParserConfigurationException, IOException, SAXException {
        Assert.isTrue(schemas.length > 0, "At least one schema file resource is required");
        
        setXsd(schemas[0]);
        
        super.afterPropertiesSet();
    }
    
    /**
     * @param schemas the schema resources to set
     */
    public void setSchemas(Resource[] schemas) {
        this.schemas = schemas.clone();
    }
}
