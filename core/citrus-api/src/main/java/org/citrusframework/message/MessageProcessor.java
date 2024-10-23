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

package org.citrusframework.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Processor performs operations on the given message. The processor is able to change message content such as payload and headers.
 */
@FunctionalInterface
public interface MessageProcessor extends MessageTransformer {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    /** Message processor resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/message/processor";

    /** Type resolver to find custom message processors on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves processor from resource path lookup with given processor resource name. Scans classpath for processor meta information
     * with given name and returns instance of processor. Returns optional instead of throwing exception when no processor
     * could be found.
     * @param processor
     * @return
     */
    static <T extends MessageProcessor, B extends Builder<T, B>> Optional<Builder<T, B>> lookup(String processor) {
        try {
            Builder<T, B> instance = TYPE_RESOLVER.resolve(processor);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn("Failed to resolve message processor from resource '{}/{}'", RESOURCE_PATH, processor);
        }

        return Optional.empty();
    }

    /**
     * Process message with given test context. Processors can change the message payload and headers.
     * @param message the message to process.
     * @param context the current test context.
     * @return the processed message.
     */
    void process(Message message, TestContext context);

    /**
     * Adapt to message transformer API.
     * @param message the message to process.
     * @param context the current test context.
     * @return
     */
    default Message transform(Message message, TestContext context) {
        process(message, context);
        return message;
    }

    /**
     * Fluent builder
     * @param <T> processor type
     * @param <B> builder reference to self
     */
    interface Builder<T extends MessageProcessor, B extends Builder<T, B>> {

        /**
         * Builds new message processor instance.
         * @return the built processor.
         */
        T build();
    }
}
