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

import com.consol.citrus.admin.jaxb.CitrusNamespacePrefixMapper;
import com.consol.citrus.admin.spring.config.*;
import com.consol.citrus.admin.util.JAXBHelper;
import com.consol.citrus.util.XMLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.*;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import java.io.File;
import java.util.*;

/**
 * Service is able to add, remove update Spring XML bean definitions to some ordinary
 * Spring XML application context. Service uses XML load and save parser and serializer
 * in order to manipulate XML contents.
 * 
 * @author Christoph Deppisch
 */
@Component
public class SpringBeanService {

    @Autowired
    protected JAXBHelper jaxbHelper;
    
    /** JaxBContext holds xml bean definition packages known to this context */
    private JAXBContext jaxbContext;
    
    @PostConstruct
    protected void initJaxbContext() {
        jaxbContext = jaxbHelper.createJAXBContextByPath(
                "com.consol.citrus.admin.spring.model",
                "com.consol.citrus.model.config.core",
                "com.consol.citrus.model.config.ws",
                "com.consol.citrus.model.config.mail",
                "com.consol.citrus.model.config.ssh",
                "com.consol.citrus.model.config.vertx",
                "com.consol.citrus.model.config.http");
    }

    /**
     * Reads file import locations from Spring bean application context.
     * @param configFile
     * @return
     */
    public List<File> getConfigImports(File configFile) {
        LSParser parser = XMLUtils.createLSParser();

        GetSpringImportsFilter filter = new GetSpringImportsFilter(configFile);
        parser.setFilter(filter);
        parser.parseURI(configFile.toURI().toString());

        return filter.getImportedFiles();
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
        LSParser parser = XMLUtils.createLSParser();

        GetSpringBeanFilter filter = new GetSpringBeanFilter(id, type);
        parser.setFilter(filter);

        List<File> configFiles = new ArrayList<File>();
        configFiles.add(configFile);
        configFiles.addAll(getConfigImports(configFile));

        for (File file : configFiles) {
            parser.parseURI(file.toURI().toString());

            if (filter.getBeanDefinition() != null) {
                return createJaxbObjectFromElement(filter.getBeanDefinition(), type);
            }
        }

        return null;
    }
    
    /**
     * Finds all bean definition elements by type in Spring application context and
     * performs unmarshalling in order to return a list of JaxB object.
     * @param configFile
     * @param type
     * @return
     */
    public <T> List<T> getBeanDefinitions(File configFile, Class<T> type) {
        return getBeanDefinitions(configFile, type, null);
    }

    /**
     * Finds all bean definition elements by type and attribute values in Spring application context and
     * performs unmarshalling in order to return a list of JaxB object.
     * @param configFile
     * @param type
     * @param attributes
     * @return
     */
    public <T> List<T> getBeanDefinitions(File configFile, Class<T> type, Map<String, String> attributes) {
        List<T> beanDefinitions = new ArrayList<T>();

        List<File> importedFiles = getConfigImports(configFile);
        for (File importLocation : importedFiles) {
            beanDefinitions.addAll(getBeanDefinitions(importLocation, type, attributes));
        }

        LSParser parser = XMLUtils.createLSParser();

        GetSpringBeansFilter filter = new GetSpringBeansFilter(type, attributes);
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
        Document doc = XMLUtils.createLSParser().parseURI(configFile.toURI().toString());
        
        LSSerializer serializer = XMLUtils.createLSSerializer();

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
        Document doc = XMLUtils.createLSParser().parseURI(configFile.toURI().toString());
        
        LSSerializer serializer = XMLUtils.createLSSerializer();

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
        LSSerializer serializer = XMLUtils.createLSSerializer();

        UpdateSpringBeanFilter filter = new UpdateSpringBeanFilter(id, createElementFromJaxbObject(jaxbElement));
        serializer.setFilter(filter);

        List<File> configFiles = new ArrayList<File>();
        configFiles.add(configFile);
        configFiles.addAll(getConfigImports(configFile));

        LSParser parser = XMLUtils.createLSParser();
        GetSpringBeanFilter getBeanFilter = new GetSpringBeanFilter(id, jaxbElement.getClass());
        parser.setFilter(getBeanFilter);

        for (File file : configFiles) {
            Document doc = parser.parseURI(file.toURI().toString());

            if (getBeanFilter.getBeanDefinition() != null) {
                serializer.writeToURI(doc, file.toURI().toString());
                return;
            }
        }
    }
    
    /**
     * Creates a DOM element node from JAXB element.
     * @param jaxbElement
     * @return
     */
    private Element createElementFromJaxbObject(Object jaxbElement) {
        LSInput input = XMLUtils.createLSInput();
        input.setStringData(jaxbHelper.marshal(jaxbContext, jaxbElement));

        Element element = (Element)XMLUtils.createLSParser().parse(input).getDocumentElement().cloneNode(true);

        // remove all namespace declarations from element as we have set those already in root element
        element.getAttributes().removeNamedItem(XMLConstants.XMLNS_ATTRIBUTE);

        CitrusNamespacePrefixMapper namespacePrefixMapper = new CitrusNamespacePrefixMapper();
        for (String prefix : namespacePrefixMapper.getNamespaceMappings().values()) {
            if (element.hasAttribute(XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix)) {
                element.getAttributes().removeNamedItem(XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix);
            }
        }

        return element;
    }
    
    /**
     * Creates a DOM element node from JAXB element.
     * @param element
     * @param type
     * @return
     */
    private <T> T createJaxbObjectFromElement(Element element, Class<T> type) {
        LSSerializer serializer = XMLUtils.createLSSerializer();
        return jaxbHelper.unmarshal(jaxbContext, type, serializer.writeToString(element));
    }

}
