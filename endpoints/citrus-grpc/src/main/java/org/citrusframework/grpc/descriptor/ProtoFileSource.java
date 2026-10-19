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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.squareup.wire.schema.Extend;
import com.squareup.wire.schema.Field;
import com.squareup.wire.schema.Location;
import com.squareup.wire.schema.MessageType;
import com.squareup.wire.schema.ProtoFile;
import com.squareup.wire.schema.Schema;
import com.squareup.wire.schema.SchemaLoader;
import com.squareup.wire.schema.internal.SchemaEncoder;
import com.squareup.wire.schema.internal.parser.ProtoFileElement;
import com.squareup.wire.schema.internal.parser.ProtoParser;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;

/**
 * Descriptor source backed by {@code .proto} source files. The files and their imports are parsed
 * with Square Wire, encoded as Protobuf descriptors and completed with the declared
 * {@code json_name} options that Wire does not encode.
 */
public class ProtoFileSource implements DescriptorSource {

    private final String resourcePath;
    private final List<String> protoPaths;

    public ProtoFileSource(String resourcePath) {
        this(resourcePath, List.of());
    }

    public ProtoFileSource(String resourcePath, List<String> protoPaths) {
        this.resourcePath = resourcePath;
        this.protoPaths = List.copyOf(protoPaths);
    }

    @Override
    public List<FileDescriptor> getDescriptors() {
        Resource resource = Resources.create(resourcePath);
        if (!resource.exists()) {
            throw new DescriptorRegistryException(
                    "Unable to find Protobuf source file resource '%s'".formatted(resourcePath));
        }

        Path root = null;
        try {
            root = Files.createTempDirectory("citrus-grpc-proto");

            String logicalPath = logicalPath(resourcePath);
            Map<String, byte[]> sources = new LinkedHashMap<>();
            collect(logicalPath, read(resource), sources);
            write(root, sources);

            Schema schema = loadSchema(root, sources.keySet());
            List<FileDescriptorProto> descriptors = encode(schema, root);
            return build(descriptors);
        } catch (DescriptorRegistryException e) {
            throw e;
        } catch (IOException e) {
            throw new DescriptorRegistryException(
                    "Failed to read Protobuf source file resource '%s'".formatted(resourcePath), e);
        } catch (RuntimeException e) {
            throw new DescriptorRegistryException(
                    "Failed to load Protobuf source file '%s': %s".formatted(resourcePath, e.getMessage()), e);
        } finally {
            delete(root);
        }
    }

    private void collect(String logicalPath, byte[] content, Map<String, byte[]> sources) {
        if (sources.putIfAbsent(logicalPath, content) != null) {
            return;
        }

        for (String imported : imports(logicalPath, content)) {
            if (WellKnownTypes.isRuntimeProto(imported)) {
                continue;
            }
            collect(imported, readImport(logicalPath, imported), sources);
        }
    }

    private List<String> imports(String logicalPath, byte[] content) {
        try {
            ProtoFileElement element = new ProtoParser(Location.get(logicalPath),
                    new String(content, StandardCharsets.UTF_8).toCharArray()).readProtoFile();
            List<String> imports = new ArrayList<>(element.getImports());
            imports.addAll(element.getPublicImports());
            imports.addAll(element.getWeakImports());
            return imports;
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            if (message.contains("edition") || message.contains("group")) {
                throw new DescriptorRegistryException(
                        "Failed to parse Protobuf source file '%s': %s - use a descriptor set "
                                .formatted(logicalPath, message)
                                + "(protoc --descriptor_set_out) for this file", e);
            }
            throw new DescriptorRegistryException(
                    "Failed to parse Protobuf source file '%s': %s".formatted(logicalPath, message), e);
        }
    }

    private byte[] readImport(String importer, String imported) {
        List<String> candidates = new ArrayList<>();
        candidates.add(imported);
        for (String protoPath : protoPaths) {
            candidates.add(join(protoPath, imported));
        }
        candidates.add(join(directory(resourcePath), imported));

        for (String candidate : candidates) {
            Resource resource = Resources.create(candidate);
            if (resource.exists()) {
                return read(resource);
            }
        }

        throw new DescriptorRegistryException(
                "Unable to resolve import '%s' of Protobuf source file '%s'".formatted(imported, importer));
    }

    private Schema loadSchema(Path root, Set<String> logicalPaths) {
        SchemaLoader loader = new SchemaLoader(FileSystems.getDefault());
        List<Location> sourcePath = logicalPaths.stream()
                .map(logicalPath -> Location.get(root.toString(), logicalPath))
                .toList();
        loader.initRoots(sourcePath, List.of(Location.get(root.toString())));
        return loader.loadSchema();
    }

    private List<FileDescriptorProto> encode(Schema schema, Path root) {
        List<FileDescriptorProto> descriptors = new ArrayList<>();
        SchemaEncoder encoder = new SchemaEncoder(schema);

        for (ProtoFile protoFile : schema.getProtoFiles()) {
            String name = logicalName(protoFile, root);
            if (WellKnownTypes.isRuntimeProto(name)) {
                continue;
            }

            FileDescriptorProto encoded;
            try {
                encoded = FileDescriptorProto.parseFrom(encoder.encode(protoFile).toByteArray());
            } catch (Exception e) {
                throw new DescriptorRegistryException(
                        "Failed to encode Protobuf source file '%s': %s".formatted(name, e.getMessage()), e);
            }

            descriptors.add(patchJsonNames(encoded.toBuilder().setName(name).build(), schema));
        }

        return descriptors;
    }

    private FileDescriptorProto patchJsonNames(FileDescriptorProto proto, Schema schema) {
        Map<String, Map<Integer, String>> extensionJsonNames = extensionJsonNames(schema);
        FileDescriptorProto.Builder builder = proto.toBuilder();

        for (int i = 0; i < builder.getMessageTypeCount(); i++) {
            builder.setMessageType(i, patchMessage(builder.getMessageType(i), proto.getPackage(),
                    schema, extensionJsonNames));
        }

        for (int i = 0; i < builder.getExtensionCount(); i++) {
            builder.setExtension(i, patchField(builder.getExtension(i),
                    extensionJsonNames.getOrDefault(builder.getExtension(i).getExtendee(), Map.of())));
        }

        return builder.build();
    }

    private DescriptorProto patchMessage(DescriptorProto message, String parent, Schema schema,
                                         Map<String, Map<Integer, String>> extensionJsonNames) {
        String fullName = parent.isEmpty() ? message.getName() : parent + "." + message.getName();
        Map<Integer, String> jsonNames = new HashMap<>();

        if (schema.getType(fullName) instanceof MessageType messageType) {
            for (Field field : messageType.getFieldsAndOneOfFields()) {
                if (field.getDeclaredJsonName() != null) {
                    jsonNames.put(field.getTag(), field.getDeclaredJsonName());
                }
            }
        }

        DescriptorProto.Builder builder = message.toBuilder();
        for (int i = 0; i < builder.getFieldCount(); i++) {
            builder.setField(i, patchField(builder.getField(i), jsonNames));
        }
        for (int i = 0; i < builder.getExtensionCount(); i++) {
            builder.setExtension(i, patchField(builder.getExtension(i),
                    extensionJsonNames.getOrDefault(builder.getExtension(i).getExtendee(), Map.of())));
        }
        for (int i = 0; i < builder.getNestedTypeCount(); i++) {
            builder.setNestedType(i, patchMessage(builder.getNestedType(i), fullName,
                    schema, extensionJsonNames));
        }

        return builder.build();
    }

    private FieldDescriptorProto patchField(FieldDescriptorProto field, Map<Integer, String> jsonNames) {
        String jsonName = jsonNames.get(field.getNumber());
        if (jsonName == null) {
            return field;
        }
        return field.toBuilder().setJsonName(jsonName).build();
    }

    private Map<String, Map<Integer, String>> extensionJsonNames(Schema schema) {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        for (ProtoFile protoFile : schema.getProtoFiles()) {
            addExtensions(protoFile.getExtendList(), result);
            for (com.squareup.wire.schema.Type type : protoFile.getTypes()) {
                addNestedExtensions(type, result);
            }
        }
        return result;
    }

    private void addNestedExtensions(com.squareup.wire.schema.Type type,
                                     Map<String, Map<Integer, String>> result) {
        if (type instanceof MessageType messageType) {
            addExtensions(messageType.getNestedExtendList(), result);
        }
        for (com.squareup.wire.schema.Type nested : type.getNestedTypes()) {
            addNestedExtensions(nested, result);
        }
    }

    private void addExtensions(List<Extend> extensions, Map<String, Map<Integer, String>> result) {
        for (Extend extension : extensions) {
            Map<Integer, String> jsonNames = result.computeIfAbsent(
                    extension.getType().toString(), key -> new HashMap<>());
            for (Field field : extension.getFields()) {
                if (field.getDeclaredJsonName() != null) {
                    jsonNames.put(field.getTag(), field.getDeclaredJsonName());
                }
            }
        }
    }

    private List<FileDescriptor> build(List<FileDescriptorProto> descriptors) {
        Map<String, FileDescriptor> available = new LinkedHashMap<>(WellKnownTypes.all());
        List<FileDescriptorProto> pending = new ArrayList<>(descriptors);
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
                            "Failed to build Protobuf descriptor '%s' from source file '%s'"
                                    .formatted(proto.getName(), resourcePath), e);
                }
            }

            if (!progress) {
                throw new DescriptorRegistryException(
                        "Unable to resolve imports of %s in Protobuf source file '%s'"
                                .formatted(pending.stream().map(FileDescriptorProto::getName).toList(),
                                        resourcePath));
            }
        }

        return files;
    }

    private static void write(Path root, Map<String, byte[]> sources) throws IOException {
        for (Map.Entry<String, byte[]> source : sources.entrySet()) {
            Path target = root.resolve(source.getKey());
            Files.createDirectories(target.getParent());
            Files.write(target, source.getValue());
        }
    }

    private static void delete(Path root) {
        if (root == null) {
            return;
        }
        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup of the temporary source tree
                }
            });
        } catch (IOException ignored) {
            // best effort cleanup of the temporary source tree
        }
    }

    private static byte[] read(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            if (inputStream == null) {
                throw new IOException("resource is not readable");
            }
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new DescriptorRegistryException(
                    "Failed to read Protobuf source file resource '%s'".formatted(resource.location()), e);
        }
    }

    private static String logicalPath(String value) {
        if (value.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
            return value.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
        }
        if (value.startsWith(Resources.FILESYSTEM_RESOURCE_PREFIX)) {
            value = value.substring(Resources.FILESYSTEM_RESOURCE_PREFIX.length());
        }
        return Path.of(value).getFileName().toString();
    }

    private static String logicalName(ProtoFile protoFile, Path root) {
        String path = protoFile.getLocation().getPath();
        Path candidate = Path.of(path);
        if (candidate.isAbsolute()) {
            return root.relativize(candidate).toString();
        }
        return path;
    }

    private static String directory(String value) {
        if (value.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
            String path = value.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
            int index = path.lastIndexOf('/');
            return index < 0 ? Resources.CLASSPATH_RESOURCE_PREFIX
                    : Resources.CLASSPATH_RESOURCE_PREFIX + path.substring(0, index);
        }
        if (value.startsWith(Resources.FILESYSTEM_RESOURCE_PREFIX)) {
            String path = value.substring(Resources.FILESYSTEM_RESOURCE_PREFIX.length());
            int index = path.lastIndexOf('/');
            return index < 0 ? Resources.FILESYSTEM_RESOURCE_PREFIX
                    : Resources.FILESYSTEM_RESOURCE_PREFIX + path.substring(0, index);
        }
        int index = value.lastIndexOf('/');
        return index < 0 ? "." : value.substring(0, index);
    }

    private static String join(String base, String path) {
        return base.endsWith("/") ? base + path : base + "/" + path;
    }
}
