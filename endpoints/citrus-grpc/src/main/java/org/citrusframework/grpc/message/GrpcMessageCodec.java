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

package org.citrusframework.grpc.message;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;

/**
 * Converts message bodies between their JSON representation and Protobuf messages.
 *
 * <p>On the send side a JSON string or a generated Protobuf message is converted into a
 * {@link DynamicMessage} of the method's request type. On the receive side message bytes are
 * parsed into a {@link DynamicMessage} and printed as JSON, by default with every field present
 * (including fields with default values) and enums by name.</p>
 *
 * @since 5.1
 */
public class GrpcMessageCodec {

    private final GrpcDescriptorRegistry registry;
    private final boolean omitDefaultValues;
    private final JsonFormat.Parser parser;
    private final JsonFormat.Printer printer;

    /**
     * Creates a codec that prints all fields including default values.
     *
     * @param registry the descriptor registry used to resolve message types and {@code Any} payloads
     */
    public GrpcMessageCodec(GrpcDescriptorRegistry registry) {
        this(registry, false);
    }

    /**
     * Creates a codec.
     *
     * @param registry the descriptor registry used to resolve message types and {@code Any} payloads
     * @param omitDefaultValues whether fields with default values are omitted when printing
     */
    public GrpcMessageCodec(GrpcDescriptorRegistry registry, boolean omitDefaultValues) {
        this.registry = registry;
        this.omitDefaultValues = omitDefaultValues;
        this.parser = JsonFormat.parser().usingTypeRegistry(registry.typeRegistry());

        JsonFormat.Printer jsonPrinter = JsonFormat.printer()
                .usingTypeRegistry(registry.typeRegistry());

        if (!omitDefaultValues) {
            jsonPrinter = jsonPrinter.alwaysPrintFieldsWithNoPresence();
        }

        this.printer = jsonPrinter;
    }

    /**
     * Converts the given message body into a {@link DynamicMessage} of the given type.
     *
     * @param type the expected Protobuf message type
     * @param body the message body — a JSON string or a Protobuf message of the given type
     * @return the converted message
     */
    public DynamicMessage encode(Descriptor type, Object body) {
        if (body instanceof Message message) {
            if (!message.getDescriptorForType().equals(type)) {
                throw new GrpcMessageCodecException(String.format(
                        "Message body is of type '%s' but '%s' was expected",
                        message.getDescriptorForType().getFullName(), type.getFullName()));
            }

            try {
                return DynamicMessage.parseFrom(type, message.toByteArray());
            } catch (InvalidProtocolBufferException e) {
                throw new GrpcMessageCodecException(String.format(
                        "Failed to read message body of type '%s': %s", type.getFullName(), e.getMessage()), e);
            }
        }

        DynamicMessage.Builder builder = DynamicMessage.newBuilder(type);

        try {
            parser.merge(String.valueOf(body), builder);
        } catch (InvalidProtocolBufferException e) {
            throw new GrpcMessageCodecException(String.format(
                    "Failed to parse JSON message body for type '%s': %s", type.getFullName(), e.getMessage()), e);
        }

        return builder.build();
    }

    /**
     * Converts the given message bytes into their JSON representation.
     *
     * @param type the Protobuf message type
     * @param body the message bytes
     * @return the JSON representation of the message
     */
    public String decode(Descriptor type, byte[] body) {
        try {
            DynamicMessage message = DynamicMessage.parseFrom(type, body);
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new GrpcMessageCodecException(String.format(
                    "Failed to read Protobuf message of type '%s': %s", type.getFullName(), e.getMessage()), e);
        }
    }

    /**
     * Converts the given message into its JSON representation.
     *
     * @param message the message
     * @return the JSON representation of the message
     */
    public String decode(Message message) {
        try {
            return printer.print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new GrpcMessageCodecException(String.format(
                    "Failed to read Protobuf message of type '%s': %s",
                    message.getDescriptorForType().getFullName(), e.getMessage()), e);
        }
    }
}
