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

import static org.easymock.EasyMock.*;

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class RootQNameSchemaMappingStrategyTest {
    
    private XsdSchema schemaMock = EasyMock.createMock(XsdSchema.class);
    
    @Test
    public void testPositiveMapping() {
        RootQNameSchemaMappingStrategy strategy = new RootQNameSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("foo", schemaMock);
        mappings.put("bar", EasyMock.createMock(XsdSchema.class));
        
        strategy.setMappings(mappings);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(schemaMock);
        
        Assert.assertEquals(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"), schemaMock);
        
        verify(schemaMock);
    }
    
    @Test
    public void testPositiveMappingWithNamespaces() {
        RootQNameSchemaMappingStrategy strategy = new RootQNameSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("{http://citrusframework.org/schema/foo}foo", EasyMock.createMock(XsdSchema.class));
        mappings.put("{http://citrusframework.org/schema}foo", schemaMock);
        mappings.put("bar", EasyMock.createMock(XsdSchema.class));
        
        strategy.setMappings(mappings);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(schemaMock);
        
        Assert.assertEquals(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"), schemaMock);
        
        verify(schemaMock);
    }
    
    @Test
    public void testNoMappingFound() {
        RootQNameSchemaMappingStrategy strategy = new RootQNameSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("{http://citrusframework.org/schema/foos}foos", EasyMock.createMock(XsdSchema.class));
        mappings.put("{http://citrusframework.org/schema}foos", schemaMock);
        
        strategy.setMappings(mappings);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(schemaMock);
        
        Assert.assertNull(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"));
        
        verify(schemaMock);
    }
    
    @Test
    public void testMappingErrorWithNamespaceInconstistency() {
        RootQNameSchemaMappingStrategy strategy = new RootQNameSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("{http://citrusframework.org/schema/foo}foo", EasyMock.createMock(XsdSchema.class));
        mappings.put("{http://citrusframework.org/schema}foo", schemaMock);
        mappings.put("bar", EasyMock.createMock(XsdSchema.class));
        
        strategy.setMappings(mappings);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema/unknown").anyTimes();
        
        replay(schemaMock);
        
        try {
            strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo");
            Assert.fail("Missing exception due to schema target namespace inconsistency");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().startsWith("Schema target namespace inconsitency"));
        }
        
        verify(schemaMock);
    }
}
