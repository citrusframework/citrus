/*
 * Copyright the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JarWSDLLocatorTest {

    private final Resource wsdl = Resources.fromClasspath("org/citrusframework/validation/SampleService.wsdl");

    @Test
    public void testGetImportInputSource() {
        JarWSDLLocator locator = new JarWSDLLocator(wsdl);
        Assert.assertNotNull(locator.getBaseInputSource());
        Assert.assertNotNull(locator.getBaseURI());
        Assert.assertTrue(locator.getBaseURI().endsWith("org/citrusframework/validation/SampleService.wsdl"));
        Assert.assertNull(locator.getLatestImportURI());
        Assert.assertNotNull(locator.getImportInputSource(locator.getBaseURI(), "types.xsd"));
        Assert.assertTrue(locator.getLatestImportURI().endsWith("org/citrusframework/validation/types.xsd"));
    }

    @Test
    public void testGetImportInputSourceFromSpecialCharDir() {
        // Running from maven, rather than from IDE, URLs might get decoded, in case they contain special chars like @.
        Resource wsdlFromSpecialCharDir = Resources.fromClasspath(
            "org/citrusframework/validation/dirWithSpecialChar@/SampleService2.wsdl");
        JarWSDLLocator locator = new JarWSDLLocator(wsdlFromSpecialCharDir);
        Assert.assertNotNull(locator.getBaseInputSource());
        Assert.assertNotNull(locator.getBaseURI());
        Assert.assertTrue(locator.getBaseURI().endsWith(
            "org/citrusframework/validation/dirWithSpecialChar@/SampleService2.wsdl"));
        Assert.assertNull(locator.getLatestImportURI());
        Assert.assertNotNull(locator.getImportInputSource(locator.getBaseURI(), "types2.xsd"));
        Assert.assertTrue(locator.getLatestImportURI().endsWith(
            "org/citrusframework/validation/dirWithSpecialChar@/types2.xsd"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".* does not exists")
    public void testGetInvalidImportInputSource() {
        JarWSDLLocator locator = new JarWSDLLocator(wsdl);
        locator.getImportInputSource(locator.getBaseURI(), "invalid.xsd");
    }
}
