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

package org.citrusframework.openapi.random;

import java.util.Collections;
import java.util.function.BiConsumer;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;

/**
 * A simple builder for building {@link java.util.random.RandomGenerator}s.
 */
public class RandomGeneratorBuilder {

    private final OasSchema schema = new Oas30Schema();

    private RandomGeneratorBuilder() {
    }

    static RandomGeneratorBuilder randomGeneratorBuilder() {
        return new RandomGeneratorBuilder();
    }

    static RandomGeneratorBuilder randomGeneratorBuilder(String type, String format) {
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
