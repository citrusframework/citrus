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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TargetNamespaceSchemaMappingStrategyTest {
    
    private XsdSchema schemaMock = EasyMock.createMock(XsdSchema.class);
    
    @Test
    public void testPositiveMappingWithNamespaces() {
        TargetNamespaceSchemaMappingStrategy strategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(schemaMock);
        
        Assert.assertEquals(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"), schemaMock);
        
        verify(schemaMock);
    }
    
    @Test
    public void testNoMappingFound() {
        TargetNamespaceSchemaMappingStrategy strategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        reset(schemaMock);
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema/foos").anyTimes();
        
        replay(schemaMock);
        
        Assert.assertNull(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"));
        
        verify(schemaMock);
    }
    
}
