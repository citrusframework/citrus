/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.yaml.actions;

import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class AntRun implements TestActionBuilder<AntRunAction>, ReferenceResolverAware {

    private final AntRunAction.Builder builder = new AntRunAction.Builder();

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        builder.description(value);
    }

    /**
     * Sets the build file path.
     * @param buildFilePath
     * @return
     */
    public void setBuildFile(String buildFilePath) {
        builder.buildFilePath(buildFilePath);
    }

    /**
     * Build target name to call.
     * @param execute
     */
    public void setExecute(Execute execute) {
        if (execute.getTarget() != null) {
            builder.target(execute.target);
        }

        if (execute.getTargets() != null) {
            builder.targets(execute.targets.split(","));
        }
    }

    /**
     * Adds build properties file.
     * @param file
     */
    public void setPropertyFile(String file) {
        builder.propertyFile(file);
    }

    /**
     * Adds a build property by name and value.
     * @param properties
     */
    public void setProperties(List<Property> properties) {
        properties.forEach(prop -> builder.property(prop.getName(), prop.getValue()));
    }

    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    public void setBuildListener(String buildListener) {
        builder.listenerName(buildListener);
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @Override
    public AntRunAction build() {
        builder.setReferenceResolver(referenceResolver);
        return builder.build();
    }

    public static class Property {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Execute {

        protected String target;
        protected String targets;

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public String getTargets() {
            return targets;
        }

        public void setTargets(String targets) {
            this.targets = targets;
        }
    }
}
