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

package org.citrusframework.yaml;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestSource;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.yaml.actions.CreateEndpoint;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

/**
 * Loads test case from given YAML source.
 */
public class YamlTestLoader extends DefaultTestLoader implements TestSourceAware {

    private TestSource source;

    private final Yaml yaml;

    /**
     * Default constructor.
     */
    public YamlTestLoader() {
        Constructor constructor = new Constructor(YamlTestCase.class, new LoaderOptions());

        constructor.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<?> type, String name, BeanAccess beanAccess) {
                return super.getProperty(type, camelCase(name), beanAccess);
            }
        });

        try {
            Level original = java.util.logging.Logger.getLogger("org.yaml.snakeyaml.introspector").getLevel();
            if (!Level.SEVERE.equals(original)) {
                // Avoid snakeyaml warning messages due to missing methods in org.citrusframework.yaml.TestActions
                java.util.logging.Logger.getLogger("org.yaml.snakeyaml.introspector").setLevel(java.util.logging.Level.SEVERE);
            }
        } catch (Exception e) {
            // ignore and keep the original log level
        }

        Map<String, TestActionBuilder<?>> builders = YamlTestActionBuilder.lookup();
        if (!builders.isEmpty()) {
            TypeDescription actions = new TypeDescription(TestActions.class);
            for (Map.Entry<String, TestActionBuilder<?>> builder : builders.entrySet()) {
                actions.substituteProperty(camelCase(builder.getKey().replaceAll("\\.", "-")),
                        builder.getValue().getClass(), "getAction", "setAction", TestActionBuilder.class);
            }
            constructor.addTypeDescription(actions);
        }

        Map<String, EndpointBuilder<?>> endpointBuilders = EndpointBuilder.lookup();
        if (!endpointBuilders.isEmpty()) {
            TypeDescription createEndpoint = new TypeDescription(CreateEndpoint.class);
            TypeDescription endpoint = new TypeDescription(YamlTestCase.Endpoint.class);
            for (Map.Entry<String, EndpointBuilder<?>> builder : endpointBuilders.entrySet()) {
                endpoint.substituteProperty(camelCase(builder.getKey().replaceAll("\\.", "-")),
                        builder.getValue().getClass(), "getBuilder", "setBuilder", EndpointBuilder.class);

                createEndpoint.substituteProperty(camelCase(builder.getKey().replaceAll("\\.", "-")),
                        builder.getValue().getClass(), "getEndpoint", "setEndpoint", EndpointBuilder.class);
            }

            constructor.addTypeDescription(endpoint);
            constructor.addTypeDescription(createEndpoint);
        }

        yaml = new Yaml(constructor);
    }

    /**
     * Constructor with context file and parent application context field.
     */
    public YamlTestLoader(Class<?> testClass, String testName, String packageName) {
        this();

        this.testClass = testClass;
        this.testName = testName;
        this.packageName = packageName;
    }

    @Override
    public void doLoad() {
        try {
            Resource yamlSource = getSource().getSourceFile();
            YamlTestCase tc = yaml.load(FileUtils.readToString(yamlSource));
            testCase = tc.getTestCase();
            if (runner instanceof DefaultTestCaseRunner) {
                ((DefaultTestCaseRunner) runner).setTestCase(testCase);
            }

            testCase.getActionBuilders().stream()
                    .filter(action -> ReferenceResolverAware.class.isAssignableFrom(action.getClass()))
                    .map(ReferenceResolverAware.class::cast)
                    .forEach(action -> action.setReferenceResolver(context.getReferenceResolver()));

            configurer.forEach(handler -> handler.accept(testCase));
            citrus.run(testCase, context);
            handler.forEach(handler -> handler.accept(testCase));
        } catch (IOException e) {
            throw context.handleError(testName, packageName, "Failed to load YAML test with name '" + testName + "'", e);
        }
    }

    /**
     * Gets custom source file for the YAML test case. If not set creates default
     * source file path from testName and packageName.
     */
    public TestSource getSource() {
        if (source != null) {
            return source;
        } else {
            String path = packageName.replace('.', '/');
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_YAML) ? testName : testName + FileUtils.FILE_EXTENSION_YAML;
            return new TestSource(TestLoader.YAML, testName, Resources.CLASSPATH_RESOURCE_PREFIX + path + "/" + fileName);
        }
    }

    /**
     * Sets custom source file for YAML test case.
     */
    @Override
    public void setSource(TestSource source) {
        this.source = source;
    }

    /**
     * Convert dash style to camel case.
     */
    private static String camelCase(String input) {
        if (input.indexOf('-') < 0) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '-') {
                if (i == input.length() - 1) {
                    continue;
                }
                result.append(String.valueOf(input.charAt(++i)).toUpperCase());
            } else {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }
}
