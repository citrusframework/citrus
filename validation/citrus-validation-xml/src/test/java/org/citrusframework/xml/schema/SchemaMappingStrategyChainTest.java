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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class SchemaMappingStrategyChainTest {
    
    private XsdSchema schemaMock = Mockito.mock(XsdSchema.class);
    
    @Test
    public void testStrategyChain() {
        Document doc = Mockito.mock(Document.class);
        Node rootNode = Mockito.mock(Node.class);
        
        SchemaMappingStrategyChain strategy = new SchemaMappingStrategyChain();
        RootQNameSchemaMappingStrategy qNameStrategy = new RootQNameSchemaMappingStrategy();
        TargetNamespaceSchemaMappingStrategy namespaceStrategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("{http://citrusframework.org/schema}foo", schemaMock);
        qNameStrategy.setMappings(mappings);
        
        List<XsdSchemaMappingStrategy> strategies = new ArrayList<XsdSchemaMappingStrategy>();
        strategies.add(qNameStrategy);
        strategies.add(namespaceStrategy);
        
        strategy.setStrategies(strategies);

        reset(doc, rootNode, schemaMock);
        
        when(doc.getFirstChild()).thenReturn(rootNode);
        when(rootNode.getNamespaceURI()).thenReturn("http://citrusframework.org/schema");
        when(rootNode.getLocalName()).thenReturn("foo");
        
        when(schemaMock.getTargetNamespace()).thenReturn("http://citrusframework.org/schema");

        
        Assert.assertEquals(strategy.getSchema(schemas, doc), schemaMock);

    }
    
    @Test
    public void testStrategyChainFallback() {
        Document doc = Mockito.mock(Document.class);
        Node rootNode = Mockito.mock(Node.class);
        
        SchemaMappingStrategyChain strategy = new SchemaMappingStrategyChain();
        RootQNameSchemaMappingStrategy qNameStrategy = new RootQNameSchemaMappingStrategy();
        TargetNamespaceSchemaMappingStrategy namespaceStrategy = new TargetNamespaceSchemaMappingStrategy();
        
        List<XsdSchema> schemas = new ArrayList<XsdSchema>();
        schemas.add(schemaMock);

        Map<String, XsdSchema> mappings = new HashMap<String, XsdSchema>();
        mappings.put("{http://citrusframework.org/schema}foo", schemaMock);
        qNameStrategy.setMappings(mappings);
        
        List<XsdSchemaMappingStrategy> strategies = new ArrayList<XsdSchemaMappingStrategy>();
        strategies.add(qNameStrategy);
        strategies.add(namespaceStrategy);
        
        strategy.setStrategies(strategies);

        reset(doc, rootNode, schemaMock);
        
        when(doc.getFirstChild()).thenReturn(rootNode);
        when(rootNode.getNamespaceURI()).thenReturn("http://citrusframework.org/schema");
        when(rootNode.getLocalName()).thenReturn("bar");
        
        when(schemaMock.getTargetNamespace()).thenReturn("http://citrusframework.org/schema");

        
        Assert.assertEquals(strategy.getSchema(schemas, doc), schemaMock);

    }
    
}
