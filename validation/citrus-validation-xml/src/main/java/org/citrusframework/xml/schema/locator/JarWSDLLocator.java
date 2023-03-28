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

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xml.sax.InputSource;

/**
 * Locates WSDL import sources in Jar files
 */
public class JarWSDLLocator implements WSDLLocator {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JarWSDLLocator.class);

    private Resource wsdl;
    private Resource importResource = null;

    public JarWSDLLocator(Resource wsdl) {
        this.wsdl = wsdl;
    }

    @Override
    public InputSource getBaseInputSource() {
        try {
            return new InputSource(wsdl.getInputStream());
        } catch (IOException e) {
            return null;
        }
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

        try {
            importResource = new PathMatchingResourcePatternResolver().getResource(resolvedImportLocation);
            return new InputSource(importResource.getInputStream());
        } catch (IOException e) {
            log.warn(String.format("Failed to resolve imported WSDL schema path location '%s'", importLocation), e);
            return null;
        }
    }

    @Override
    public String getBaseURI() {
        try {
            return wsdl.getURI().toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getLatestImportURI() {
        if (importResource == null) {
            return null;
        }

        try {
            return importResource.getURI().toString();
        } catch (IOException e) {
            log.warn("Failed to resolve last imported WSDL schema resource", e);
            return null;
        }
    }

    @Override
    public void close() {
    }
}
