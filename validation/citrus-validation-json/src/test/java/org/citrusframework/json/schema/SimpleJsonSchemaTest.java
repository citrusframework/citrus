package org.citrusframework.json.schema;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ByteArrayResource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.citrusframework.spi.Resources.FileSystemResource;
import org.citrusframework.spi.Resources.UrlResource;
import org.testng.annotations.Test;

public class SimpleJsonSchemaTest {

    private static final String VALID_JSON_SCHEMA = """
            {
              "$schema": "http://json-schema.org/draft-07/schema#",
              "type": "object",
              "properties": {
                "name": { "type": "string" }
              }
            }
            """;

    private static final String INVALID_JSON_SCHEMA = "{ not valid json schema";

    @Test
    public void constructor_withResource_setsJsonField() {
        Resource resource = createByteArrayResource(VALID_JSON_SCHEMA);

        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);

        assertEquals(simpleJsonSchema.getJson(), resource);
        assertNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void defaultConstructor_leavesFieldsUninitialized() {
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema();

        assertNull(simpleJsonSchema.getJson());
        assertNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withByteArrayResource_parsesSchema() {
        ByteArrayResource resource = createByteArrayResource(VALID_JSON_SCHEMA);
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withUrlResource_parsesSchema() throws Exception {
        ClasspathResource resource = new ClasspathResource("classpath:org/citrusframework/json/schema/jsonSchema.json");
        URL url = resource.getFile().toURI().toURL();
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(new UrlResource(url));

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withClasspathResource_parsesSchema() {
        ClasspathResource resource = new ClasspathResource("classpath:org/citrusframework/json/schema/jsonSchema.json");
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withFileSystemResource_parsesSchema() {
        ClasspathResource resource = new ClasspathResource("classpath:org/citrusframework/json/schema/jsonSchema.json");
        FileSystemResource fileSystemResource = new FileSystemResource(resource.getFile());
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(fileSystemResource);

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withJarUrlResource_parsesSchema() throws Exception {
        ClasspathResource resource = new ClasspathResource("classpath:org/citrusframework/json/schema/test-schema.jar");

        URL url = resource.getFile().toURI().toURL();
        URL jarUrl = new URL("jar:"+url+"!/jsonschema.json");
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(new UrlResource(jarUrl));

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withUrlResourceFallback_usesInputStream() throws Exception {
        UrlResource urlResource = new UrlResource(new URL("http://"+ UUID.randomUUID().toString())) {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(VALID_JSON_SCHEMA.getBytes(StandardCharsets.UTF_8));
            }
        };

        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(urlResource);

        simpleJsonSchema.initialize();

        assertNotNull(simpleJsonSchema.getSchema());
    }

    @Test
    public void initialize_withInvalidSchema_throwsCitrusRuntimeException() {
        ByteArrayResource resource = createByteArrayResource(INVALID_JSON_SCHEMA);
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema(resource);

        assertThrows(CitrusRuntimeException.class, simpleJsonSchema::initialize);
    }

    @Test
    public void setJson_updatesJsonField() {
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema();
        Resource resource = createByteArrayResource(VALID_JSON_SCHEMA);

        simpleJsonSchema.setJson(resource);

        assertEquals(simpleJsonSchema.getJson(), resource);
    }

    @Test
    public void equals_sameInstance_returnsTrue() {
        SimpleJsonSchema simpleJsonSchema = new SimpleJsonSchema();

        assertEquals(simpleJsonSchema, simpleJsonSchema);
    }

    private ByteArrayResource createByteArrayResource(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }

}