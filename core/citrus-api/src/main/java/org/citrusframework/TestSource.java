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

package org.citrusframework;

import java.nio.charset.StandardCharsets;

import org.citrusframework.common.TestLoader;
import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 4.0
 */
public class TestSource {

    /** Test type, name and optional source file path */
    private String type;
    private String name;
    private String filePath;

    private Resource sourceFile;

    public TestSource(String type, String name) {
        this(type, name, "%s.%s".formatted(name, type));
    }

    public TestSource(String type, String name, String filePath) {
        this.type = type;
        this.name = name;
        this.filePath = filePath;
    }

    public TestSource(Class<?> testClass) {
        this(TestLoader.JAVA, testClass.getName());

        String path = testClass.getPackageName().replace('.', '/');
        String fileName = testClass.getName() + ".java";
        sourceFile = new Resources.ClasspathResource(path.isEmpty() ? fileName : path + "/" + fileName);
    }

    /**
     * The test source type. Usually one of java, xml, groovy, yaml.
     */
    public String getType() {
        return type;
    }

    @SchemaProperty(description = "The test source type.")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the name.
     */
    public String getName() {
        return name;
    }

    @SchemaProperty(description = "The source file name.")
    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    @SchemaProperty(description = "The source file path.")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Gets the file resource for this test source.
     */
    public Resource getSourceFile() {
        if (sourceFile == null) {
            sourceFile = Resources.create(filePath);
        }

        return sourceFile;
    }

    /**
     * Gets the file resource for this test source and uses given test context
     * to resolve the file path with test variables.
     */
    public Resource getSourceFile(TestContext context) {
        if (sourceFile == null) {
            sourceFile = Resources.create(context.replaceDynamicContentInString(filePath));
        }

        return sourceFile;
    }

    public void setSourceFile(Resource sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Add source code for this test source.
     * Uses in memory resource to load the test source code.
     */
    public TestSource sourceCode(String sourceCode) {
        this.sourceFile = new Resources.ByteArrayResource(sourceCode.getBytes(StandardCharsets.UTF_8));
        return this;
    }
}
