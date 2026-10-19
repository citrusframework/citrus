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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.google.protobuf.TypeRegistry;

/**
 * Registry of Protobuf descriptors used by a gRPC endpoint. Descriptors are loaded lazily on first
 * access, cached and shared by all calls of the endpoint.
 */
public class GrpcDescriptorRegistry {

    private final List<String> descriptorValues;
    private final List<String> protoPaths;

    private volatile DescriptorSnapshot snapshot;

    public GrpcDescriptorRegistry(List<String> descriptorValues) {
        this(descriptorValues, List.of());
    }

    public GrpcDescriptorRegistry(List<String> descriptorValues, List<String> protoPaths) {
        this.descriptorValues = List.copyOf(descriptorValues);
        this.protoPaths = List.copyOf(protoPaths);
    }

    /**
     * Find a method by its full name ({@code package.Service/Method}).
     */
    public MethodDescriptor findMethod(String fullName) {
        MethodDescriptor method = snapshot().methods.get(fullName);
        if (method == null) {
            throw new DescriptorRegistryException(
                    "Unknown gRPC method '%s' - known services are %s"
                            .formatted(fullName, snapshot().serviceNames));
        }
        return method;
    }

    /**
     * Find a message type by its full name ({@code package.Message}).
     */
    public Descriptor findMessage(String fullName) {
        Descriptor message = snapshot().messages.get(fullName);
        if (message == null) {
            throw new DescriptorRegistryException(
                    "Unknown Protobuf message type '%s'".formatted(fullName));
        }
        return message;
    }

    /**
     * All services known to this registry.
     */
    public List<ServiceDescriptor> services() {
        return snapshot().services;
    }

    /**
     * Type registry used to resolve {@code google.protobuf.Any} messages.
     */
    public TypeRegistry typeRegistry() {
        return snapshot().typeRegistry;
    }

    private DescriptorSnapshot snapshot() {
        DescriptorSnapshot current = snapshot;
        if (current == null) {
            synchronized (this) {
                current = snapshot;
                if (current == null) {
                    current = load();
                    snapshot = current;
                }
            }
        }
        return current;
    }

    private DescriptorSnapshot load() {
        Map<String, FileDescriptor> files = new LinkedHashMap<>();
        for (String value : descriptorValues) {
            for (FileDescriptor file : source(value).getDescriptors()) {
                collect(file, files);
            }
        }

        Map<String, MethodDescriptor> methods = new LinkedHashMap<>();
        Map<String, Descriptor> messages = new LinkedHashMap<>();
        List<ServiceDescriptor> services = new ArrayList<>();

        for (FileDescriptor file : files.values()) {
            for (ServiceDescriptor service : file.getServices()) {
                services.add(service);
                for (MethodDescriptor method : service.getMethods()) {
                    methods.put(service.getFullName() + "/" + method.getName(), method);
                }
            }
            for (Descriptor message : file.getMessageTypes()) {
                indexMessages(message, messages);
            }
        }

        TypeRegistry.Builder typeRegistry = TypeRegistry.newBuilder();
        for (FileDescriptor file : files.values()) {
            typeRegistry.add(file.getMessageTypes());
        }

        List<String> serviceNames = services.stream()
                .map(ServiceDescriptor::getFullName)
                .toList();

        return new DescriptorSnapshot(List.copyOf(services), Map.copyOf(methods),
                Map.copyOf(messages), typeRegistry.build(), serviceNames);
    }

    private DescriptorSource source(String value) {
        if (value.endsWith(".desc") || value.endsWith(".pb") || value.endsWith(".binpb")
                || value.endsWith(".protoset")) {
            return new DescriptorSetSource(value);
        }

        try {
            Class<?> type = Class.forName(value, false, classLoader());
            return new GeneratedClassSource(type);
        } catch (ClassNotFoundException e) {
            throw new DescriptorRegistryException(
                    "Unable to resolve Protobuf descriptor source '%s' - expected a .proto file, a descriptor set file (.desc, .pb, .binpb, .protoset) or the name of a generated Protobuf class"
                            .formatted(value));
        }
    }

    private ClassLoader classLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Optional.ofNullable(classLoader).orElseGet(GrpcDescriptorRegistry.class::getClassLoader);
    }

    private static void collect(FileDescriptor file, Map<String, FileDescriptor> files) {
        if (files.putIfAbsent(file.getName(), file) != null) {
            return;
        }
        for (FileDescriptor dependency : file.getDependencies()) {
            collect(dependency, files);
        }
    }

    private static void indexMessages(Descriptor message, Map<String, Descriptor> messages) {
        messages.put(message.getFullName(), message);
        for (Descriptor nested : message.getNestedTypes()) {
            indexMessages(nested, messages);
        }
    }

    private record DescriptorSnapshot(List<ServiceDescriptor> services,
                                      Map<String, MethodDescriptor> methods,
                                      Map<String, Descriptor> messages,
                                      TypeRegistry typeRegistry,
                                      List<String> serviceNames) {
    }
}
