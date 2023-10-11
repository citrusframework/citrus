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

import java.net.URI;
import javax.wsdl.xml.WSDLLocator;

import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Locates WSDL import sources in Jar files
 */
public class JarWSDLLocator implements WSDLLocator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JarWSDLLocator.class);

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
        URI importURI = URI.create(importLocation);
        if (importURI.isAbsolute()) {
            resolvedImportLocation = importLocation;
        } else {
            resolvedImportLocation = parentLocation.substring(0, parentLocation.lastIndexOf('/') + 1) + importLocation;
        }

        importResource = Resources.create(resolvedImportLocation);
        return new InputSource(importResource.getInputStream());
    }

    @Override
    public String getBaseURI() {
        return wsdl.getURI().toString();
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
