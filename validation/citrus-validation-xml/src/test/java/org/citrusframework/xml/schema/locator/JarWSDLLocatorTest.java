/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.xml.schema.locator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JarWSDLLocatorTest {

    private Resource wsdl = new ClassPathResource("org/citrusframework/validation/SampleService.wsdl");

    @Test
    public void testGetImportInputSource() throws Exception {
        JarWSDLLocator locator = new JarWSDLLocator(wsdl);
        Assert.assertNotNull(locator.getBaseInputSource());
        Assert.assertNotNull(locator.getBaseURI());
        Assert.assertTrue(locator.getBaseURI().endsWith("org/citrusframework/validation/SampleService.wsdl"));

        Assert.assertNull(locator.getLatestImportURI());
        Assert.assertNull(locator.getImportInputSource(locator.getBaseURI(), "invalid.xsd"));
        Assert.assertTrue(locator.getLatestImportURI().endsWith("org/citrusframework/validation/invalid.xsd"));
        Assert.assertNotNull(locator.getImportInputSource(locator.getBaseURI(), "types.xsd"));
        Assert.assertTrue(locator.getLatestImportURI().endsWith("org/citrusframework/validation/types.xsd"));
    }

}
