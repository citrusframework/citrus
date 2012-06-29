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

package com.consol.citrus.xml.xpath;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class XPathUtilsTest {

    @Test
    public void testDynamicNamespaceCreation() {
        Map<String, String> namespaces;
        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/foo}bar");

        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get("dns1"), "http://citrusframework.org/foo");
        
        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar/{http://citrusframework.org/foo}value");
        
        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("dns1"), "http://citrusframework.org/foo");
        Assert.assertEquals(namespaces.get("dns2"), "http://citrusframework.org/bar");
        
        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar/{urn:citrus}value");
        
        Assert.assertEquals(namespaces.size(), 3);
        Assert.assertEquals(namespaces.get("dns1"), "http://citrusframework.org/foo");
        Assert.assertEquals(namespaces.get("dns2"), "http://citrusframework.org/bar");
        Assert.assertEquals(namespaces.get("dns3"), "urn:citrus");
        
    }
    
    @Test
    public void testDynamicNamespaceReplacement() {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://citrusframework.org/foo");
        namespaces.put("ns2", "http://citrusframework.org/bar");
        namespaces.put("ns3", "http://citrusframework.org/foo-bar");
        
        Assert.assertEquals(XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/foo}bar", namespaces),
                "//ns1:Foo/ns1:bar");
        
        Assert.assertEquals(XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar", namespaces),
                "//ns1:Foo/ns2:bar");
        
        Assert.assertEquals(XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo-bar}Foo/bar", namespaces),
                "//ns3:Foo/bar");
        
        Assert.assertEquals(XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/unkown}Foo/{http://citrusframework.org/unknown}bar", namespaces),
                "//{http://citrusframework.org/unkown}Foo/{http://citrusframework.org/unknown}bar");
    }
}
