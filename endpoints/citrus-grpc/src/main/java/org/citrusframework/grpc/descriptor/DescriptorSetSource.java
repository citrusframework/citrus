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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

/**
 * Descriptor source backed by a binary {@code FileDescriptorSet} resource. Files are built in
 * dependency order and may import the Google well-known types, which are resolved from the
 * Protobuf runtime.
 */
public class DescriptorSetSource implements DescriptorSource {

    private static final Map<String, FileDescriptor> WELL_KNOWN_TYPES = WellKnownTypes.all();

    private final String resourcePath;

    public DescriptorSetSource(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public List<FileDescriptor> getDescriptors() {
        Resource resource = Resources.create(resourcePath);
        if (!resource.exists()) {
            throw new DescriptorRegistryException(
                    "Unable to find Protobuf descriptor set resource '%s'".formatted(resourcePath));
        }

        FileDescriptorSet descriptorSet;
        try (InputStream inputStream = resource.getInputStream()) {
            if (inputStream == null) {
                throw new IOException("resource is not readable");
            }
            descriptorSet = FileDescriptorSet.parseFrom(inputStream);
        } catch (IOException e) {
            throw new DescriptorRegistryException(
                    "Failed to read Protobuf descriptor set resource '%s'".formatted(resourcePath), e);
        }

        Map<String, FileDescriptor> available = new LinkedHashMap<>(WELL_KNOWN_TYPES);
        List<FileDescriptorProto> pending = new ArrayList<>(descriptorSet.getFileList());
        List<FileDescriptor> files = new ArrayList<>();

        while (!pending.isEmpty()) {
            boolean progress = false;
            Iterator<FileDescriptorProto> iterator = pending.iterator();
            while (iterator.hasNext()) {
                FileDescriptorProto proto = iterator.next();
                if (available.containsKey(proto.getName())) {
                    iterator.remove();
                    progress = true;
                    continue;
                }

                if (!available.keySet().containsAll(proto.getDependencyList())) {
                    continue;
                }

                FileDescriptor[] dependencies = proto.getDependencyList().stream()
                        .map(available::get)
                        .toArray(FileDescriptor[]::new);

                try {
                    FileDescriptor file = FileDescriptor.buildFrom(proto, dependencies);
                    available.put(file.getName(), file);
                    files.add(file);
                    iterator.remove();
                    progress = true;
                } catch (DescriptorValidationException e) {
                    throw new DescriptorRegistryException(
                            "Failed to build Protobuf descriptor '%s' from resource '%s'"
                                    .formatted(proto.getName(), resourcePath), e);
                }
            }

            if (!progress) {
                List<String> unresolved = pending.stream()
                        .map(FileDescriptorProto::getName)
                        .toList();
                throw new DescriptorRegistryException(
                        "Unable to resolve imports of %s in Protobuf descriptor set resource '%s'"
                                .formatted(unresolved, resourcePath));
            }
        }

        return files;
    }

}
