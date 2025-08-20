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

package org.citrusframework.camel.message;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.reifier.dataformat.DataFormatReifier;
import org.apache.camel.support.processor.MarshalProcessor;
import org.apache.camel.support.processor.UnmarshalProcessor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.processor.camel.CamelDataFormatMessageProcessorBuilder;

/**
 * Camel message processor working with data formats to marshal/unmarshal the message body.
 *
 */
public class CamelDataFormatMessageProcessor extends CamelMessageProcessor {

    /**
     * Constructor initializing camel context and processor.
     */
    public CamelDataFormatMessageProcessor(CamelContext camelContext, Processor processor) {
        super(camelContext, processor);
    }

    /**
     * Fluent builder.
     */
    public static class Builder extends CamelMessageProcessorBuilder<CamelDataFormatMessageProcessor, Builder>
            implements CamelDataFormatMessageProcessorBuilder<CamelDataFormatMessageProcessor, Builder> {

        private final InlineProcessDefinition processDefinition = new InlineProcessDefinition(this);
        private DataFormatClause.Operation operation;

        private DataFormatDefinition dataFormat;
        private boolean allowNullBody;

        public static CamelDataFormatClauseSupport<InlineProcessDefinition> marshal() {
            return new Builder()
                    .operation(DataFormatClause.Operation.Marshal)
                    .getDataFormatClause();
        }

        public static CamelDataFormatClauseSupport<InlineProcessDefinition> unmarshal() {
            return new Builder()
                    .operation(DataFormatClause.Operation.Unmarshal)
                    .getDataFormatClause();
        }

        public static CamelDataFormatClauseSupport<InlineProcessDefinition> marshal(CamelContext camelContext) {
            return new Builder()
                    .operation(DataFormatClause.Operation.Marshal)
                    .camelContext(camelContext)
                    .getDataFormatClause();
        }

        public static CamelDataFormatClauseSupport<InlineProcessDefinition> unmarshal(CamelContext camelContext) {
            return new Builder()
                    .operation(DataFormatClause.Operation.Unmarshal)
                    .camelContext(camelContext)
                    .getDataFormatClause();
        }

        @Override
        public Builder operation(String operation) {
            return operation(DataFormatClause.Operation.valueOf(operation));
        }

        public Builder operation(DataFormatClause.Operation operation) {
            this.operation = operation;
            return this;
        }

        @Override
        public Builder dataFormat(Object dataFormat) {
            if (dataFormat instanceof DataFormatDefinition dataFormatDefinition) {
                return dataFormat(dataFormatDefinition);
            } else {
                throw new CitrusRuntimeException("Invalid data format definition type: %s".formatted(dataFormat.getClass().getName()));
            }
        }

        public Builder dataFormat(DataFormatDefinition dataFormat) {
            this.dataFormat = dataFormat;
            return this;
        }

        @Override
        public Builder allowNullBody(boolean allowNullBody) {
            this.allowNullBody = allowNullBody;
            return this;
        }

        @Override
        public CamelDataFormatClauseSupport<InlineProcessDefinition> getDataFormatClause() {
            return new CamelDataFormatClauseSupport<>(processDefinition, operation);
        }

        @Override
        public CamelDataFormatMessageProcessor doBuild() {
            if (processDefinition.getCamelContext() != null) {
                camelContext = processDefinition.getCamelContext();
            }

            Processor processor;
            if (operation.equals(DataFormatClause.Operation.Marshal)) {
                processor = new MarshalProcessor(DataFormatReifier.getDataFormat(camelContext, dataFormat));
            } else {
                processor = new UnmarshalProcessor(DataFormatReifier.getDataFormat(camelContext, dataFormat), allowNullBody);
            }

            return new CamelDataFormatMessageProcessor(camelContext, processor);
        }

    }

}
