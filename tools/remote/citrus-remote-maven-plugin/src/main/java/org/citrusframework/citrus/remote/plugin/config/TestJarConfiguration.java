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

package org.citrusframework.citrus.remote.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class TestJarConfiguration implements Serializable {

    @Parameter
    private File testClassesDirectory;

    @Parameter(defaultValue = "tests")
    private String classifier;

    /**
     * List of files to include. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the test JAR.
     */
    @Parameter
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the test JAR.
     */
    @Parameter
    private String[] excludes;

    /**
     * Gets the testClassesDirectory.
     *
     * @return
     */
    public File getTestClassesDirectory() {
        return testClassesDirectory;
    }

    /**
     * Sets the testClassesDirectory.
     *
     * @param testClassesDirectory
     */
    public void setTestClassesDirectory(File testClassesDirectory) {
        this.testClassesDirectory = testClassesDirectory;
    }

    /**
     * Gets the classifier.
     *
     * @return
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets the classifier.
     *
     * @param classifier
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Gets the includes.
     *
     * @return
     */
    public String[] getIncludes() {
        return includes;
    }

    /**
     * Sets the includes.
     *
     * @param includes
     */
    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    /**
     * Gets the excludes.
     *
     * @return
     */
    public String[] getExcludes() {
        return excludes;
    }

    /**
     * Sets the excludes.
     *
     * @param excludes
     */
    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}
