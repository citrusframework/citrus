/*
 * Copyright 2006-2015 the original author or authors.
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
import java.util.Map;

/**
 * Specialised Xml validation context adds XPath expression evaluation.
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public class XpathXmlMessageValidationContext extends XmlMessageValidationContext {

    /** Map holding xpath expressions as key and expected values as values */
    private Map<String, String> pathValidationExpressions = new HashMap<String, String>();

    /**
     * Get the control message elements that have to be present in
     * the received message. Message element values are compared as well.
     * @return the pathValidationExpressions
     */
    public Map<String, String> getPathValidationExpressions() {
        return pathValidationExpressions;
    }

    /**
     * Set the control message elements explicitly validated XPath expression validation.
     * @param pathValidationExpressions the pathValidationExpressions to set
     */
    public void setPathValidationExpressions(Map<String, String> pathValidationExpressions) {
        this.pathValidationExpressions = pathValidationExpressions;
    }
}
