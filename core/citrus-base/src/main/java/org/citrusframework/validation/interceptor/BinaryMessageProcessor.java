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

package org.citrusframework.validation.interceptor;

import java.nio.charset.Charset;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

/**
 * Message construction processor automatically converts message payloads to binary content. Supports String typed message payloads and
 * payload resources.
 *
 */
public class BinaryMessageProcessor extends AbstractMessageProcessor {

    private final Charset encoding;

    public BinaryMessageProcessor() {
        this(Builder.toBinary());
    }

    public BinaryMessageProcessor(Builder builder) {
        this.encoding = builder.encoding;
    }

    @Override
    protected void processMessage(Message message, TestContext context) {
        if (message.getPayload() instanceof String) {
            message.setPayload(message.getPayload(String.class).getBytes(encoding));
        } else if (message.getPayload() instanceof Resource) {
            message.setPayload(FileUtils.copyToByteArray(message.getPayload(Resource.class)));
        } else {
            message.setPayload(message.getPayload(byte[].class));
        }

        message.setType(MessageType.BINARY.name());
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements MessageProcessor.Builder<BinaryMessageProcessor, Builder> {

        private Charset encoding = Charset.forName(CitrusSettings.CITRUS_FILE_ENCODING);

        public static Builder toBinary() {
            return new Builder();
        }

        /**
         * With custom charset encoding identified by its name.
         * @param charsetName
         * @return
         */
        public Builder encoding(String charsetName) {
            return encoding(Charset.forName(charsetName));
        }

        /**
         * With custom charset encoding.
         * @param encoding
         * @return
         */
        public Builder encoding(Charset encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public BinaryMessageProcessor build() {
            return new BinaryMessageProcessor(this);
        }
    }
}
