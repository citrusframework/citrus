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

package org.citrusframework.xml.actions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.AntRunAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "ant")
public class AntRun implements TestActionBuilder<AntRunAction>, ReferenceResolverAware {

    private final AntRunAction.Builder builder = new AntRunAction.Builder();

    private ReferenceResolver referenceResolver;

    @XmlElement
    public AntRun setDescription(String value) {
        builder.description(value);
        return this;
    }

    /**
     * Sets the build file path.
     * @param buildFilePath
     * @return
     */
    @XmlAttribute(name = "build-file")
    public AntRun setBuildFile(String buildFilePath) {
        builder.buildFilePath(buildFilePath);
        return this;
    }

    /**
     * Build target name to call.
     * @param execute
     */
    @XmlElement
    public AntRun setExecute(Execute execute) {
        if (execute.getTarget() != null) {
            builder.target(execute.target);
        }

        if (execute.getTargets() != null) {
            builder.targets(execute.targets.split(","));
        }
        return this;
    }

    /**
     * Adds a build property by name and value.
     * @param properties
     */
    @XmlElement
    public AntRun setProperties(Properties properties) {
        if (properties.getFile() != null) {
            builder.propertyFile(properties.getFile());
        }

        properties.getProperties().forEach(prop -> builder.property(prop.getName(), prop.getValue()));
        return this;
    }

    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    @XmlAttribute(name = "build-listener")
    public AntRun setBuildListener(String buildListener) {
        builder.listenerName(buildListener);
        return this;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "properties"
    })
    public static class Properties {

        @XmlElement(name = "property")
        protected List<Property> properties;

        @XmlAttribute
        protected String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public List<Property> getProperties() {
            if (properties == null) {
                properties = new ArrayList<>();
            }

            return properties;
        }

        public void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {})
        public static class Property {
            @XmlAttribute
            private String name;
            @XmlAttribute
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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    public static class Execute {

        @XmlAttribute(name = "target")
        protected String target;

        @XmlAttribute(name = "targets")
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
