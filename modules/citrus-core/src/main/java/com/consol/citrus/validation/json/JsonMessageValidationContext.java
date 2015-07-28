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

package com.consol.citrus.validation.json;

import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.ControlMessageValidationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Validation context holding JSON specific validation information.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonMessageValidationContext extends ControlMessageValidationContext {

    /** Map holding xpath expressions to identify the ignored message elements */
    private Set<String> ignoreExpressions = new HashSet<String>();

    /**
     * Default constructor using message type field.
     */
    public JsonMessageValidationContext() {
        super(MessageType.JSON.toString());
    }

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    /**
     * Set ignored message elements.
     * @param ignoreExpressions the ignoreExpressions to set
     */
    public void setIgnoreExpressions(Set<String> ignoreExpressions) {
        this.ignoreExpressions = ignoreExpressions;
    }

}
