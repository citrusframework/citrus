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

package org.citrusframework.maven.plugin;

import static java.lang.String.format;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.CODEGEN_NAME;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.GENERATED_SCHEMA_FOLDER;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.RESOURCE_FOLDER;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.ROOT_CONTEXT_PATH;
import static org.citrusframework.openapi.generator.CitrusJavaCodegen.SOURCE_FOLDER;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openapitools.codegen.plugin.CodeGenMojo;

/**
 * Wrapper class that uses reflection to expose several properties of the {@link CodeGenMojo} for
 * explicit assignment.
 */
public class CodeGenMojoWrapper extends CodeGenMojo {



    @SuppressWarnings("rawtypes")
    private final Map configOptionsProperties = new HashMap<>();

    public CodeGenMojoWrapper() throws MojoExecutionException {
        setFixedConfigOptions();
        setPrivateField("configOptions", configOptionsProperties);
    }

    private void setFixedConfigOptions() throws MojoExecutionException {
        setPrivateField("generateSupportingFiles", true);
        setPrivateField("generatorName", CODEGEN_NAME);
    }

    public CodeGenMojoWrapper project(MavenProject mavenProject) throws MojoExecutionException {
        setPrivateField("project", mavenProject);
        return this;
    }

    public CodeGenMojoWrapper output(File output) throws MojoExecutionException {
        setPrivateField("output", output);
        return this;
    }

    public CodeGenMojoWrapper inputSpec(String inputSpec) throws MojoExecutionException {
        setPrivateField("inputSpec", inputSpec);
        return this;
    }

    public CodeGenMojoWrapper mojoExecution(MojoExecution mojoExecution)
        throws MojoExecutionException {
        setPrivateField("mojo", mojoExecution);
        return this;
    }

    public CodeGenMojoWrapper configOptions(
        @SuppressWarnings("rawtypes") Map configProperties) {
        //noinspection unchecked
        configOptionsProperties.putAll(configProperties);

        propagateContextPathToCodegen();
        return this;
    }

    /**
     * In version 7.9 of the code generator, the basePath cannot be configured directly. Additionally,
     * if the OpenAPI server specifies a hostname, that hostname becomes part of the basePath.
     * This behavior is incompatible with the API generator, as the host is already provided by the Citrus endpoint.
     * Therefore, the contextPath is used instead of the basePath. The contextPath is passed to the code generator
     * via additional-properties to ensure proper configuration.
     */
    private void propagateContextPathToCodegen() {
        // Pass in contextPath as contextPath into the generator
        if (configOptionsProperties.containsKey(ROOT_CONTEXT_PATH)) {
            // Note that an null value indicates "no context path".
            String contextPath = (String) configOptionsProperties.get(ROOT_CONTEXT_PATH);
            contextPath = contextPath == null ? "" : contextPath;
            //noinspection unchecked
            configOptionsProperties.put("additional-properties",
                "rootContextPath=" + contextPath);
        }
    }

    public CodeGenMojoWrapper globalProperties(@SuppressWarnings("rawtypes") Map globalProperties) {
        //noinspection unchecked
        this.globalProperties.putAll(globalProperties);
        return this;
    }

    public CodeGenMojoWrapper schemaFolder(String schemaFolder) {
        //noinspection unchecked
        configOptionsProperties.put(GENERATED_SCHEMA_FOLDER, schemaFolder);
        return this;
    }

    public CodeGenMojoWrapper resourceFolder(String resourceFolder) {
        //noinspection unchecked
        configOptionsProperties.put(RESOURCE_FOLDER, resourceFolder);
        return this;
    }

    public CodeGenMojoWrapper sourceFolder(String sourceFolder) {
        //noinspection unchecked
        configOptionsProperties.put(SOURCE_FOLDER, sourceFolder);
        return this;
    }

    // Accessibility bypass
    @SuppressWarnings("java:S3011")
    private void setPrivateField(String fieldName, Object fieldValue)
        throws MojoExecutionException {
        try {
            var field = CodeGenMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MojoExecutionException(
                format("Could not reflectively set field value '%s' for field '%s'", fieldValue,
                    fieldName));
        }
    }
}
