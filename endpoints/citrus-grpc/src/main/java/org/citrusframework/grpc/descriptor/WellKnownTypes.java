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

package org.citrusframework.grpc.descriptor;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.protobuf.Any;
import com.google.protobuf.Api;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Int32Value;
import com.google.protobuf.SourceContext;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Type;

/**
 * Google well-known Protobuf types as provided by the Protobuf runtime. Descriptor sources reuse
 * these instances instead of building their own copies.
 */
final class WellKnownTypes {

    private WellKnownTypes() {
    }

    static Map<String, FileDescriptor> all() {
        Map<String, FileDescriptor> types = new LinkedHashMap<>();
        types.put("google/protobuf/any.proto", Any.getDescriptor().getFile());
        types.put("google/protobuf/api.proto", Api.getDescriptor().getFile());
        types.put("google/protobuf/descriptor.proto", DescriptorProtos.getDescriptor());
        types.put("google/protobuf/duration.proto", Duration.getDescriptor().getFile());
        types.put("google/protobuf/empty.proto", Empty.getDescriptor().getFile());
        types.put("google/protobuf/field_mask.proto", FieldMask.getDescriptor().getFile());
        types.put("google/protobuf/source_context.proto", SourceContext.getDescriptor().getFile());
        types.put("google/protobuf/struct.proto", Struct.getDescriptor().getFile());
        types.put("google/protobuf/timestamp.proto", Timestamp.getDescriptor().getFile());
        types.put("google/protobuf/type.proto", Type.getDescriptor().getFile());
        types.put("google/protobuf/wrappers.proto", Int32Value.getDescriptor().getFile());
        return types;
    }

    /**
     * Protobuf files that are provided by the runtime and must not be loaded from source.
     */
    static boolean isRuntimeProto(String name) {
        return name.startsWith("google/protobuf/") || name.startsWith("wire/");
    }
}
