/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class ZipMessage extends DefaultMessage {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ZipMessage.class);

    /** Entries in this zip message */
    private final List<Entry> entries = new ArrayList<>();

    public ZipMessage() {
        super();
    }

    @Override
    public byte[] getPayload() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Entry entry : entries) {
                addToZip("", entry, zos);
            }
        } catch(IOException e) {
            throw new CitrusRuntimeException("Failed to create zip archive", e);
        }

        return bos.toByteArray();
    }

    /**
     * Adds new zip archive entry. Resource can be a file or directory. In case of directory all files will be automatically added
     * to the zip archive. Directory structures are retained throughout this process.
     *
     * @param resource
     * @return
     */
    public ZipMessage addEntry(Resource resource) {
        try {
            addEntry(new Entry(resource.getFile()));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to read zip entry content from given resource: %s", resource.getLocation()), e);
        }
        return this;
    }

    /**
     * Adds new zip archive entry. Resource can be a file or directory. In case of directory all files will be automatically added
     * to the zip archive. Directory structures are retained throughout this process.
     *
     * @param resource
     * @return
     */
    public ZipMessage addEntry(Path resource) {
        try {
            addEntry(new Entry(resource.toFile()));
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to read zip entry content from given resource: %s", resource), e);
        }
        return this;
    }

    /**
     * Adds new zip archive entry. Entry can be a file or directory. In case of directory all files will be automatically added
     * to the zip archive. Directory structures are retained throughout this process.
     * @param file
     * @return
     */
    public ZipMessage addEntry(File file) {
        try {
            addEntry(new Entry(file));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read zip entry content from given file", e);
        }
        return this;
    }

    /**
     * Adds new zip archive entry with given content.
     *
     * @param fileName
     * @param content
     * @return
     */
    public ZipMessage addEntry(String fileName, byte[] content) {
        Entry entry = new Entry(fileName);
        entry.setContent(content);
        addEntry(entry);
        return this;
    }

    /**
     * Adds new zip archive entry.
     * @param entry
     * @return
     */
    public ZipMessage addEntry(Entry entry) {
        entries.add(entry);
        return this;
    }

    /*
     * recursively add files to the zip files
     */
    private void addToZip(String path, Entry entry, ZipOutputStream zos) throws IOException {
        String name = (path.endsWith("/") ? path : path + "/") + entry.getName();
        if (entry.isDirectory()) {
            logger.debug("Adding directory to zip: " + name);

            zos.putNextEntry(new ZipEntry(name.endsWith("/") ? name : name + "/"));
            for (Entry child : entry.getEntries()) {
                if (!StringUtils.hasText(path)) {
                    addToZip(entry.getName(), child, zos);
                } else {
                    addToZip(name, child, zos);
                }
            }
            zos.closeEntry();
        } else {
            logger.debug("Adding file to zip: " + name);

            zos.putNextEntry(new ZipEntry(name));
            zos.write(entry.getContent());
            zos.closeEntry();
        }
    }

    /**
     * Zip message entry representing a directory or file in the zip message.
     */
    public static class Entry {
        /** Entry name - file name or directory name */
        private final String name;

        /** Optional child entries - for directories */
        private final List<Entry> entries = new ArrayList<>();

        /** Binary content of this entry - empty for directories */
        private byte[] content;

        /**
         * Constructor initializing name.
         * @param name
         */
        public Entry(String name) {
            this.name = name;
        }

        public Entry(File file) throws IOException {
            this(file.isDirectory() ? file.getName() + "/" : file.getName(), file);
        }

        public Entry(String name, File file) throws IOException {
            this(name);

            if (file.isDirectory()) {
                for (File child : Optional.ofNullable(file.listFiles()).orElseGet(() -> new File[] {})) {
                    entries.add(new Entry(child));
                }
            } else {
                this.content = FileUtils.copyToByteArray(file);
            }
        }

        /**
         * Gets the name.
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the directory.
         *
         * @return
         */
        public boolean isDirectory() {
            return StringUtils.hasText(name) && name.endsWith("/");
        }

        /**
         * Gets the entries.
         *
         * @return
         */
        public List<Entry> getEntries() {
            return entries;
        }

        /**
         * Adds new zip archive entry.
         * @param entry
         * @return
         */
        public Entry addEntry(Entry entry) {
            entries.add(entry);
            return this;
        }


        /**
         * Gets the content.
         *
         * @return
         */
        public byte[] getContent() {
            return content;
        }

        /**
         * Sets the content.
         *
         * @param content
         */
        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}
