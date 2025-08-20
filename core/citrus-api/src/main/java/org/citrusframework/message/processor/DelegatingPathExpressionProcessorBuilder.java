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

package org.citrusframework.message.processor;

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.message.MessageProcessor;

public interface DelegatingPathExpressionProcessorBuilder<T extends MessageProcessor, B extends DelegatingPathExpressionProcessorBuilder<T, B>>
        extends MessageProcessor.Builder<T, B>, WithExpressions<B> {

    interface Factory {
        DelegatingPathExpressionProcessorBuilder<?, ?> path();
    }
}
