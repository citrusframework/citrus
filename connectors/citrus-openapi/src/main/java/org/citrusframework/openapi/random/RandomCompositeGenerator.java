package org.citrusframework.openapi.random;

import static org.springframework.util.CollectionUtils.isEmpty;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.citrusframework.openapi.model.OasModelHelper;

/**
 * A generator for producing random composite schemas based on an OpenAPI schema. This class extends
 * the {@link RandomGenerator} and provides a specific implementation for generating composite schemas
 * with constraints defined in the schema.
 *
 * <p>The generator supports composite schemas, which include `allOf`, `anyOf`, and `oneOf` constructs.</p>
 */
public class RandomCompositeGenerator extends RandomGenerator {

    @Override
    public boolean handles(OasSchema other) {
        return OasModelHelper.isCompositeSchema(other);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {

        if (!isEmpty(schema.allOf)) {
            createAllOff(randomContext, schema);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.anyOf)) {
            createAnyOf(randomContext, oas30Schema);
        } else if (schema instanceof Oas30Schema oas30Schema && !isEmpty(oas30Schema.oneOf)) {
            createOneOf(randomContext, oas30Schema.oneOf);
        }
    }

    private static void createOneOf(RandomContext randomContext, List<OasSchema> schemas) {
        int schemaIndex = ThreadLocalRandom.current().nextInt(schemas.size());
        randomContext.getRandomModelBuilder().object(() ->
            randomContext.generate(schemas.get(schemaIndex)));
    }

    private static void createAnyOf(RandomContext randomContext, Oas30Schema schema) {

        randomContext.getRandomModelBuilder().object(() -> {
            boolean anyAdded = false;
            for (OasSchema oneSchema : schema.anyOf) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    randomContext.generate(oneSchema);
                    anyAdded = true;
                }
            }

            // Add at least one
            if (!anyAdded) {
                createOneOf(randomContext, schema.anyOf);
            }
        });
    }

    private static Map<String, Object> createAllOff(RandomContext randomContext, OasSchema schema) {
        Map<String, Object> allOf = new HashMap<>();

        randomContext.getRandomModelBuilder().object(() -> {
            for (OasSchema oneSchema : schema.allOf) {
                randomContext.generate(oneSchema);
            }
        });

        return allOf;
    }
}
