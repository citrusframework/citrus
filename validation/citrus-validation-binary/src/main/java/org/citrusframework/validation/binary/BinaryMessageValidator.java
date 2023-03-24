/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation.binary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Message validator compares binary streams. Assumes control
 * message payload is convertable to an input stream so we can compare the stream data with buffer read.
 *
 * @author Christoph Deppisch
 */
public class BinaryMessageValidator extends DefaultMessageValidator {

    private static final int BUFFER_SIZE = 1024;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) throws ValidationException {
        try (InputStream receivedInput = receivedMessage.getPayload(InputStream.class);
             InputStream controlInput = controlMessage.getPayload(InputStream.class)) {

            ReadableByteChannel receivedBytes = Channels.newChannel(receivedInput);
            ReadableByteChannel controlBytes = Channels.newChannel(controlInput);

            ByteBuffer receivedBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            ByteBuffer controlBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            while (true) {
                int n1 = receivedBytes.read(receivedBuffer);
                int n2 = controlBytes.read(controlBuffer);

                if (n1 == -1 || n2 == -1) return;

                receivedBuffer.flip();
                controlBuffer.flip();

                for (int i = 0; i < Math.min(n1, n2); i++) {
                    if (receivedBuffer.get() != controlBuffer.get()) {
                        throw new ValidationException("Received input stream is not equal to given control");
                    }
                }

                receivedBuffer.compact();
                receivedBuffer.compact();
            }

        } catch (IOException e) {
            throw new ValidationException("Failed to compare binary input streams", e);
        }
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.BINARY.toString());
    }
}
