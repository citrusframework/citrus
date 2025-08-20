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
import org.apache.camel.model.MarshalDefinition;
import org.apache.camel.model.ProcessDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.UnmarshalDefinition;
import org.citrusframework.camel.dsl.CamelContextAware;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;

/**
 * Special process definition implementation combines Citrus message processor builder with Camel processor definition.
 * Configures given message processor builder delegate with data format from Camel processor definition.
 * Delegates message processor builder API calls to a given delegate.
 */
public class InlineProcessDefinition extends ProcessDefinition implements
        MessageProcessor.Builder<CamelDataFormatMessageProcessor, InlineProcessDefinition>,
        ReferenceResolverAware, CamelContextAware<InlineProcessDefinition> {

    private final CamelDataFormatMessageProcessor.Builder delegate;

    public InlineProcessDefinition(CamelDataFormatMessageProcessor.Builder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addOutput(ProcessorDefinition<?> output) {
        if (output instanceof MarshalDefinition marshalDefinition) {
            delegate.dataFormat(marshalDefinition.getDataFormatType());
        }

        if (output instanceof UnmarshalDefinition unmarshalDefinition) {
            delegate.dataFormat(unmarshalDefinition.getDataFormatType());

            if (StringUtils.hasText(unmarshalDefinition.getAllowNullBody())) {
                delegate.allowNullBody(Boolean.parseBoolean(unmarshalDefinition.getAllowNullBody()));
            }
        }
    }

    @Override
    public CamelDataFormatMessageProcessor build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
    }

    @Override
    public InlineProcessDefinition camelContext(CamelContext camelContext) {
        delegate.camelContext(camelContext);
        return this;
    }
}
