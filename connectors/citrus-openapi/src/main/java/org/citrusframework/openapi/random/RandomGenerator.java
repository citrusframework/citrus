package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;

import java.util.Objects;

/**
 * Abstract base class for generators that produce random data based on an OpenAPI schema.
 * Subclasses must implement the {@link #generate} method to provide specific random data generation logic.
 *
 * <p>The class provides methods for determining if a generator can handle a given schema,
 * based on the schema type, format, pattern, and enum constraints.
 */
public abstract class RandomGenerator {

    public static final String ANY = "$ANY$";
    public static final RandomGenerator NOOP_RANDOM_GENERATOR = new RandomGenerator() {

        @Override
        void generate(RandomContext randomContext, OasSchema schema) {
            // Do nothing
        }
    };
    private final OasSchema schema;

    protected RandomGenerator() {
        this.schema = null;
    }

    protected RandomGenerator(OasSchema schema) {
        this.schema = schema;
    }

    public boolean handles(OasSchema other) {
        if (other == null || schema == null) {
            return false;
        }

        if (ANY.equals(schema.type) || Objects.equals(schema.type, other.type)) {
            if (schema.format != null) {
                return (ANY.equals(schema.format) && other.format != null) || Objects.equals(schema.format, other.format);
            }

            if (schema.pattern != null) {
                return (ANY.equals(schema.pattern) && other.pattern != null) || Objects.equals(schema.pattern, other.pattern);
            }

            if (schema.enum_ != null && other.enum_ != null) {
                return true;
            }

            return true;
        }

        return false;
    }

    abstract void generate(RandomContext randomContext, OasSchema schema);

}