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
import org.apache.camel.model.MarshalDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.UnmarshalDefinition;
import org.apache.camel.reifier.dataformat.DataFormatReifier;
import org.apache.camel.support.processor.MarshalProcessor;
import org.apache.camel.support.processor.UnmarshalProcessor;
import org.citrusframework.camel.dsl.CamelContextAware;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

/**
 * Camel message processor working with data formats to marshal/unmarshal the message body.
 *
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

        private final InlineProcessDefinition processDefinition = new InlineProcessDefinition();
        private DataFormatClause.Operation operation;

        private DataFormatDefinition dataFormat;
        private boolean allowNullBody;

        public static DataFormatClause<InlineProcessDefinition> marshal() {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Marshal;
            return new DataFormatClause<>(builder.processDefinition, builder.operation);
        }

        public static DataFormatClause<InlineProcessDefinition> unmarshal() {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Unmarshal;
            return new DataFormatClause<>(builder.processDefinition, builder.operation);
        }

        public static DataFormatClause<InlineProcessDefinition> marshal(CamelContext camelContext) {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Marshal;
            builder.camelContext(camelContext);
            return new DataFormatClause<>(builder.processDefinition, builder.operation);
        }

        public static DataFormatClause<InlineProcessDefinition> unmarshal(CamelContext camelContext) {
            Builder builder = new Builder();
            builder.operation = DataFormatClause.Operation.Unmarshal;
            builder.camelContext(camelContext);
            return new DataFormatClause<>(builder.processDefinition, builder.operation);
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

        public class InlineProcessDefinition extends ProcessDefinition implements
                MessageProcessor.Builder<CamelDataFormatMessageProcessor, Builder>, ReferenceResolverAware, CamelContextAware<Builder> {

            @Override
            public void addOutput(ProcessorDefinition<?> output) {
                if (output instanceof MarshalDefinition marshalDefinition) {
                    dataFormat = marshalDefinition.getDataFormatType();
                }

                if (output instanceof UnmarshalDefinition unmarshalDefinition) {
                    dataFormat = unmarshalDefinition.getDataFormatType();

                    if (StringUtils.hasText(unmarshalDefinition.getAllowNullBody())) {
                        allowNullBody = Boolean.parseBoolean(unmarshalDefinition.getAllowNullBody());
                    }
                }
            }

            @Override
            public CamelDataFormatMessageProcessor build() {
                return Builder.this.build();
            }

            @Override
            public void setReferenceResolver(ReferenceResolver referenceResolver) {
                Builder.this.referenceResolver = referenceResolver;
            }

            @Override
            public Builder camelContext(CamelContext camelContext) {
                Builder.this.camelContext = camelContext;
                return Builder.this;
            }
        }
    }
}
