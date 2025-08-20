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
import org.apache.camel.processor.TransformProcessor;
import org.citrusframework.message.processor.camel.CamelExpressionClause;
import org.citrusframework.message.processor.camel.CamelTransformMessageProcessorBuilder;

/**
 * Camel message processor performs transformation on message headers and body.
 *
 */
public class CamelTransformMessageProcessor extends CamelMessageProcessor {

    /**
     * Constructor initializing camel context and processor.
     * @param camelContext
     * @param processor
     */
    public CamelTransformMessageProcessor(CamelContext camelContext, TransformProcessor processor) {
        super(camelContext, processor);
    }

    /**
     * Fluent builder.
     */
    public static class Builder extends CamelMessageProcessorBuilder<CamelTransformMessageProcessor, Builder>
            implements CamelTransformMessageProcessorBuilder<CamelTransformMessageProcessor, Builder, ExpressionFactory, Expression> {

        private final CamelExpressionClause<Builder, ExpressionFactory, Expression> expression;

        public Builder() {
            expression = new CamelExpressionClauseSupport<>(this);
        }

        public Builder(CamelContext camelContext) {
            expression = new CamelExpressionClauseSupport<>(this);
            camelContext(camelContext);
        }

        public static CamelExpressionClause<Builder, ExpressionFactory, Expression> transform() {
            return new Builder().getExpression();
        }

        public static CamelExpressionClause<Builder, ExpressionFactory, Expression> transform(CamelContext camelContext) {
            return new Builder(camelContext).getExpression();
        }

        @Override
        public CamelExpressionClause<Builder, ExpressionFactory, Expression> getExpression() {
            return expression;
        }

        @Override
        public CamelTransformMessageProcessor doBuild() {
            TransformProcessor processor;
            if (expression.getExpressionType() != null) {
                processor = new TransformProcessor(expression.getExpressionType().createExpression(camelContext));
            } else {
                processor = new TransformProcessor(expression.getExpressionValue());
            }

            return new CamelTransformMessageProcessor(camelContext, processor);
        }
    }
}
