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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.grpc.ServiceDescriptor;
import io.grpc.protobuf.ProtoFileDescriptorSupplier;

/**
 * Descriptor source backed by a generated Protobuf class. Supports generated message classes,
 * generated file (outer) classes and generated gRPC service classes.
 */
public class GeneratedClassSource implements DescriptorSource {

    private final Class<?> type;

    public GeneratedClassSource(Class<?> type) {
        this.type = type;
    }

    @Override
    public List<FileDescriptor> getDescriptors() {
        FileDescriptor file = fileFromServiceClass();
        if (file == null) {
            file = fileFromGeneratedClass();
        }

        if (file == null) {
            throw new DescriptorRegistryException(
                    "Unable to resolve Protobuf descriptors from class '%s' - expected a generated message class, a generated file class or a generated gRPC service class"
                            .formatted(type.getName()));
        }

        return List.of(file);
    }

    private FileDescriptor fileFromServiceClass() {
        Object serviceDescriptor = invokeStatic("getServiceDescriptor");
        if (serviceDescriptor instanceof ServiceDescriptor service
                && service.getSchemaDescriptor() instanceof ProtoFileDescriptorSupplier supplier) {
            return supplier.getFileDescriptor();
        }
        return null;
    }

    private FileDescriptor fileFromGeneratedClass() {
        Object descriptor = invokeStatic("getDescriptor");
        if (descriptor instanceof FileDescriptor file) {
            return file;
        }
        if (descriptor instanceof Descriptor message) {
            return message.getFile();
        }
        return null;
    }

    private Object invokeStatic(String methodName) {
        try {
            Method method = type.getMethod(methodName);
            if (!Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            return method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
