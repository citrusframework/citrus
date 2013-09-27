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
 * @author Christoph Deppisch
 */
public class FileTreeModel {

    private String compactFolder;
    private String[] folders;
    private List<FileModel> xmlFiles;
    private List<FileModel> javaFiles;

    /**
     * File properties representing a file in tree.
     */
    public static class FileModel extends HashMap<String, String> {

        private static final String FILENAME = "fileName";
        private static final String EXTENSION = "extension";
        private static final String FILEPATH = "filePath";

        public String getFileName() {
            return get(FILENAME);
        }

        public void setFileName(String fileName) {
            put(FILENAME, fileName);
        }

        public String getExtension() {
            return get(EXTENSION);
        }

        public void setExtension(String extension) {
            put(EXTENSION, extension);
        }

        public String getFilePath() {
            return get(FILEPATH);
        }

        public void setFilePath(String filePath) {
            put(FILEPATH, filePath);
        }
    }

    public String getCompactFolder() {
        return compactFolder;
    }

    public void setCompactFolder(String compactFolder) {
        this.compactFolder = compactFolder;
    }

    public String[] getFolders() {
        return folders;
    }

    public void setFolders(String[] folders) {
        this.folders = folders;
    }

    public List<FileModel> getXmlFiles() {
        return xmlFiles;
    }

    public void setXmlFiles(List<FileModel> xmlFiles) {
        this.xmlFiles = xmlFiles;
    }

    public List<FileModel> getJavaFiles() {
        return javaFiles;
    }

    public void setJavaFiles(List<FileModel> javaFiles) {
        this.javaFiles = javaFiles;
    }

}
