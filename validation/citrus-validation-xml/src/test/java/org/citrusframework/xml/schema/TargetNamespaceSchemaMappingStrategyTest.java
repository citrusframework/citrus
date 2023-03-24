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

import org.mockito.Mockito;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class TargetNamespaceSchemaMappingStrategyTest {
    
    private XsdSchema schemaMock = Mockito.mock(XsdSchema.class);
    
    @Test
    public void testPositiveMappingWithNamespaces() {
        TargetNamespaceSchemaMappingStrategy strategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        reset(schemaMock);
        
        when(schemaMock.getTargetNamespace()).thenReturn("http://citrusframework.org/schema");

        
        Assert.assertEquals(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"), schemaMock);

    }
    
    @Test
    public void testNoMappingFound() {
        TargetNamespaceSchemaMappingStrategy strategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        reset(schemaMock);
        
        when(schemaMock.getTargetNamespace()).thenReturn("http://citrusframework.org/schema/foos");

        
        Assert.assertNull(strategy.getSchema(schemas, "http://citrusframework.org/schema", "foo"));

    }
    
}
