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

import java.net.URI;
import javax.wsdl.xml.WSDLLocator;

import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.xml.sax.InputSource;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Locates WSDL import sources in Jar files
 */
public class JarWSDLLocator implements WSDLLocator {

    private final Resource wsdl;
    private Resource importResource = null;

    public JarWSDLLocator(Resource wsdl) {
        this.wsdl = wsdl;
    }

    @Override
    public InputSource getBaseInputSource() {
        return new InputSource(wsdl.getInputStream());
    }

    @Override
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        String resolvedImportLocation;
        String decodedImportLocation = decode(importLocation, UTF_8);
        URI importURI = URI.create(decodedImportLocation);
        if (importURI.isAbsolute()) {
            resolvedImportLocation = decodedImportLocation;
        } else {
            String decodedParentLocation = decode(parentLocation, UTF_8);
            resolvedImportLocation = decodedParentLocation.substring(0, decodedParentLocation.lastIndexOf('/') + 1) + decodedImportLocation;
        }

        importResource = Resources.create(resolvedImportLocation);
        return new InputSource(importResource.getInputStream());
    }

    @Override
    public String getBaseURI() {
        return decode(wsdl.getURI().toString(), UTF_8);
    }

    @Override
    public String getLatestImportURI() {
        if (importResource == null) {
            return null;
        }

        return importResource.getURI().toString();
    }

    @Override
    public void close() {
    }

}
