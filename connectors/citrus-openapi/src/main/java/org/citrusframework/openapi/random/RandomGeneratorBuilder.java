package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import java.util.Collections;
import java.util.function.BiConsumer;

/**
 * A simple builder for building {@link java.util.random.RandomGenerator}s.
 */
public class RandomGeneratorBuilder {

    private final OasSchema schema = new Oas30Schema();

    private RandomGeneratorBuilder() {
    }

    static RandomGeneratorBuilder builder() {
        return new RandomGeneratorBuilder();
    }

    static RandomGeneratorBuilder builder(String type, String format) {
        return new RandomGeneratorBuilder().with(type, format);
    }

    RandomGeneratorBuilder with(String type, String format) {
        schema.type = type;
        schema.format = format;
        return this;
    }


    RandomGeneratorBuilder withType(String type) {
        schema.type = type;
        return this;
    }

    RandomGeneratorBuilder withFormat(String format) {
        schema.format = format;
        return this;
    }

    RandomGeneratorBuilder withPattern(String pattern) {
        schema.pattern = pattern;
        return this;
    }

    RandomGeneratorBuilder withEnum() {
        schema.enum_ = Collections.emptyList();
        return this;
    }

    RandomGenerator build(BiConsumer<RandomContext, OasSchema> consumer) {
        return new RandomGenerator(schema) {
            @Override
            void generate(RandomContext randomContext, OasSchema schema) {
                consumer.accept(randomContext, schema);
            }
        };
    }

}