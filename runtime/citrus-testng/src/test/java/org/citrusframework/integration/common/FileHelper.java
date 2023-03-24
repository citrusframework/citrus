/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.integration.common;

import java.io.File;
import java.io.IOException;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Common helper methods for working with files when testing
 */
public interface FileHelper {

    /**
     * Creates a tmp file with no content. This file will be deleted on JVM exit.
     *
     * @return the tmp file
     */
    static File createTmpFile() {
        final File tempFile;
        try {
            tempFile = File.createTempFile("citrus", ".tmp");
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
}
