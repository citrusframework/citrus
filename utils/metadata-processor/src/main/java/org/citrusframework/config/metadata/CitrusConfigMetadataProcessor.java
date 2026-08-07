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

package org.citrusframework.config.metadata;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citrusframework.config.CitrusConfigProperties;
import org.citrusframework.config.CitrusConfigProperty;

/**
 * Annotation processor that scans for {@link CitrusConfigProperties} and {@link CitrusConfigProperty}
 * annotations and generates {@code META-INF/spring-configuration-metadata.json} for IDE auto-completion.
 */
@SupportedAnnotationTypes({
        "org.citrusframework.config.CitrusConfigProperties",
        "org.citrusframework.config.CitrusConfigProperty"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CitrusConfigMetadataProcessor extends AbstractProcessor {

    private static final String METADATA_PATH = "META-INF/spring-configuration-metadata.json";

    private final List<GroupEntry> groups = new ArrayList<>();
    private final List<PropertyEntry> properties = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!properties.isEmpty()) {
                writeMetadata();
            }
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(CitrusConfigProperties.class)) {
            if (element instanceof TypeElement typeElement) {
                CitrusConfigProperties groupAnnotation = typeElement.getAnnotation(CitrusConfigProperties.class);
                groups.add(new GroupEntry(
                        groupAnnotation.prefix(),
                        typeElement.getQualifiedName().toString(),
                        groupAnnotation.description()
                ));

                for (Element enclosed : typeElement.getEnclosedElements()) {
                    if (enclosed instanceof VariableElement field) {
                        CitrusConfigProperty propAnnotation = field.getAnnotation(CitrusConfigProperty.class);
                        if (propAnnotation != null) {
                            Object constantValue = field.getConstantValue();
                            if (constantValue instanceof String propertyName) {
                                properties.add(new PropertyEntry(
                                        propertyName,
                                        propAnnotation.type(),
                                        propAnnotation.description(),
                                        parseDefaultValue(propAnnotation.type(), propAnnotation.defaultValue()),
                                        typeElement.getQualifiedName().toString(),
                                        propAnnotation.deprecated(),
                                        propAnnotation.replacement()
                                ));
                            } else {
                                processingEnv.getMessager().printMessage(
                                        Diagnostic.Kind.WARNING,
                                        "@CitrusConfigProperty on field '" + field.getSimpleName()
                                                + "' has no compile-time constant String value — skipped",
                                        field
                                );
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private void writeMetadata() {
        try {
            String outputDir = processingEnv.getOptions().get("citrus.metadata.outputDir");
            if (outputDir != null && !outputDir.isEmpty()) {
                Path metadataFile = Path.of(outputDir, METADATA_PATH);
                Files.createDirectories(metadataFile.getParent());
                Files.writeString(metadataFile, toJson());
            } else {
                FileObject resource = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT, "", METADATA_PATH);
                try (Writer writer = resource.openWriter()) {
                    writer.write(toJson());
                }
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write " + METADATA_PATH + ": " + e.getMessage());
        }
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of("citrus.metadata.outputDir");
    }

    String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  \"groups\": [");
        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('\n');
            groups.get(i).appendJson(sb, "    ");
        }
        if (!groups.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("],\n");

        sb.append("  \"properties\": [");
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('\n');
            properties.get(i).appendJson(sb, "    ");
        }
        if (!properties.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("],\n");

        sb.append("  \"hints\": []\n");
        sb.append("}\n");

        return sb.toString();
    }

    private static String parseDefaultValue(String type, String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return raw;
    }

    static String escapeJson(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    record GroupEntry(String name, String sourceType, String description) {
        void appendJson(StringBuilder sb, String indent) {
            sb.append(indent).append("{\n");
            sb.append(indent).append("  \"name\": \"").append(escapeJson(name)).append('"');
            sb.append(",\n").append(indent).append("  \"type\": \"").append(escapeJson(sourceType)).append('"');
            if (description != null && !description.isEmpty()) {
                sb.append(",\n").append(indent).append("  \"description\": \"").append(escapeJson(description)).append('"');
            }
            sb.append('\n').append(indent).append('}');
        }
    }

    record PropertyEntry(String name, String type, String description, String defaultValue,
                          String sourceType, boolean deprecated, String replacement) {
        void appendJson(StringBuilder sb, String indent) {
            sb.append(indent).append("{\n");
            sb.append(indent).append("  \"name\": \"").append(escapeJson(name)).append('"');
            sb.append(",\n").append(indent).append("  \"type\": \"").append(escapeJson(type)).append('"');
            if (description != null && !description.isEmpty()) {
                sb.append(",\n").append(indent).append("  \"description\": \"").append(escapeJson(description)).append('"');
            }
            sb.append(",\n").append(indent).append("  \"sourceType\": \"").append(escapeJson(sourceType)).append('"');
            if (defaultValue != null) {
                sb.append(",\n").append(indent).append("  \"defaultValue\": ");
                appendTypedValue(sb, type, defaultValue);
            }
            if (deprecated) {
                sb.append(",\n").append(indent).append("  \"deprecation\": {");
                if (replacement != null && !replacement.isEmpty()) {
                    sb.append("\"replacement\": \"").append(escapeJson(replacement)).append('"');
                }
                sb.append('}');
            }
            sb.append('\n').append(indent).append('}');
        }

        private static void appendTypedValue(StringBuilder sb, String type, String value) {
            if ("java.lang.Boolean".equals(type) || "boolean".equals(type)) {
                sb.append(value.toLowerCase());
            } else if ("java.lang.Integer".equals(type) || "int".equals(type)
                    || "java.lang.Long".equals(type) || "long".equals(type)) {
                sb.append(value);
            } else {
                sb.append('"').append(escapeJson(value)).append('"');
            }
        }
    }
}
