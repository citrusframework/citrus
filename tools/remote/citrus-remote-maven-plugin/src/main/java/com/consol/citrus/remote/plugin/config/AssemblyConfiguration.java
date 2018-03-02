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

package com.consol.citrus.remote.plugin.config;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AssemblyConfiguration implements Serializable {

    @Parameter
    private AssemblyDescriptorConfiguration descriptor;

    @Parameter
    private MavenArchiveConfiguration archive;

    @Parameter(property = "citrus.remote.test.jar.provided", defaultValue = "false")
    private boolean testJarProvided = false;

    private File outputDirectory;
    private File workingDirectory;
    private File temporaryRootDirectory;

    /**
     * Gets the descriptor.
     *
     * @return
     */
    public AssemblyDescriptorConfiguration getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the descriptor.
     *
     * @param descriptor
     */
    public void setDescriptor(AssemblyDescriptorConfiguration descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Gets the archive.
     *
     * @return
     */
    public MavenArchiveConfiguration getArchive() {
        return archive;
    }

    /**
     * Sets the archive.
     *
     * @param archive
     */
    public void setArchive(MavenArchiveConfiguration archive) {
        this.archive = archive;
    }

    /**
     * Gets the outputDirectory.
     *
     * @return
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the outputDirectory.
     *
     * @param outputDirectory
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Gets the workingDirectory.
     *
     * @return
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the workingDirectory.
     *
     * @param workingDirectory
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the temporaryRootDirectory.
     *
     * @return
     */
    public File getTemporaryRootDirectory() {
        return temporaryRootDirectory;
    }

    /**
     * Sets the temporaryRootDirectory.
     *
     * @param temporaryRootDirectory
     */
    public void setTemporaryRootDirectory(File temporaryRootDirectory) {
        this.temporaryRootDirectory = temporaryRootDirectory;
    }

    /**
     * Gets the testJarProvided.
     *
     * @return
     */
    public boolean isTestJarProvided() {
        return testJarProvided;
    }

    /**
     * Sets the testJarProvided.
     *
     * @param testJarProvided
     */
    public void setTestJarProvided(boolean testJarProvided) {
        this.testJarProvided = testJarProvided;
    }
}
