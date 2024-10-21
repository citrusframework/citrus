package org.citrusframework.openapi.random;

import io.apicurio.datamodels.openapi.models.OasSchema;
import io.apicurio.datamodels.openapi.v3.models.Oas30Schema;
import org.citrusframework.openapi.util.OpenApiUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static org.citrusframework.openapi.OpenApiConstants.TYPE_OBJECT;

/**
 * A generator for producing random objects based on an OpenAPI schema. This class extends
 * the {@link RandomGenerator} and provides a specific implementation for generating objects
 * with properties defined in the schema.
 * <p>
 * The generator supports object schemas and prevents recursion by keeping track of the
 * schemas being processed.</p>
 */
public class RandomObjectGenerator extends RandomGenerator {

    private static final String OBJECT_STACK = "OBJECT_STACK";

    private static final OasSchema OBJECT_SCHEMA = new Oas30Schema();

    static {
        OBJECT_SCHEMA.type = TYPE_OBJECT;
    }

    public RandomObjectGenerator() {
        super(OBJECT_SCHEMA);
    }

    @Override
    void generate(RandomContext randomContext, OasSchema schema) {
        Deque<OasSchema> objectStack = randomContext.get(OBJECT_STACK, k -> new ArrayDeque<>());

        if (objectStack.contains(schema)) {
            // If we have already created this schema, we are very likely in a recursion and need to stop.
            return;
        }

        objectStack.push(schema);
        randomContext.getRandomModelBuilder().object(() -> {
            if (schema.properties != null) {
                for (Map.Entry<String, OasSchema> entry : schema.properties.entrySet()) {
                    if (randomContext.getSpecification().isGenerateOptionalFields()
                            || OpenApiUtils.isRequired(schema, entry.getKey())) {
                        randomContext.getRandomModelBuilder()
                                .property(entry.getKey(), () -> randomContext.generate(entry.getValue()));
                    }
                }
            }
        });

        objectStack.pop();
    }
}
