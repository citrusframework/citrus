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

package com.consol.citrus.camel.dsl;

import com.consol.citrus.camel.message.CamelDataFormatMessageProcessor;
import com.consol.citrus.camel.message.CamelMessageProcessor;
import com.consol.citrus.camel.message.CamelTransformMessageProcessor;
import com.consol.citrus.camel.message.format.DataFormatClauseSupport;
import org.apache.camel.Processor;
import org.apache.camel.builder.ExpressionClauseSupport;

/**
 * Support class combining all available Apache Camel Java DSL capabilities.
 * @author Christoph Deppisch
 */
public class CamelSupport {

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public static CamelSupport camel() {
        return new CamelSupport();
    }

    /**
     * Message processor delegating to given Apache Camel processor.
     * @param processor
     * @return
     */
    public CamelMessageProcessor.Builder process(Processor processor) {
        return CamelMessageProcessor.Builder.process(processor);
    }

    /**
     * Message processor transforming message with given expression.
     * @return
     */
    public ExpressionClauseSupport<CamelTransformMessageProcessor.Builder> transform() {
        return CamelTransformMessageProcessor.Builder.transform();
    }

    /**
     * Message processor marshalling message body with given data format.
     * @return
     */
    public DataFormatClauseSupport<CamelDataFormatMessageProcessor.Builder> marshal() {
        return CamelDataFormatMessageProcessor.Builder.marshal();
    }

    /**
     * Message processor unmarshalling message body with given data format.
     * @return
     */
    public DataFormatClauseSupport<CamelDataFormatMessageProcessor.Builder> unmarshal() {
        return CamelDataFormatMessageProcessor.Builder.unmarshal();
    }
}
