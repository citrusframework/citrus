/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.service;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;

import com.consol.citrus.admin.config.AddSpringBeanFilter;
import com.consol.citrus.admin.config.RemoveSpringBeanFilter;
import com.consol.citrus.admin.util.JAXBHelper;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Service is able to add, remove update Spring XML bean definitions to some ordinary
 * Spring XML application context. Service uses XML load and save parser and serializer
 * in order to manipulate XML contents.
 * 
 * @author Christoph Deppisch
 */
@Component
public class SpringBeanConfigService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SpringBeanConfigService.class);

    @Autowired
    protected JAXBHelper jaxbHelper;
    
    /** JaxBContext holds xml bean definition packages known to this context */
    private JAXBContext jaxbContext;
    
    /** DOM implementation */
    private static DOMImplementationRegistry registry = null;
    private static DOMImplementationLS domImpl = null;
    
    static {
        try {
            registry = DOMImplementationRegistry.newInstance();
            
            DOMImplementationList domImplList = registry.getDOMImplementationList("LS");

            if (log.isDebugEnabled()) {
                for (int i = 0; i < domImplList.getLength(); i++) {
                    log.debug("Found DOMImplementationLS: " + domImplList.item(i));
                }
            }
            
            domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            if (log.isDebugEnabled()) {
                log.debug("Using DOMImplementationLS: " + domImpl.getClass().getName());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    @PostConstruct
    protected void initJaxbContext() {
        jaxbContext = jaxbHelper.createJAXBContextByPath("com.consol.citrus.model.config.core");
    }
    
    /**
     * Method adds a new Spring bean definition to the XML application context file.
     * @param configFile
     * @param jaxbElement
     */
    public void addBeanDefinition(File configFile, Object jaxbElement) {
        Document doc = createLSParser().parseURI(configFile.toURI().toString());
        
        LSSerializer serializer = createLSSerializer();
        
        serializer.setFilter(new AddSpringBeanFilter(createElementFromJaxbObject(jaxbElement)));
        serializer.writeToURI(doc, configFile.toURI().toString());
    }
    
    /**
     * Method removes a Spring bean definition from the XML application context file. Bean definition is 
     * identified by its id or bean name.
     * @param configFile
     * @param id
     */
    public void removeBeanDefinition(File configFile, String id) {
        Document doc = createLSParser().parseURI(configFile.toURI().toString());
        
        LSSerializer serializer = createLSSerializer();
        
        serializer.setFilter(new RemoveSpringBeanFilter(id));
        serializer.writeToURI(doc, configFile.toURI().toString());
    }
    
    /**
     * Creates a DOM element node from JAXB element.
     * @param jaxbElement
     * @return
     */
    private Element createElementFromJaxbObject(Object jaxbElement) {
        LSInput input = domImpl.createLSInput();
        input.setStringData(jaxbHelper.marshal(jaxbContext, jaxbElement));
        
        return (Element)createLSParser().parse(input).getDocumentElement().cloneNode(true);
    }

    /**
     * Creates basic LSParser instance and sets common 
     * properties and configuration parameters.
     * @return
     */
    private LSParser createLSParser() {
        LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        
        if (parser.getDomConfig().canSetParameter("cdata-sections", true)) {
            parser.getDomConfig().setParameter("cdata-sections", true);
        } else {
            log.warn("Unable to set cdata-sections parameter on LSParser");
        }
        
        if (parser.getDomConfig().canSetParameter("split-cdata-sections", false)) {
            parser.getDomConfig().setParameter("split-cdata-sections", false);
        } else {
            log.warn("Unable to set split-cdata-sections parameter on LSParser");
        }
        
        return parser;
    }
    
    /**
     * Creates basic LSSerializer instance and sets common 
     * properties and configuration parameters.
     * @return
     */
    private LSSerializer createLSSerializer() {
        LSSerializer serializer = domImpl.createLSSerializer();
        
        if (log.isDebugEnabled()) {
            log.debug("Using LSSerializer: " + serializer.getClass().getName());
        }

        if (serializer.getDomConfig().canSetParameter("split-cdata-sections", false)) {
            serializer.getDomConfig().setParameter("split-cdata-sections", false);
        } else {
            log.warn("Unable to set split-cdata-sections parameter on LSSerializer");
        }
        
        if (serializer.getDomConfig().canSetParameter("format-pretty-print", true)) {
            serializer.getDomConfig().setParameter("format-pretty-print", true);
        } else {
            log.warn("Unable to set format-pretty-print parameter on LSSerializer - XML output will be in pure format");
        }
        
        return serializer;
    }
}
