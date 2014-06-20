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

package com.consol.citrus.admin.model;

import java.util.*;

/**
 * Model for file tree Handlebars template. Constructs a file tree for one single directory with nested folders and files.
 * Supports compact representation of empty folder sequences.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class FileTreeModel {

    private String compactFolder;
    private String[] folders;
    private List<TestFileModel> xmlFiles;
    private List<TestFileModel> javaFiles;

    /**
     * File properties representing a file in tree.
     */
    public static class TestFileModel extends HashMap<String, Object> {

        private static final String FILENAME = "fileName";
        private static final String EXTENSION = "extension";
        private static final String FILEPATH = "filePath";
        private static final String TEST_METHODS = "testMethods";

        public void setFileName(String fileName) {
            put(FILENAME, fileName);
        }

        public void setExtension(String extension) {
            put(EXTENSION, extension);
        }

        public void setFilePath(String filePath) {
            put(FILEPATH, filePath);
        }

        public void setTestMethods(List<String> methods) {
            put(TEST_METHODS, methods);
        }

        public String getFilename() {
            return get(FILENAME).toString();
        }

        public List<String> getTestMethods() {
            return (List<String>) get(TEST_METHODS);
        }
    }

    /**
     * Gets the compact folder.
     * @return
     */
    public String getCompactFolder() {
        return compactFolder;
    }

    /**
     * Sets the compact folder.
     * @param compactFolder
     */
    public void setCompactFolder(String compactFolder) {
        this.compactFolder = compactFolder;
    }

    /**
     * Gets the folders.
     * @return
     */
    public String[] getFolders() {
        return folders;
    }

    /**
     * Sets the folders.
     * @param folders
     */
    public void setFolders(String[] folders) {
        this.folders = Arrays.copyOf(folders, folders.length);
    }

    /**
     * Gets the xml files.
     * @return
     */
    public List<TestFileModel> getXmlFiles() {
        return xmlFiles;
    }

    /**
     * Sets the xml files.
     * @param xmlFiles
     */
    public void setXmlFiles(List<TestFileModel> xmlFiles) {
        this.xmlFiles = xmlFiles;
    }

    /**
     * Gets the Java files.
     * @return
     */
    public List<TestFileModel> getJavaFiles() {
        return javaFiles;
    }

    /**
     * Sets the Java files.
     * @param javaFiles
     */
    public void setJavaFiles(List<TestFileModel> javaFiles) {
        this.javaFiles = javaFiles;
    }

}
