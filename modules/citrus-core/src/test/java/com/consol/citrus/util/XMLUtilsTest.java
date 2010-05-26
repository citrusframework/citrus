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

package com.consol.citrus.util;

import java.util.Map;

import javax.xml.XMLConstants;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XMLUtilsTest {

    @Test
    public void testLookupNamespacesInXMLFragment() {
        Map<String, String> namespaces;
        
        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");
        
        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/xmlns/test\" xmlns:ns2=\"http://www.consol.de/xmlns/test2\"></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");
    }
    
    @Test
    public void testLookupNamespacesInXMLFragmentSingleQuotes() {
        Map<String, String> namespaces;
        
        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://www.consol.de/test' xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");
        
        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");
        
        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://www.consol.de/xmlns/test' xmlns:ns2='http://www.consol.de/xmlns/test2'></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");
    }
    
    @Test
    public void testLookupNamespacesInXMLFragmentWithAtributes() {
        Map<String, String> namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" id=\"123456789\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");
    }
    
    @Test
    public void testLookupNamespacesInXMLFragmentDefaultNamespaces() {
        Map<String, String> namespaces;
        
        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/test-default\"></testRequest>");
        
        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");
        
        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns='http://www.consol.de/test-default'></testRequest>");
        
        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");
        
        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/test-default\" xmlns:ns1=\"http://www.consol.de/test\"></testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        
        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/xmlns/test-default\" xmlns:ns1=\"http://www.consol.de/xmlns/test\"></testRequest>");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/xmlns/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
    }
    
    @Test
    public void testLookupNamespacesInXMLFragmentNoNamespacesFound() {
        Map<String, String> namespaces = XMLUtils.lookupNamespaces("<testRequest id=\"123456789\"></testRequest>");
        
        Assert.assertEquals(namespaces.size(), 0);
    }
}
