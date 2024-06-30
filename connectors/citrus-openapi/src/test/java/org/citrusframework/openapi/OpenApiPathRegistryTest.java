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

package org.citrusframework.openapi;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class OpenApiPathRegistryTest {

    private static final String[] SEGMENTS = {"api", "v1", "pet", "user", "order", "product",
        "category", "service", "data"};
    private static final String VARIABLE_TEMPLATE = "{%s}";
    private static final String[] VARIABLES = {"id", "userId", "orderId", "productId",
        "categoryId"};

    public static List<String> generatePaths(int numberOfPaths) {
        List<String> paths = new ArrayList<>();
        Random random = new Random();

        Set<String> allGenerated = new HashSet<>();
        while (allGenerated.size() < numberOfPaths) {
            int numberOfSegments = 1 + random.nextInt(7); // 1 to 7 segments
            StringBuilder pathBuilder = new StringBuilder("/api/v1");

            int nids = 0;
            for (int j = 0; j < numberOfSegments; j++) {
                if (nids < 2 && nids < numberOfSegments - 1 && random.nextBoolean()) {
                    nids++;
                    // Add a segment with a variable
                    pathBuilder.append("/").append(String.format(VARIABLE_TEMPLATE,
                        VARIABLES[random.nextInt(VARIABLES.length)]));
                } else {
                    // Add a fixed segment
                    pathBuilder.append("/").append(SEGMENTS[random.nextInt(SEGMENTS.length)]);
                }
            }

            String path = pathBuilder.toString();
            if (!allGenerated.contains(path)) {
                paths.add(path);
                allGenerated.add(path);
            }
        }
        return paths;
    }

    @Test
    public void insertShouldSucceedOnSameValue() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2", "root"));
        assertTrue(openApiPathRegistry.insert("/s1/s2", "root"));
        assertEquals(openApiPathRegistry.search("/s1/s2"), "root");
    }

    @Test
    public void insertShouldFailOnSamePathWithDifferentValue() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2", "root1"));
        assertFalse(openApiPathRegistry.insert("/s1/s2", "root2"));
        assertEquals(openApiPathRegistry.search("/s1/s2"), "root1");
    }

    @Test
    public void searchShouldSucceedOnPartialPathMatchWithDifferentVariables() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}", "root1"));
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id2}/s4/{id1}", "root2"));
        assertEquals(openApiPathRegistry.search("/s1/s2/1111"), "root1");
        assertEquals(openApiPathRegistry.search("/s1/s2/123/s4/222"), "root2");

        openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id2}", "root1"));
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}/s4/{id2}", "root2"));
        assertEquals(openApiPathRegistry.search("/s1/s2/1111"), "root1");
        assertEquals(openApiPathRegistry.search("/s1/s2/123/s4/222"), "root2");
    }

    @Test
    public void insertShouldFailOnMatchingPathWithDifferentValue() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2", "root1"));
        assertFalse(openApiPathRegistry.insert("/s1/{id1}", "root2"));
        assertEquals(openApiPathRegistry.search("/s1/s2"), "root1");
        assertNull(openApiPathRegistry.search("/s1/111"));

        assertTrue(openApiPathRegistry.insert("/s1/s2/s3/{id2}", "root3"));
        assertFalse(openApiPathRegistry.insert("/s1/{id1}/s3/{id2}", "root4"));
        assertEquals(openApiPathRegistry.search("/s1/s2/s3/123"), "root3");
        assertEquals(openApiPathRegistry.search("/s1/s2/s3/456"), "root3");
        assertNull(openApiPathRegistry.search("/s1/111/s3/111"));

        openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/{id1}", "root2"));
        assertFalse(openApiPathRegistry.insert("/s1/s2", "root1"));
        assertEquals(openApiPathRegistry.search("/s1/111"), "root2");
        assertEquals(openApiPathRegistry.search("/s1/s2"), "root2");

        assertTrue(openApiPathRegistry.insert("/s1/{id1}/s3/{id2}", "root3"));
        assertFalse(openApiPathRegistry.insert("/s1/s2/s3/{id2}", "root4"));
        assertEquals(openApiPathRegistry.search("/s1/5678/s3/1234"), "root3");
        assertEquals(openApiPathRegistry.search("/s1/s2/s3/1234"), "root3");
    }

    @Test
    public void insertShouldNotOverwriteNested() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}", "root1"));
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}/s3/{id2}", "root2"));
        assertEquals(openApiPathRegistry.search("/s1/s2/123"), "root1");
        assertEquals(openApiPathRegistry.search("/s1/s2/1233/s3/121"), "root2");

        openApiPathRegistry = new OpenApiPathRegistry<>();
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}/s3/{id2}", "root2"));
        assertTrue(openApiPathRegistry.insert("/s1/s2/{id1}", "root1"));
        assertEquals(openApiPathRegistry.search("/s1/s2/123"), "root1");
        assertEquals(openApiPathRegistry.search("/s1/s2/1233/s3/121"), "root2");
    }

    @Test
    public void randomAccess() {
        OpenApiPathRegistry<String> openApiPathRegistry = new OpenApiPathRegistry<>();

        int numberOfPaths = 1000; // Specify the number of paths you want to generate
        List<String> paths = generatePaths(numberOfPaths);

        Map<String, String> pathToValueMap = paths.stream()
            .collect(Collectors.toMap(path -> path, k -> k.replaceAll("\\{[a-zA-Z]*}", "1111")));
        paths.removeIf(path -> !openApiPathRegistry.insert(path, pathToValueMap.get(path)));

        Random random = new Random();
        int[] indexes = new int[1000];
        for (int i = 0; i < 1000; i++) {
            indexes[i] = random.nextInt(paths.size() - 1);
        }

        for (int i = 0; i < 1000; i++) {
            String path = paths.get(indexes[i]);
            String realPath = pathToValueMap.get(path);
            String result = openApiPathRegistry.search(realPath);
            Assert.assertNotNull(result,
                "No result for real path " + realPath + " expected a match by path " + path);
            Assert.assertEquals(result, realPath);
        }
    }
}
