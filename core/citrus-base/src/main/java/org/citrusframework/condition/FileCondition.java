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

package org.citrusframework.condition;

import java.io.File;

import org.citrusframework.context.TestContext;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the presence of a file and returns true if the file exists
 *
 * @author Martin Maher
 * @since 2.4
 */
public class FileCondition extends AbstractCondition {

    /** File path to check for existence */
    private String filePath;
    private File file;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(FileCondition.class);

    /**
     * Default constructor.
     */
    public FileCondition() {
        super("file-check");
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Checking file path '%s'", file != null ? file.getPath() : filePath));
        }

        if (file != null) {
            return file.exists() && file.isFile();
        } else {
            try {
                return FileUtils.getFileResource(context.replaceDynamicContentInString(filePath), context).getFile().isFile();
            } catch (Exception e) {
                logger.warn(String.format("Failed to access file resource '%s'", e.getMessage()));
                return false;
            }
        }

    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return String.format("File condition success - file '%s' does exist", file != null ? file.getPath() : context.replaceDynamicContentInString(filePath));
    }

    @Override
    public String getErrorMessage(TestContext context) {
        return String.format("Failed to check file condition - file '%s' does not exist", file != null ? file.getPath() : context.replaceDynamicContentInString(filePath));
    }

    /**
     * Gets the filePath.
     *
     * @return The path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets the filePath.
     *
     * @param filePath The path to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the file.
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file The file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "FileCondition{" +
                "filePath='" + filePath + '\'' +
                ", file=" + file +
                ", name=" + getName() +
                '}';
    }
}
