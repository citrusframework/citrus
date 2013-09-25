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

package com.consol.citrus.admin.util;

import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author Martin Maher, Christoph Deppisch
 * @since 2013.04.22
 */
@Component
public class FileHelperImpl implements FileHelper {

    /**
     * {@inheritDoc}
     */
    public String[] getFolders(String directory) {
        if (new File(directory).exists()) {
            String[] files = new File(directory).list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.charAt(0) != '.' && new File(dir, name).isDirectory();
                }
            });


            if (files != null) {
                Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
                return files;
            } else {
                return new String[] {};
            }
        } else {
            throw new CitrusAdminRuntimeException("Could not open directory because it does not exist: " + directory);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getFiles(String directory, final String fileExtension) {
        List<String> fileNames = new ArrayList<String>();

        File startDir = new File(directory);

        if (!startDir.exists()) {
            return new String[] {};
        }

        File[] found = startDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(fileExtension);
            }
        });

        for (File file : found) {
            fileNames.add(FilenameUtils.getBaseName(file.getName()));
        }

        return fileNames.toArray(new String[fileNames.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public File findFileInPath(File path, String filename, boolean recursive) {
        if (!path.isDirectory()) {
            String msg = String.format("Expected a directory but instead got '%s'", path.getAbsolutePath());
            throw new UnsupportedOperationException(msg);
        }

        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                File returnedFile = findFileInPath(file, filename, recursive);
                if (returnedFile != null) {
                    return returnedFile;
                }
            } else {
                if (file.getName().equals(filename)) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String decodeDirectoryUrl(String url, String rootDirectory) {
        String directory = null;

        try {
            directory = URLDecoder.decode(url, "UTF-8"); // TODO use system default encoding?
        } catch (UnsupportedEncodingException e) {
            throw new CitrusAdminRuntimeException("Unable to decode directory URL", e);
        }

        if (directory.equals("/")) {
            if (StringUtils.hasText(rootDirectory)) {
                directory = rootDirectory;
            } else {
                return rootDirectory;
            }
        }

        if (directory.charAt(directory.length() - 1) == '\\') {
            directory = directory.substring(0, directory.length() - 1) + "/";
        } else if (directory.charAt(directory.length() - 1) != '/') {
            directory += "/";
        }

        return directory;
    }
}
