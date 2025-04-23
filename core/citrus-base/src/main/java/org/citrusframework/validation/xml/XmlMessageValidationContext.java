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

package org.citrusframework.validation.xml;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.context.ValidationStatus;

/**
 * XML validation context holding validation specific information needed for XML
 * message validation.
 */
public class XmlMessageValidationContext extends DefaultMessageValidationContext {

    /** Optional delegate acting as a parent context that should be updated with this context */
    private final MessageValidationContext delegate;

    /** Namespace definitions resolving namespaces in XML message validation */
    private final Map<String, String> namespaces;

    /** Map holding control namespaces for validation */
    private final Map<String, String> controlNamespaces;

    /**
     * Default constructor.
     */
    public XmlMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public XmlMessageValidationContext(XmlValidationContextBuilder<?, ?> builder) {
        super(builder);
        this.delegate = builder.delegate;
        this.namespaces = builder.namespaces;
        this.controlNamespaces = builder.controlNamespaces;
    }

    @Override
    public void updateStatus(ValidationStatus status) {
        super.updateStatus(status);

        if (delegate != null) {
            delegate.updateStatus(status);
        }
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

        /**
         * Adapt the given message validation context to the Xml message validation context.
         * @param messageValidationContext the source
         * @return a new instance of Xml message validation context that holds all values from the given message validation context.
         */
        public static Builder adapt(MessageValidationContext.Builder<?, ?> messageValidationContext) {
            return adapt(messageValidationContext.build());
        }

        /**
         * Adapt the given message validation context to the Xml message validation context.
         * @param messageValidationContext the source
         * @return a new instance of Xml message validation context that holds all values from the given message validation context.
         */
        public static Builder adapt(MessageValidationContext messageValidationContext) {
            XmlMessageValidationContext.Builder builder = new XmlMessageValidationContext.Builder();

            builder.ignore(messageValidationContext.getIgnoreExpressions());
            builder.schemaValidation(messageValidationContext.isSchemaValidationEnabled());
            builder.schema(messageValidationContext.getSchema());
            builder.schemaRepository(messageValidationContext.getSchemaRepository());
            builder.delegate(messageValidationContext);

            return builder;
        }

        public XpathMessageValidationContext.Builder expressions() {
            return new XpathMessageValidationContext.Builder();
        }

        public XpathMessageValidationContext.Builder expression(String path, Object expectedValue) {
            return new XpathMessageValidationContext.Builder().expression(path, expectedValue);
        }

        /**
         * Convert to Xpath message validation context builder.
         * @return
         */
        public XpathMessageValidationContext.Builder xpath() {
            return new XpathMessageValidationContext.Builder()
                        .namespaceContext(namespaces)
                        .namespaces(controlNamespaces)
                        .schemaValidation(schemaValidation)
                        .schemaRepository(schemaRepository)
                        .schema(schema)
                        .ignore(ignoreExpressions);
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
            extends MessageValidationContext.Builder<T, S> implements XmlNamespaceAware {

        protected final S self;

        protected MessageValidationContext delegate;

        protected Map<String, String> namespaces = new HashMap<>();
        protected final Map<String, String> controlNamespaces = new HashMap<>();

        protected XmlValidationContextBuilder() {
            this.self = (S) this;
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
         * @param prefix
         * @param namespaceUri
         * @return
         */
        public S namespaceContext(final String prefix, final String namespaceUri) {
            this.namespaces.put(prefix, namespaceUri);
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

        @Override
        public void setNamespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        /**
         * Sets a parent context that is updated with the status as a delegate.
         */
        protected S delegate(MessageValidationContext delegate) {
            this.delegate = delegate;
            return self;
        }
    }

    /**
     * Get the namespace definitions for this validator.
     * @return the namespaceContext
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Get control namespace elements.
     * @return the controlNamespaces
     */
    public Map<String, String> getControlNamespaces() {
        return controlNamespaces;
    }

}
