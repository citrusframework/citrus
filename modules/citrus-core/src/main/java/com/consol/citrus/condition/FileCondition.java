/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.condition;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Tests for the presence of a file and returns true if the file exists
 *
 * @author Martin Maher
 * @since 2.4
 */
public class FileCondition extends AbstractCondition {

    /** File path to check for existence */
    private String filePath;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(FileCondition.class);

    /**
     * Default constructor.
     */
    public FileCondition() {
        super("file-check");
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Checking file path '%s'", filePath));
        }

        try {
            return FileUtils.getFileResource(filePath, context).getFile().isFile();
        } catch (IOException e) {
            log.warn(String.format("Failed to access file resource '%s'", e.getMessage()));
            return false;
        }
    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return String.format("File condition success - file '%s' does exist", context.replaceDynamicContentInString(filePath));
    }

    @Override
    public String getErrorMessage(TestContext context) {
        return String.format("Failed to check file condition - file '%s' does not exist", context.replaceDynamicContentInString(filePath));
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
