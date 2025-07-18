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

package org.citrusframework.validation.binary;

import java.io.ByteArrayOutputStream;
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
 * message payload is convertable to an input stream, so we can compare the stream data with buffer read.
 */
public class BinaryMessageValidator extends DefaultMessageValidator {

    private static final int BUFFER_SIZE = 1024;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, ValidationContext validationContext) throws ValidationException {
        try (InputStream receivedInput = receivedMessage.getPayload(InputStream.class);
             InputStream controlInput = controlMessage.getPayload(InputStream.class)) {

            logger.debug("Start binary message validation");

            ReadableByteChannel receivedBytes = Channels.newChannel(receivedInput);
            ReadableByteChannel controlBytes = Channels.newChannel(controlInput);

            ByteBuffer receivedBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            ByteBuffer controlBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            ByteArrayOutputStream receivedResult = new ByteArrayOutputStream();
            ByteArrayOutputStream controlResult = new ByteArrayOutputStream();
            while (true) {
                int n1 = receivedBytes.read(receivedBuffer);
                int n2 = controlBytes.read(controlBuffer);

                if (n1 == -1 && n2 == -1) {
                    logger.debug("Binary message validation successful: All values OK");
                    return;
                } else if (n1 == -1) {
                    throw new ValidationException(("Received input stream reached end-of-stream - " +
                            "control input stream is not finished yet"));
                } else if (n2 == -1) {
                    if (controlResult.size() > 0) {
                        throw new ValidationException(
                            ("Control input stream reached end-of-stream - " +
                                "received input stream is not finished yet"));
                    } else {
                        // Binary message validation without a specific control message - skip.
                        return;
                    }
                }

                receivedBuffer.flip();
                controlBuffer.flip();

                for (int i = 0; i < Math.min(n1, n2); i++) {
                    byte received = receivedBuffer.get();
                    byte control = controlBuffer.get();

                    receivedResult.write((char)received);
                    controlResult.write((char)control);

                    if (received != control) {
                        logger.info("Received input stream is not equal - expected '%s', but was '%s'".formatted(controlResult.toString(), receivedResult.toString()));
                        throw new ValidationException(("Received input stream is not equal to given control, " +
                                "expected '%s', but was '%s'").formatted(
                                        controlResult.toString().substring(controlResult.toString().length() - Math.min(25, controlResult.size())),
                                        receivedResult.toString().substring(receivedResult.toString().length() - Math.min(25, receivedResult.size()))));
                    }
                }

                receivedBuffer.compact();
                controlBuffer.compact();
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
