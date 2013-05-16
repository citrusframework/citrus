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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;

import com.consol.citrus.admin.config.*;
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
public class SpringConfigService {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SpringConfigService.class);

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
     * Finds bean definition element by id and type in Spring application context and
     * performs unmarshalling in order to return JaxB object.
     * @param configFile
     * @param id
     * @param type
     * @return
     */
    public <T> T getBeanDefinition(File configFile, String id, Class<T> type) {
        LSParser parser = createLSParser();
        
        GetSpringBeanFilter filter = new GetSpringBeanFilter(id);
        parser.setFilter(filter);
        parser.parseURI(configFile.toURI().toString());
        
        return createJaxbObjectFromElement(filter.getBeanDefinition(), type);
    }
    
    /**
     * Finds all bean definition elements by type in Spring application context and
     * performs unmarshalling in order to return a list of JaxB object.
     * @param configFile
     * @param type
     * @return
     */
    public <T> List<T> getBeanDefinitions(File configFile, Class<T> type) {
        List<T> beanDefinitions = new ArrayList<T>();
        
        LSParser parser = createLSParser();
        
        GetSpringBeansFilter filter = new GetSpringBeansFilter(type);
        parser.setFilter(filter);
        parser.parseURI(configFile.toURI().toString());
        
        for (Element element : filter.getBeanDefinitions()) {
            beanDefinitions.add(createJaxbObjectFromElement(element, type));
        }
        
        return beanDefinitions;
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
     * Method updates an existing Spring bean definition in a XML application context file. Bean definition is 
     * identified by its id or bean name.
     * @param configFile
     * @param id
     */
    public void updateBeanDefinition(File configFile, String id, Object jaxbElement) {
        Document doc = createLSParser().parseURI(configFile.toURI().toString());
        
        LSSerializer serializer = createLSSerializer();
        
        serializer.setFilter(new UpdateSpringBeanFilter(id, createElementFromJaxbObject(jaxbElement)));
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
     * Creates a DOM element node from JAXB element.
     * @param element
     * @param type
     * @return
     */
    private <T> T createJaxbObjectFromElement(Element element, Class<T> type) {
        LSSerializer serializer = createLSSerializer();
        return (T)jaxbHelper.unmarshal(jaxbContext, type, serializer.writeToString(element));
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

        if (serializer.getDomConfig().canSetParameter("element-content-whitespace", true)) {
            serializer.getDomConfig().setParameter("element-content-whitespace", true);
        } else {
            log.warn("Unable to set element-content-whitespace parameter on LSSerializer");
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
