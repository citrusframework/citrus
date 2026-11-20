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

package org.citrusframework.dsl.schema.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CitrusSchemaGeneratorVersionTest {

    @Test
    void shouldRejectBlankVersion() throws IOException {
        Path outputDir = Path.of("target/schema-blank");
        deleteRecursively(outputDir);

        for (String version : new String[]{"", "   "}) {
            CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class,
                    () -> CitrusSchemaGenerator.main(new String[]{outputDir.toString(), version}));

            Assertions.assertTrue(error.getMessage().contains("META-INF/citrus.version"),
                    "Error message should name the missing version property: " + error.getMessage());
            Assertions.assertFalse(Files.exists(outputDir.resolve("citrus")),
                    "No catalog files should be written for a blank version");
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path file : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        }
    }
}
