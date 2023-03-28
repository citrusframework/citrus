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

import org.apache.camel.CamelContext;
import org.apache.camel.builder.ExpressionClauseSupport;
import org.apache.camel.processor.TransformProcessor;

/**
 * Camel message processor performs transformation on message headers and body.
 *
 * @author Christoph Deppisch
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
    public static class Builder extends CamelMessageProcessorBuilder<CamelTransformMessageProcessor, Builder> {

        private ExpressionClauseSupport<Builder> expression;

        public static ExpressionClauseSupport<Builder> transform() {
            Builder builder = new Builder();
            builder.expression = new ExpressionClauseSupport<>(builder);
            return builder.expression;
        }

        public static ExpressionClauseSupport<Builder> transform(CamelContext camelContext) {
            Builder builder = new Builder();
            builder.expression = new ExpressionClauseSupport<>(builder);
            builder.camelContext(camelContext);
            return builder.expression;
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
