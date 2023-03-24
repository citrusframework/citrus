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

package org.citrusframework.dsl;

import org.citrusframework.validation.GenericValidationProcessor;
import org.citrusframework.validation.xml.XmlMarshallingValidationProcessor;
import org.citrusframework.validation.xml.XmlMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class XmlSupport {

    /**
     * Entrance for all Xml related validation functionalities.
     * @return
     */
    public static XmlMessageValidationContext.Builder xml() {
        return XmlMessageValidationContext.Builder.xml();
    }

    /**
     * Marshalling validation processor builder entrance.
     * @param validationProcessor
     * @param <T>
     * @return
     */
    public static <T> XmlMarshallingValidationProcessor.Builder<T> validate(GenericValidationProcessor<T> validationProcessor) {
        return XmlMarshallingValidationProcessor.Builder.validate(validationProcessor);
    }
}
