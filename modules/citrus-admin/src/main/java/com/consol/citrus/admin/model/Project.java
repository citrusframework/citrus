/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.admin.model;

import com.consol.citrus.admin.configuration.*;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Project model holding Citrus project related information.
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class Project {

    private String name;
    private String description;
    private String version;
    private final String projectHome;
    private String basePackage = System.getProperty(PropertyConstants.BASE_PACKAGE, "com.consol.citrus");

    /** List of run configurations */
    private List<RunConfiguration> runConfigurationList = new ArrayList<RunConfiguration>();

    /**
     * Default constructor using project home directory.
     * @param projectHome
     */
    public Project(String projectHome) {
        try {
            this.projectHome = new File(projectHome).getCanonicalPath();
        } catch (IOException e) {
            throw new CitrusAdminRuntimeException("Unable to access project home directory", e);
        }

        this.version = "1.4";
        this.name = "citrus-project";
        this.description = "Citrus base integration project holding all internal " +
                "integration tests for modules citrus-core, citrus-http, citrus-ws, citrus-ssh, citrus-vertx and citrus-mail.";

        MavenRunConfiguration mavenRunConfiguration = new MavenRunConfiguration();
        mavenRunConfiguration.setId("Maven");
        mavenRunConfiguration.setStandard(true);
        runConfigurationList.add(mavenRunConfiguration);

        ClasspathRunConfiguration classpathRunConfiguration = new ClasspathRunConfiguration();
        classpathRunConfiguration.setId("Classpath");
        runConfigurationList.add(classpathRunConfiguration);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public RunConfiguration getRunConfiguration(String id) {
        for (RunConfiguration runConfiguration : runConfigurationList) {
            if (runConfiguration.getId().equals(id)) {
                return runConfiguration;
            }
        }

        throw new CitrusAdminRuntimeException("Unknown run configuration: " + id);
    }

    public List<RunConfiguration> getRunConfigurations() {
        return runConfigurationList;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getProjectHome() {
        return this.projectHome;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
