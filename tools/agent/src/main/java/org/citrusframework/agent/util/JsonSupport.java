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

package org.citrusframework.agent.util;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.citrusframework.TestSource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;

public final class JsonSupport {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .addModule(new AgentModule())
                .build()
                .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));

        OBJECT_MAPPER.setSerializerFactory(OBJECT_MAPPER.getSerializerFactory()
                .withAdditionalSerializers(new SimpleSerializers(List.of(new DurationSerializer(), new ThrowableSerializer()))));
    }

    private JsonSupport() {
        // prevent instantiation of utility class
    }

    public static ObjectMapper json() {
        return OBJECT_MAPPER;
    }

    public static <T> T read(String body, Class<T> bodyType) {
        try {
            return json().readValue(body, bodyType);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read json body", e);
        }
    }

    public static String render(Object model) {
        try {
            return json().writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new CitrusRuntimeException("Failed to write json test results", e);
        }
    }

    private static class DurationSerializer extends StdSerializer<Duration> {

        protected DurationSerializer() {
            super(Duration.class);
        }

        @Override
        public void serialize(Duration duration, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            long milliseconds = duration.toMillis();
            jsonGenerator.writeNumber(milliseconds);
        }
    }

    private static class ThrowableSerializer extends StdSerializer<Throwable> {

        protected ThrowableSerializer() {
            super(Throwable.class);
        }

        @Override
        public void serialize(Throwable throwable, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString("%s - %s".formatted(throwable.getClass().getName(), throwable.getMessage()));
        }
    }

    private static class AgentModule extends SimpleModule {

        public AgentModule() {
            addDeserializer(TestSource.class, new JsonDeserializer<>() {
                @Override
                public TestSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    ObjectNode node = p.readValueAsTree();
                    String filePath = Optional.ofNullable(node.get("filePath"))
                            .map(JsonNode::textValue)
                            .orElseThrow(() -> new CitrusRuntimeException("Missing test source file path"));
                    String type = Optional.ofNullable(node.get("type"))
                            .map(JsonNode::textValue)
                            .orElseGet(() -> FileUtils.getFileExtension(filePath));
                    String name = Optional.ofNullable(node.get("name"))
                            .map(JsonNode::textValue)
                            .orElseGet(() -> FileUtils.getBaseName(FileUtils.getFileName(filePath)));

                    TestSource source = new TestSource(type, name, filePath);

                    Resource sourceFile = Resources.fromClasspath(filePath);
                    if (sourceFile.exists()) {
                        source.setSourceFile(sourceFile);
                    }
                    return source;
                }
            });
        }
    }
}
