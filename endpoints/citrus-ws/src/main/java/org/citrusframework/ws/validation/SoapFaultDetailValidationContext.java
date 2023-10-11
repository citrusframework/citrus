/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.ws.validation;

import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;

/**
 * Soap fault detail validation context extends XML validation context.
 * @author Christoph Deppisch
 */
public class SoapFaultDetailValidationContext extends XmlMessageValidationContext {

    /**
     * Default constructor.
     */
    public SoapFaultDetailValidationContext() {
        this(Builder.faultDetail());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public SoapFaultDetailValidationContext(Builder builder) {
        super(builder);
    }

    /**
     * Fluent builder.
     */
    public static final class Builder extends XmlValidationContextBuilder<SoapFaultDetailValidationContext, Builder> {

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder faultDetail() {
            return new Builder();
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
        public SoapFaultDetailValidationContext build() {
            return new SoapFaultDetailValidationContext(this);
        }
    }
}
