/*
 * Copyright 2020 the original author or authors.
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

package com.consol.citrus.dsl;

import com.consol.citrus.validation.xml.XmlMarshallingValidationProcessor;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageProcessor;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class XmlSupport {

    /**
     * Static entrance for all Xml related Java DSL functionalities.
     * @return
     */
    public static XmlSupport xml() {
        return new XmlSupport();
    }

    public XpathSupport xpath() {
        return new XpathSupport();
    }

    public XpathPayloadVariableExtractor.Builder extract() {
        return new XpathPayloadVariableExtractor.Builder();
    }

    public XmlMessageValidationContext.Builder validate() {
        return new XmlMessageValidationContext.Builder();
    }

    public <T> XmlMarshallingValidationProcessor.Builder<T> validate(Class<T> type) {
        return XmlMarshallingValidationProcessor.Builder.validate(type);
    }

    public static final class XpathSupport {
        /**
         * Static entrance for all Xpath related Java DSL functionalities.
         * @return
         */
        public static XpathSupport xpath() {
            return new XpathSupport();
        }

        public XpathMessageProcessor.Builder process() {
            return new XpathMessageProcessor.Builder();
        }

        public XpathPayloadVariableExtractor.Builder extract() {
            return new XpathPayloadVariableExtractor.Builder();
        }

        public XpathMessageValidationContext.Builder validate() {
            return new XpathMessageValidationContext.Builder();
        }
    }
}
