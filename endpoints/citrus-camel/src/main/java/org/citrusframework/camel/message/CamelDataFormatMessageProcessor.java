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

package org.citrusframework.camel.message;

import org.citrusframework.camel.message.format.DataFormatClauseSupport;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.reifier.dataformat.DataFormatReifier;
import org.apache.camel.support.processor.MarshalProcessor;
import org.apache.camel.support.processor.UnmarshalProcessor;

/**
 * Camel message processor working with data formats to marshal/unmarshal the message body.
 *
 * @author Christoph Deppisch
 */
public class CamelDataFormatMessageProcessor extends CamelMessageProcessor {

    /**
     * Constructor initializing camel context and processor.
     * @param camelContext
     * @param processor
     */
    public CamelDataFormatMessageProcessor(CamelContext camelContext, Processor processor) {
        super(camelContext, processor);
    }

    /**
     * Fluent builder.
     */
    public static class Builder extends CamelMessageProcessorBuilder<CamelDataFormatMessageProcessor, Builder> {

        private DataFormatClauseSupport<Builder> dataFormat;
        private DataFormatClause.Operation operation;

        public static DataFormatClauseSupport<Builder> marshal() {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Marshal;
            builder.dataFormat = new DataFormatClauseSupport<>(builder, builder.operation);
            return builder.dataFormat;
        }

        public static DataFormatClauseSupport<Builder> unmarshal() {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Unmarshal;
            builder.dataFormat = new DataFormatClauseSupport<>(builder, builder.operation);
            return builder.dataFormat;
        }

        @Override
        public CamelDataFormatMessageProcessor doBuild() {
            if (dataFormat.getCamelContext() != null) {
                camelContext = dataFormat.getCamelContext();
            }

            Processor processor;
            if (operation.equals(DataFormatClause.Operation.Marshal)) {
                processor = new MarshalProcessor(DataFormatReifier.getDataFormat(camelContext, dataFormat.getDataFormat()));
            } else {
                processor = new UnmarshalProcessor(DataFormatReifier.getDataFormat(camelContext, dataFormat.getDataFormat()), dataFormat.isAllowNullBody());
            }

            return new CamelDataFormatMessageProcessor(camelContext, processor);
        }
    }
}
