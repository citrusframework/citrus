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

package org.citrusframework.generate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.citrusframework.CitrusSettings;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class AbstractTestGenerator<T extends TestGenerator> implements TestGenerator<T> {

    /** Test name */
    private String name;

    /** Test author */
    private String author;

    /** Test description */
    private String description;

    /** Target package of test case */
    private String targetPackage;

    /** Source directory for tests */
    private String srcDirectory = CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY;

    /** Target unit testing framework */
    private UnitFramework framework;

    /** Target file extension */
    private String fileExtension;

    /** Should generate disabled test */
    private boolean disabled = false;

    protected T self;

    public AbstractTestGenerator() {
        self = (T) this;
    }

    /**
     * Set name via builder method.
     * @param name
     * @return
     */
    public T withName(String name) {
        this.name = name;
        return self;
    }

    /**
     * Set author via builder method.
     * @param author
     * @return
     */
    public T withAuthor(String author) {
        this.author = author;
        return self;
    }

    /**
     * Set description via builder method.
     * @param description
     * @return
     */
    public T withDescription(String description) {
        this.description = description;
        return self;
    }

    /**
     * Set file extension via builder method.
     * @param extension
     * @return
     */
    public T withFileExtension(String extension) {
        this.fileExtension = extension;
        return self;
    }

    /**
     * Set package via builder method.
     * @param targetPackage
     * @return
     */
    public T usePackage(String targetPackage) {
        this.targetPackage = targetPackage;
        return self;
    }

    /**
     * Set test source directory via builder method.
     * @param srcDirectory
     * @return
     */
    public T useSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
        return self;
    }

    /**
     * Set the unit testing framework to use.
     * @param framework
     * @return
     */
    public T withFramework(UnitFramework framework) {
        this.framework = framework;
        return self;
    }

    /**
     * Set the disabled state to use.
     * @param disabled
     * @return
     */
    public T withDisabled(boolean disabled) {
        this.disabled = disabled;
        return self;
    }

    /**
     * Construct default test method name from test name.
     * @return
     */
    protected String getMethodName() {
        return getName().substring(0,1).toLowerCase() + getName().substring(1);
    }

    /**
     * Get current date in special format.
     * @return
     */
    protected String getCreationDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(GregorianCalendar.getInstance().getTime());
    }

    /**
     * Get current date time in special format.
     * @return
     */
    protected String getUpdateDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(GregorianCalendar.getInstance().getTime());
    }

    /**
     * Construct proper target file to save test content to.
     * @return
     */
    protected File getTargetFile() {
        return new File(getSrcDirectory() + File.separator + getTargetPackage().replace('.', File.separatorChar) + File.separator + getName() + getFileExtension());
    }

    /**
     * Set the test name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the test name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the test author.
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Get the test author.
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the test description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the test description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the test package.
     * @param targetPackage the targetPackage to set
     */
    public void setPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Get the test source directory.
     * @return the srcDirectory
     */
    public String getSrcDirectory() {
        return srcDirectory;
    }

    /**
     * Set the test source directory.
     * @param srcDirectory the srcDirectory to set
     */
    public void setSrcDirectory(String srcDirectory) {
        this.srcDirectory = srcDirectory;
    }

    /**
     * Get the test package.
     * @return the targetPackage
     */
    public String getPackage() {
        return targetPackage;
    }

    /**
     * Get the target package.
     * @return the targetPackage
     */
    public String getTargetPackage() {
        return targetPackage;
    }

    /**
     * Set the target package.
     * @param targetPackage the targetPackage to set
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /**
     * Get the unit test framework (usually TestNG or JUnit).
     * @return the framework
     */
    public UnitFramework getFramework() {
        return framework;
    }

    /**
     * Set the unit test framework.
     * @param framework the framework to set
     */
    public void setFramework(UnitFramework framework) {
        this.framework = framework;
    }

    /**
     * Gets the fileExtension.
     *
     * @return
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Sets the fileExtension.
     *
     * @param fileExtension
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Gets the disabled.
     *
     * @return
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled.
     *
     * @param disabled
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
