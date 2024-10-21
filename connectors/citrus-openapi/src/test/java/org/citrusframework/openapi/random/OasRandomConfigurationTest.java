package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.citrusframework.openapi.OpenApiConstants.FORMAT_DATE;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_DATE_TIME;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_UUID;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_BOOLEAN;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_STRING;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class OasRandomConfigurationTest {

    private RandomConfiguration randomConfiguration;

    @BeforeClass
    public void setUp() {
        randomConfiguration = RandomConfiguration.RANDOM_CONFIGURATION;
    }

    @Test
    public void testGetGeneratorForDateFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = FORMAT_DATE;

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForDateTimeFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = FORMAT_DATE_TIME;

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForUUIDFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = FORMAT_UUID;

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForEmailFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = "email";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForURIFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = "uri";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForHostnameFormat() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = "hostname";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForIPv4Format() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = "ipv4";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForIPv6Format() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;
        schema.format = "ipv6";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForBooleanType() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_BOOLEAN;

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForStringType() {
        OasSchema schema = new Oas30Schema();
        schema.type = TYPE_STRING;

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForNumberType() {
        OasSchema schema = new Oas30Schema();
        schema.type = "number";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForObjectType() {
        OasSchema schema = new Oas30Schema();
        schema.type = "object";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForArrayType() {
        OasSchema schema = new Oas30Schema();
        schema.type = "array";

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForEnum() {
        OasSchema schema = new Oas30Schema();
        schema.enum_ = List.of("value1", "value2");

        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertTrue(generator.handles(schema));
    }

    @Test
    public void testGetGeneratorForNullSchema() {
        OasSchema schema = new Oas30Schema();
        RandomGenerator generator = randomConfiguration.getGenerator(schema);
        assertNotNull(generator);
        assertSame(generator, RandomGenerator.NOOP_RANDOM_GENERATOR);
    }
}
