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

package org.citrusframework.message.processor.xml;

import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;

public interface XmlMarshallingValidationProcessorBuilder<M, T extends MessageProcessor, B extends XmlMarshallingValidationProcessorBuilder<M, T, B>>
        extends MessageProcessor.Builder<T, B> {

    B unmarshaller(Object unmarshaller);

    /**
     * Sets the bean reference resolver for using endpoint names.
     */
    B withReferenceResolver(ReferenceResolver referenceResolver);
}
