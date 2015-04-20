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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;

/**
 * @author Martin Maher, Christoph Deppisch
 * @since 2013.04.22
 */
@Component
public class FileHelperImpl implements FileHelper {

    @Override
    public String[] getFolders(File directory) {
        if (directory.exists()) {
            String[] files = directory.list(new FilenameFilter() {
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

    @Override
    public File findFileInPath(File directory, String filename, boolean recursive) {
        if (!directory.isDirectory()) {
            String msg = String.format("Expected a directory but instead got '%s'", directory.getAbsolutePath());
            throw new UnsupportedOperationException(msg);
        }

        File[] files = directory.listFiles();
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

    @Override
    public String decodeDirectoryUrl(String url, String rootDirectory) {
        String directory;

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
