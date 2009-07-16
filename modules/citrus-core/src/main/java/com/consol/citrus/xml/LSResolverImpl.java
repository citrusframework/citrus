package com.consol.citrus.xml;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public class LSResolverImpl implements LSResourceResolver {
    DOMImplementationLS domImpl;
    
    public LSResolverImpl(DOMImplementationLS domImpl) {
        this.domImpl = domImpl;
    }
    
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        LSInput input = domImpl.createLSInput();
        try {
            input.setByteStream(new ClassPathResource(systemId).getInputStream());
        } catch (IOException e) {
            return null;
        }
        
        return input;
    }

}
