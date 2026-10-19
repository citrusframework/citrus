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

import java.util.List;

import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Timestamp;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GrpcMessageCodecTest {

    private static final String DESCRIPTOR_SET = "classpath:descriptors/test-protos.binpb";

    private GrpcDescriptorRegistry registry;

    @BeforeClass
    public void setUpRegistry() {
        registry = new GrpcDescriptorRegistry(List.of(DESCRIPTOR_SET));
    }

    @Test
    public void shouldEncodeJsonBody() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("helloworld.HelloRequest");

        DynamicMessage message = codec.encode(type, "{ \"name\": \"Ada\" }");

        assertThat(message.getField(type.findFieldByName("name"))).isEqualTo("Ada");
    }

    @Test
    public void shouldAcceptOriginalProtoFieldNames() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("helloworld.HelloRequest");

        DynamicMessage message = codec.encode(type, "{ \"user_id\": 7 }");

        assertThat(message.getField(type.findFieldByName("user_id"))).isEqualTo(7);
    }

    @Test
    public void shouldEncodeGeneratedMessageBody() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("helloworld.HelloRequest");
        DynamicMessage body = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("name"), "Ada")
                .build();

        DynamicMessage message = codec.encode(type, body);

        assertThat(message.getField(type.findFieldByName("name"))).isEqualTo("Ada");
    }

    @Test
    public void shouldRejectUnknownField() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("helloworld.HelloRequest");

        assertThatThrownBy(() -> codec.encode(type, "{ \"nmae\": \"Ada\" }"))
                .isInstanceOf(GrpcMessageCodecException.class)
                .hasMessageContaining("nmae");
    }

    @Test
    public void shouldRejectMessageOfWrongType() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor requestType = registry.findMessage("helloworld.HelloRequest");
        Descriptor replyType = registry.findMessage("helloworld.HelloReply");
        DynamicMessage reply = DynamicMessage.newBuilder(replyType).build();

        assertThatThrownBy(() -> codec.encode(requestType, reply))
                .isInstanceOf(GrpcMessageCodecException.class)
                .hasMessageContaining("helloworld.HelloRequest")
                .hasMessageContaining("helloworld.HelloReply");
    }

    @Test
    public void shouldPrintZeroValuesByDefault() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("feat.Counter");
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("count"), 0)
                .build();

        assertThat(codec.decode(type, message.toByteArray())).contains("\"count\": 0");
    }

    @Test
    public void shouldOmitDefaultValuesWhenConfigured() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry, true);
        Descriptor type = registry.findMessage("feat.Counter");
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("count"), 0)
                .build();

        assertThat(codec.decode(type, message.toByteArray()).replaceAll("\\s", "")).isEqualTo("{}");
    }

    @Test
    public void shouldPrintDeclaredJsonName() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("feat.Feature");
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("renamed"), "x")
                .build();

        assertThat(codec.decode(type, message.toByteArray())).contains("\"otherName\": \"x\"");
    }

    @Test
    public void shouldPrintEnumsByName() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("feat.Feature");
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("kind"),
                        type.findFieldByName("kind").getEnumType().findValueByName("KIND_BASIC"))
                .build();

        assertThat(codec.decode(type, message.toByteArray())).contains("\"KIND_BASIC\"");
    }

    @Test
    public void shouldPrintAnyWithTypeRegistry() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("feat.Feature");
        Descriptor requestType = registry.findMessage("helloworld.HelloRequest");
        Any details = Any.pack(DynamicMessage.newBuilder(requestType)
                .setField(requestType.findFieldByName("name"), "Ada")
                .build());
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("details"), details)
                .build();

        String json = codec.decode(type, message.toByteArray());

        assertThat(json).contains("type.googleapis.com/helloworld.HelloRequest");
        assertThat(json).contains("\"name\": \"Ada\"");
    }

    @Test
    public void shouldPrintWellKnownTypes() {
        GrpcMessageCodec codec = new GrpcMessageCodec(registry);
        Descriptor type = registry.findMessage("market.Price");
        DynamicMessage message = DynamicMessage.newBuilder(type)
                .setField(type.findFieldByName("as_of"),
                        Timestamp.newBuilder().setSeconds(0).build())
                .build();

        assertThat(codec.decode(type, message.toByteArray())).contains("1970-01-01T00:00:00Z");
    }
}
