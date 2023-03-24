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

package org.citrusframework.dsl;

import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;

/**
 * Message Java DSL helper.
 * @author Christoph Deppisch
 */
public class MessageSupport {

    /**
     * Static entrance for all message related Java DSL functionalities.
     * @return
     */
    public static MessageSupport message() {
        return new MessageSupport();
    }

    public MessageHeaderVariableExtractor.Builder headers() {
        return MessageHeaderSupport.fromHeaders();
    }

    public DelegatingPayloadVariableExtractor.Builder body() {
        return MessageBodySupport.fromBody();
    }

    /**
     * Message header Java DSL helper.
     */
    public static final class MessageHeaderSupport {
        /**
         * Static entrance for all message header related Java DSL functionalities.
         * @return
         */
        public static MessageHeaderVariableExtractor.Builder fromHeaders() {
            return MessageHeaderVariableExtractor.Builder.fromHeaders();
        }
    }

    /**
     * Message body Java DSL helper.
     */
    public static final class MessageBodySupport {
        /**
         * Static entrance for all message header related Java DSL functionalities.
         * @return
         */
        public static DelegatingPayloadVariableExtractor.Builder fromBody() {
            return DelegatingPayloadVariableExtractor.Builder.fromBody();
        }
    }
}
