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
import org.apache.camel.Expression;
import org.apache.camel.ExpressionFactory;
import org.apache.camel.builder.DataFormatClause;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.processor.camel.CamelExpressionClause;
import org.citrusframework.message.processor.camel.CamelMessageProcessors;

public class CamelMessageProcessorSupport implements CamelMessageProcessors {

    private CamelContext camelContext;
    private MessageProcessor.Builder<?, ?> delegate;

    @Override
    public CamelMessageProcessorSupport camelContext(Object camelContext) {
        if (camelContext instanceof CamelContext context) {
            return camelContext(context);
        } else  {
            throw new CitrusRuntimeException("Invalid Camel context type: " + camelContext.getClass().getName());
        }
    }

    public CamelMessageProcessorSupport camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    @Override
    public CamelMessageProcessor.Builder process() {
        CamelMessageProcessor.Builder builder = new CamelMessageProcessor.Builder()
                .camelContext(camelContext);
        delegate = builder;
        return builder;
    }

    @Override
    public CamelRouteProcessor.Builder route() {
        CamelRouteProcessor.Builder builder = new CamelRouteProcessor.Builder()
                .camelContext(camelContext);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelDataFormatClauseSupport<InlineProcessDefinition> marshal() {
        CamelDataFormatMessageProcessor.Builder builder = new CamelDataFormatMessageProcessor.Builder()
                .camelContext(camelContext)
                .operation(DataFormatClause.Operation.Marshal);
        this.delegate = builder;
        return builder.getDataFormatClause();
    }

    @Override
    public CamelDataFormatClauseSupport<InlineProcessDefinition> unmarshal() {
        CamelDataFormatMessageProcessor.Builder builder = new CamelDataFormatMessageProcessor.Builder()
                .camelContext(camelContext)
                .operation(DataFormatClause.Operation.Unmarshal);
        this.delegate = builder;
        return builder.getDataFormatClause();
    }

    @Override
    public CamelExpressionClause<CamelTransformMessageProcessor.Builder, ExpressionFactory, Expression> transform() {
        CamelTransformMessageProcessor.Builder builder = new CamelTransformMessageProcessor.Builder()
                .camelContext(camelContext);
        this.delegate = builder;
        return builder.getExpression();
    }

    @Override
    public CamelTransformMessageProcessor.Builder convertBodyTo(Class<?> type) {
        CamelTransformMessageProcessor.Builder builder = new CamelTransformMessageProcessor.Builder()
                .camelContext(camelContext);
        this.delegate = builder;
        return builder.getExpression()
                .body(type);
    }

    @Override
    public MessageProcessor build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Camel message processor delegate");
        }

        if (camelContext != null && delegate instanceof CamelMessageProcessors camelMessageProcessor) {
            camelMessageProcessor.camelContext(camelContext);
        }

        return delegate.build();
    }
}
