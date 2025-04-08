/*
 * Copyright the original author or authors.
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

package org.citrusframework.agent.plugin.config;

import java.util.List;
import java.util.Map;

import org.apache.maven.plugins.annotations.Parameter;

public class RunConfiguration {

    @Parameter
    private List<String> classes;

    @Parameter
    private List<String> packages;

    @Parameter
    private List<String> includes;

    @Parameter
    private List<String> sources;

    @Parameter
    private Map<String, String> systemProperties;

    @Parameter(property = "citrus.agent.run.async", defaultValue = "false")
    private boolean async;

    @Parameter(property = "citrus.agent.run.polling.interval", defaultValue = "2000")
    private long pollingInterval;

    @Parameter(property = "citrus.agent.run.engine", defaultValue = "testng")
    private String engine;

    public RunConfiguration() {
        engine = "testng";
        pollingInterval = 2000L;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public boolean hasClasses() {
        return getClasses() != null && !getClasses().isEmpty();
    }

    public boolean hasPackages() {
        return getPackages() != null && !getPackages().isEmpty();
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> properties) {
        this.systemProperties = properties;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }
}
