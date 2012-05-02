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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Christoph Deppisch
 */
public class SchemaMappingStrategyChainTest {
    
    private XsdSchema schemaMock = EasyMock.createMock(XsdSchema.class);
    
    @Test
    public void testStrategyChain() {
        Document doc = EasyMock.createMock(Document.class);
        Node rootNode = EasyMock.createMock(Node.class);
        
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
        
        expect(doc.getFirstChild()).andReturn(rootNode).anyTimes();
        expect(rootNode.getNamespaceURI()).andReturn("http://citrusframework.org/schema").anyTimes();
        expect(rootNode.getLocalName()).andReturn("foo").anyTimes();
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(doc, rootNode, schemaMock);
        
        Assert.assertEquals(strategy.getSchema(schemas, doc), schemaMock);
        
        verify(doc, rootNode, schemaMock);
    }
    
    @Test
    public void testStrategyChainFallback() {
        Document doc = EasyMock.createMock(Document.class);
        Node rootNode = EasyMock.createMock(Node.class);
        
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
        
        expect(doc.getFirstChild()).andReturn(rootNode).anyTimes();
        expect(rootNode.getNamespaceURI()).andReturn("http://citrusframework.org/schema").anyTimes();
        expect(rootNode.getLocalName()).andReturn("bar").anyTimes();
        
        expect(schemaMock.getTargetNamespace()).andReturn("http://citrusframework.org/schema").anyTimes();
        
        replay(doc, rootNode, schemaMock);
        
        Assert.assertEquals(strategy.getSchema(schemas, doc), schemaMock);
        
        verify(doc, rootNode, schemaMock);
    }
    
}
