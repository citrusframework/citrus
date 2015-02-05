package com.consol.citrus.xml.schema;

import java.io.IOException;
import java.net.URI;
import javax.wsdl.xml.WSDLLocator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xml.sax.InputSource;

/**
 * Locates WSDL import sources in Jar files
 */
public class JarWSDLLocator implements WSDLLocator {

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
            resolvedImportLocation = parentLocation.substring(0, parentLocation.lastIndexOf("/") + 1) + importLocation;
        }
        
        try {
            importResource = new PathMatchingResourcePatternResolver().getResource(resolvedImportLocation);
            return new InputSource(importResource.getInputStream());
        } catch (IOException e) {
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
        try {
            return importResource.getURI().toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void close() {
    }
}
