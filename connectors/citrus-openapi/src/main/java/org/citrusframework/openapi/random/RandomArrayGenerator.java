package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import java.util.concurrent.ThreadLocalRandom;
import org.citrusframework.openapi.model.OasModelHelper;

/**
 * A generator for producing random arrays based on an OpenAPI schema. This class extends the
 * {@link RandomGenerator} and provides a specific implementation for generating random arrays
 * with constraints defined in the schema.
 *
 * <p>The generator supports arrays with items of a single schema type. If the array's items have
 * different schemas, an {@link UnsupportedOperationException} will be thrown.</p>s
 *
 */
public class RandomArrayGenerator extends RandomGenerator {

    @Override
    public boolean handles(OasSchema other) {
        return OasModelHelper.isArrayType(other);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        Object items = schema.items;

        if (items instanceof OasSchema itemsSchema) {
            createRandomArrayValueWithSchemaItem(randomContext, schema, itemsSchema);
        } else {
            throw new UnsupportedOperationException(
                "Random array creation for an array with items having different schema is currently not supported!");
        }
    }

    private static void createRandomArrayValueWithSchemaItem(RandomContext randomContext,
        OasSchema schema,
        OasSchema itemsSchema) {

        Number minItems = schema.minItems != null ? schema.minItems : 1;
        Number maxItems = schema.maxItems != null ? schema.maxItems : 10;

        int nItems = ThreadLocalRandom.current()
            .nextInt(minItems.intValue(), maxItems.intValue() + 1);

        randomContext.getRandomModelBuilder().array(() -> {
            for (int i = 0; i < nItems; i++) {
                randomContext.generate(itemsSchema);
            }
        });
    }
}
