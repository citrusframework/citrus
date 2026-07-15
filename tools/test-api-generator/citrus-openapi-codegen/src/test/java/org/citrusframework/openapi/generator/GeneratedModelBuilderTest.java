package org.citrusframework.openapi.generator;

import org.citrusframework.openapi.generator.builder.petstore.model.Pet;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that model classes are generated with a builder when the codegen option
 * {@code generateBuilders} is enabled, see execution 'generate-openapi-builder-files' in pom.xml.
 */
class GeneratedModelBuilderTest {

    @Test
    void buildsModelViaGeneratedBuilder() {
        Pet pet = Pet.builder()
            .id(1L)
            .build();

        assertThat(pet.getId()).isEqualTo(1L);
    }

    @Test
    void toBuilderCopiesExistingValues() {
        Pet pet = Pet.builder()
            .id(1L)
            .build();

        Pet copy = pet.toBuilder()
            .id(2L)
            .build();

        assertThat(copy.getId()).isEqualTo(2L);
    }
}
