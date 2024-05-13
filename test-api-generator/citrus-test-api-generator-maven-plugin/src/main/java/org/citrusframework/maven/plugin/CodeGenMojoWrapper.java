package org.citrusframework.maven.plugin;

import static org.citrusframework.openapi.generator.JavaCitrusCodegen.CODEGEN_NAME;
import static java.lang.String.format;

import org.citrusframework.openapi.generator.JavaCitrusCodegen;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openapitools.codegen.plugin.CodeGenMojo;

/**
 * Wrapper class that uses reflection to expose several properties of the {@link CodeGenMojo} for explicit assignment.
 *
 * @author Thorsten Schlathoelter
 */
public class CodeGenMojoWrapper extends CodeGenMojo {

    private final Map<String, Object> configOptionsProperties = new HashMap<>();

    public CodeGenMojoWrapper() throws MojoExecutionException {
        setFixedConfigOptions();
        setPrivateField("configOptions", configOptionsProperties);
    }

    private void setFixedConfigOptions() throws MojoExecutionException {
        setPrivateField("generateSupportingFiles", true);
        setPrivateField( "generatorName", CODEGEN_NAME);
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

    public CodeGenMojoWrapper mojoExecution(MojoExecution mojoExecution) throws MojoExecutionException {
        setPrivateField("mojo", mojoExecution);
        return this;
    }

    public CodeGenMojoWrapper configOptions(Map<String, Object> configOptionsProperties) {
        this.configOptionsProperties.putAll(configOptionsProperties);
        return this;
    }

    public CodeGenMojoWrapper schemaFolder(String schemaFolder) {
        configOptionsProperties.put(JavaCitrusCodegen.GENERATED_SCHEMA_FOLDER, schemaFolder);
        return this;
    }

    public CodeGenMojoWrapper resourceFolder(String resourceFolder) {
        configOptionsProperties.put(JavaCitrusCodegen.RESOURCE_FOLDER, resourceFolder);
        return this;
    }

    public CodeGenMojoWrapper sourceFolder(String sourceFolder) {
        configOptionsProperties.put(JavaCitrusCodegen.SOURCE_FOLDER, sourceFolder);
        return this;
    }

    @SuppressWarnings("java:S3011") // Accessibility bypass
    private void setPrivateField(String fieldName, Object fieldValue) throws MojoExecutionException {
        try {
            var field = CodeGenMojo.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(this, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new MojoExecutionException(
                format("Could not reflectively set field value '%s' for field '%s'", fieldValue, fieldName));
        }
    }

}
