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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JarWSDLLocatorTest {

    private final Resource wsdl = Resources.fromClasspath("org/citrusframework/validation/SampleService.wsdl");

    @Test
    public void testGetImportInputSource() {
        JarWSDLLocator locator = new JarWSDLLocator(wsdl);
        Assertions.assertNotNull(locator.getBaseInputSource());
        Assertions.assertNotNull(locator.getBaseURI());
        Assertions.assertTrue(locator.getBaseURI().endsWith("org/citrusframework/validation/SampleService.wsdl"));
        Assertions.assertNull(locator.getLatestImportURI());
        Assertions.assertNotNull(locator.getImportInputSource(locator.getBaseURI(), "types.xsd"));
        Assertions.assertTrue(locator.getLatestImportURI().endsWith("org/citrusframework/validation/types.xsd"));
    }

    @Test
    public void testGetImportInputSourceFromSpecialCharDir() {
        // Running from maven, rather than from IDE, URLs might get decoded, in case they contain special chars like @.
        Resource wsdlFromSpecialCharDir = Resources.fromClasspath(
            "org/citrusframework/validation/dirWithSpecialChar@/SampleService2.wsdl");
        JarWSDLLocator locator = new JarWSDLLocator(wsdlFromSpecialCharDir);
        Assertions.assertNotNull(locator.getBaseInputSource());
        Assertions.assertNotNull(locator.getBaseURI());
        Assertions.assertTrue(locator.getBaseURI().endsWith(
            "org/citrusframework/validation/dirWithSpecialChar@/SampleService2.wsdl"));
        Assertions.assertNull(locator.getLatestImportURI());
        Assertions.assertNotNull(locator.getImportInputSource(locator.getBaseURI(), "types2.xsd"));
        Assertions.assertTrue(locator.getLatestImportURI().endsWith(
            "org/citrusframework/validation/dirWithSpecialChar@/types2.xsd"));
    }

    @Test
    public void testGetInvalidImportInputSource() {
        JarWSDLLocator locator = new JarWSDLLocator(wsdl);
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> locator.getImportInputSource(locator.getBaseURI(), "invalid.xsd"));
        assertThat(thrown).hasMessageMatching(".* does not exists");
    }
}
