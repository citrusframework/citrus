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

package com.consol.citrus.admin.spring.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter looks for import elements in Spring bean application context and returns a list
 * of file resources for these imported files.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class GetSpringImportsFilter implements LSSerializerFilter, LSParserFilter {

    /** The config resource which holds the imports */
    private File parentConfigFile;

    /** List of imported resources filtered from application context */
    private List<File> importedFiles = new ArrayList<File>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(GetSpringImportsFilter.class);

    /**
     * Constructor using the parent config resource field.
     * @param parentConfigFile
     */
    public GetSpringImportsFilter(File parentConfigFile) {
        this.parentConfigFile = parentConfigFile;
    }

    @Override
    public short startElement(Element element) {
        if (DomUtils.nodeNameEquals(element, "import")) {
            String resourceLocation = element.getAttribute("resource");

            if (StringUtils.hasText(resourceLocation)) {
                if (resourceLocation.startsWith("classpath:")) {
                    resourceLocation = resourceLocation.substring("classpath:".length());
                } else if (resourceLocation.startsWith("file:")) {
                    resourceLocation = resourceLocation.substring("file:".length());
                }

                try {
                    File importedFile = new FileSystemResource(parentConfigFile.getParentFile().getCanonicalPath() +
                            File.separator + resourceLocation).getFile();

                    if (importedFile.exists()) {
                        importedFiles.add(importedFile);
                    }
                } catch (IOException e) {
                    log.warn("Unable to resolve imported file resource location", e);
                }
            }
        }

        return NodeFilter.FILTER_ACCEPT;
    }

    @Override
    public int getWhatToShow() {
        return NodeFilter.SHOW_ELEMENT;
    }

    @Override
    public short acceptNode(Node node) {
        return NodeFilter.FILTER_ACCEPT;
    }

    /**
     * Gets the filtered import locations.
     * @return
     */
    public List<File> getImportedFiles() {
        return importedFiles;
    }
}
