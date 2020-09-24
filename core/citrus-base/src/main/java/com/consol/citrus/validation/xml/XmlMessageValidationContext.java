/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.consol.citrus.validation.context.DefaultValidationContext;
import com.consol.citrus.validation.context.SchemaValidationContext;
import com.consol.citrus.validation.context.ValidationContext;
import org.springframework.core.io.Resource;

/**
 * XML validation context holding validation specific information needed for XML
 * message validation.
 *
 * @author Christoph Deppisch
 */
public class XmlMessageValidationContext extends DefaultValidationContext implements SchemaValidationContext {

    /** Map holding xpath expressions to identify the ignored message elements */
    private final Set<String> ignoreExpressions;

    /** Namespace definitions resolving namespaces in XML message validation */
    private final Map<String, String> namespaces;

    /** dtdResource for DTD validation */
    private final Resource dtdResource;

    /** Map holding control namespaces for validation */
    private final Map<String, String> controlNamespaces;

    /** Should message be validated with its schema definition */
    private final boolean schemaValidation;

    /** Explicit schema repository to use for this validation */
    private final String schemaRepository;

    /** Explicit schema instance to use for this validation */
    private final String schema;

    /**
     * Default constructor.
     */
    public XmlMessageValidationContext() {
        this(Builder.xml());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public XmlMessageValidationContext(XmlValidationContextBuilder<?, ?> builder) {
        this.ignoreExpressions = builder.ignoreExpressions;
        this.namespaces = builder.namespaces;
        this.controlNamespaces = builder.controlNamespaces;
        this.dtdResource = builder.dtdResource;
        this.schemaValidation = builder.schemaValidation;
        this.schemaRepository = builder.schemaRepository;
        this.schema = builder.schema;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder extends XmlValidationContextBuilder<XmlMessageValidationContext, Builder> {

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder xml() {
            return new Builder();
        }

        @Override
        public XmlMessageValidationContext build() {
            return new XmlMessageValidationContext(this);
        }
    }

    /**
     * Base fluent builder for XML validation contexts.
     */
    public static abstract class XmlValidationContextBuilder<T extends XmlMessageValidationContext, S extends XmlValidationContextBuilder<T, S>>
            implements ValidationContext.Builder<T, XmlValidationContextBuilder<T, S>>, XmlNamespaceAware, SchemaValidationContext.Builder<XmlValidationContextBuilder<T, S>> {

        protected final S self;

        private Set<String> ignoreExpressions = new HashSet<>();
        private Map<String, String> namespaces = new HashMap<>();
        private Resource dtdResource;
        private Map<String, String> controlNamespaces = new HashMap<>();
        private boolean schemaValidation = true;
        private String schemaRepository;
        private String schema;

        protected XmlValidationContextBuilder() {
            this.self = (S) this;
        }

        /**
         * Sets schema validation enabled/disabled for this message.
         *
         * @param enabled
         * @return
         */
        public S schemaValidation(final boolean enabled) {
            this.schemaValidation = enabled;
            return self;
        }

        /**
         * Validates XML namespace with prefix and uri.
         *
         * @param prefix
         * @param namespaceUri
         * @return
         */
        public S namespace(final String prefix, final String namespaceUri) {
            this.controlNamespaces.put(prefix, namespaceUri);
            return self;
        }

        /**
         * Validates XML namespace with prefix and uri.
         *
         * @param namespaces
         * @return
         */
        public S namespaces(final Map<String, String> namespaces) {
            this.controlNamespaces.putAll(namespaces);
            return self;
        }

        /**
         * Add namespaces as context to the expression evaluation. Keys are prefixes and values are namespace URIs.
         *
         * @param namespaces
         * @return
         */
        public S namespaceContext(final Map<String, String> namespaces) {
            this.namespaces.putAll(namespaces);
            return self;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         *
         * @param schemaName
         * @return
         */
        public S schema(final String schemaName) {
            this.schema = schemaName;
            return self;
        }

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         *
         * @param schemaRepository
         * @return
         */
        public S schemaRepository(final String schemaRepository) {
            this.schemaRepository = schemaRepository;
            return self;
        }

        /**
         * Sets explicit DTD resource to use for validation.
         *
         * @param dtdResource
         * @return
         */
        public S dtd(final Resource dtdResource) {
            this.dtdResource = dtdResource;
            return self;
        }

        /**
         * Adds ignore path expression for message element.
         *
         * @param path
         * @return
         */
        public S ignore(final String path) {
            this.ignoreExpressions.add(path);
            return self;
        }

        /**
         * Adds a list of ignore path expressions for message element.
         *
         * @param paths
         * @return
         */
        public S ignore(final Set<String> paths) {
            this.ignoreExpressions.addAll(paths);
            return self;
        }

        @Override
        public void setNamespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }
    }

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    /**
     * Get the namespace definitions for this validator.
     * @return the namespaceContext
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Get the dtd resource.
     * @return the dtdResource
     */
    public Resource getDTDResource() {
        return dtdResource;
    }

    /**
     * Get control namespace elements.
     * @return the controlNamespaces
     */
    public Map<String, String> getControlNamespaces() {
        return controlNamespaces;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    @Override
    public String getSchemaRepository() {
        return schemaRepository;
    }

    @Override
    public String getSchema() {
        return schema;
    }

}
