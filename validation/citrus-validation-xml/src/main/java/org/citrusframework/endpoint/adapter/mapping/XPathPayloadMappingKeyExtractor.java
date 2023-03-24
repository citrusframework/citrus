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

package org.citrusframework.endpoint.adapter.mapping;

import java.util.Collections;

import org.citrusframework.message.Message;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.xpath.XPathUtils;

/**
 * Extracts predicate from message payload via XPath expression evaluation.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class XPathPayloadMappingKeyExtractor extends AbstractMappingKeyExtractor {

    /** XPath expression evaluated on message payload */
    private String xpathExpression = "local-name(/*)";

    /** Namespace context builder for XPath expression evaluation */
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    @Override
    public String getMappingKey(Message request) {
        return XPathUtils.evaluateAsString(
                XMLUtils.parseMessagePayload(request.getPayload(String.class)),
                xpathExpression,
                namespaceContextBuilder.buildContext(request, Collections.emptyMap()));
    }

    /**
     * Sets the xpath expression to evaluate.
     * @param xpathExpression
     */
    public void setXpathExpression(String xpathExpression) {
        this.xpathExpression = xpathExpression;
    }

    /**
     * Sets the namespace context builder for this extractor.
     * @param namespaceContextBuilder
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }

}
