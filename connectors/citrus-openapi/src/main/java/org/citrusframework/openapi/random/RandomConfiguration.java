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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.apicurio.datamodels.openapi.models.OasSchema;

import static org.citrusframework.openapi.OpenApiConstants.FORMAT_DATE;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_DATE_TIME;
import static org.citrusframework.openapi.OpenApiConstants.FORMAT_UUID;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_BOOLEAN;
import static org.citrusframework.openapi.OpenApiConstants.TYPE_STRING;
import static org.citrusframework.openapi.random.RandomGenerator.ANY;
import static org.citrusframework.openapi.random.RandomGenerator.NOOP_RANDOM_GENERATOR;
import static org.citrusframework.openapi.random.RandomGeneratorBuilder.randomGeneratorBuilder;

/**
 * Configuration class that initializes and manages a list of random generators
 * for producing random data based on an OpenAPI schema. This class is a singleton
 * and provides a static instance {@code RANDOM_CONFIGURATION} for global access.
 */
public class RandomConfiguration {

    public static final RandomConfiguration RANDOM_CONFIGURATION = new RandomConfiguration();

    // Patterns for random generation (potentially simplified)
    private static final String EMAIL_PATTERN = "[a-z]{5,15}\\.?[a-z]{5,15}\\@[a-z]{5,15}\\.[a-z]{2}";
    private static final String URI_PATTERN = "((http|https)://[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/[a-zA-Z0-9-]+){1,6})|(file:///[a-zA-Z0-9-]+(/[a-zA-Z0-9-]+){1,6})";
    private static final String HOSTNAME_PATTERN = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
    private static final String IPV4_PATTERN = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    private static final String IPV6_PATTERN = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    private final List<RandomGenerator> randomGenerators;

    private RandomConfiguration() {
        List<RandomGenerator> generators = new ArrayList<>();

        // Note that the order of generators in the list is relevant, as the list is traversed from start to end, to find the first matching generator for a schema, and some generators match for less significant schemas.
        generators.add(new RandomEnumGenerator());
        generators.add(randomGeneratorBuilder(TYPE_STRING, FORMAT_DATE).build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:currentDate('yyyy-MM-dd')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, FORMAT_DATE_TIME).build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:currentDate('yyyy-MM-dd'T'hh:mm:ssZ')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, FORMAT_UUID).build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomUUID()")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, "email").build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + EMAIL_PATTERN + "')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, "uri").build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + URI_PATTERN + "')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, "hostname").build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + HOSTNAME_PATTERN + "')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, "ipv4").build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + IPV4_PATTERN + "')")));
        generators.add(randomGeneratorBuilder(TYPE_STRING, "ipv6").build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + IPV6_PATTERN + "')")));
        generators.add(randomGeneratorBuilder().withType(TYPE_STRING).withPattern(ANY).build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimpleQuoted("citrus:randomPattern('" + schema.pattern + "')")));
        generators.add(randomGeneratorBuilder().withType(TYPE_BOOLEAN).build((randomContext, schema) -> randomContext.getRandomModelBuilder().appendSimple("citrus:randomEnumValue('true', 'false')")));
        generators.add(new RandomStringGenerator());
        generators.add(new RandomCompositeGenerator());
        generators.add(new RandomNumberGenerator());
        generators.add(new RandomObjectGenerator());
        generators.add(new RandomArrayGenerator());

        randomGenerators = Collections.unmodifiableList(generators);
    }

    public RandomGenerator getGenerator(OasSchema oasSchema) {
        return randomGenerators.stream().filter(generator -> generator.handles(oasSchema))
                .findFirst()
                .orElse(NOOP_RANDOM_GENERATOR);
    }
}
