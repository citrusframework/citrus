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

import java.io.File;

/**
 * Useful file utilities.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.22
 */
public interface FileHelper {
    
    /**
     * Gets list of folder names and paths for given root directory.
     * @param directory
     * @return
     */
    String[] getFolders(File directory);

    /**
     * Scans file path for filename and returns file instance.
     * @param directory
     * @param filename
     * @param recursive
     * @return
     */
    File findFileInPath(File directory, String filename, boolean recursive);


    /**
     * Decodes directory URL to proper file path.
     * @param url
     * @return
     */
    String decodeDirectoryUrl(String url, String rootDirectory);
}
